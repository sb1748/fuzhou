package com.push.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSONObject;
import com.push.util.JDYAPIUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


/**
 * 
 * <pre>
 * <b>.</b>
 * <b>Description:</b> 
 *    A4推送到A35
 * <b>Author:</b> yanwei
 * <b>Date:</b> 2019年8月2日上午10:06:38
 * </pre>
 */
public class Server {

	// 简道云（采用数据推送）
	private static final String SECRET = "SlKOlvWPSBLTxyYwaxZSBEwe";
	private static final String DATA_CREATE = "data_create";
	
	private static final String DATA_UPDATE = "data_update";

	//A35，聚合流转
	final static String APPID = "5ca877ccf99f2279a9dd6d14";
	final static String ENTRYID = "5df05a44b579470006b2c5f6";
	final static String APIKEY = "gyDVrjxlqwalw01Dx0UYjXj4PqLGDyOl";
	
	public static final String URL = "";
	public static final String USER = "";
	public static final String PASSWORD = "";
	
	// 生成签名信息
	private static String getSignature(String nonce, String payload, String secret, String timestamp) {
		return DigestUtils.sha1Hex(nonce + ":" + payload + ":" + secret + ":" + timestamp);
	}

	// 获取GET请求中的参数
	private static Map<String, String> parseParameter(String query) {
		Map<String, String> paramMap = new HashMap<String, String>();
		String[] params = query.split("&");
		for (String param : params) {
			String[] keyValue = param.split("=");
			paramMap.put(keyValue[0], keyValue[1]);
		}
		return paramMap;
	}

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(3033), 0);
		server.createContext("/callbackA4", new HttpHandler() {
			@Override
			public void handle(HttpExchange httpExchange) throws IOException {
				String method = httpExchange.getRequestMethod();
				if (method.equalsIgnoreCase("post")) {
					String payload = IOUtils.toString(httpExchange.getRequestBody(), "utf-8");
					String jdy = httpExchange.getRequestHeaders().get("x-jdy-signature").get(0);
					URI uri = httpExchange.getRequestURI();
					Map<String, String> parameterMap = parseParameter(uri.getRawQuery());
					String nonce = parameterMap.get("nonce");
					String timestamp = parameterMap.get("timestamp");
					String signature = Server.getSignature(nonce, payload, SECRET, timestamp);
					OutputStream out = httpExchange.getResponseBody();
					if (!signature.equals(jdy)) {
						httpExchange.sendResponseHeaders(401, 0);
						out.write("fail".getBytes());
						out.close();
						return;
					}
					httpExchange.sendResponseHeaders(200, 0);
					out.write("success".getBytes());
					out.close();
					// 处理数据 - 入库出库等处理
					handleData(payload);
				}
			}
		});

		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		
		//===========================部署到服务器上之前，先注释掉上面的代码，执行下面的代码，把历史数据添加到数据表中，执行完后再注释掉下面的代码，把上面的代码注释去掉==========================
		try {
			String A4_APPID = "5ca877ccf99f2279a9dd6d14";
			String A4_ENTRYID = "5c25d8b26a042e44db97198c";
			String APIKEY = "gyDVrjxlqwalw01Dx0UYjXj4PqLGDyOl";
			JDYAPIUtils api = new JDYAPIUtils(A4_APPID, A4_ENTRYID, APIKEY);
			List<Map<String, Object>> a4 = api.getAllFormData(null, null);
			Connection conn=null;
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			conn.setAutoCommit(false);
			//添加数据
			String sql = "insert into a4(zj,cgsl,bdid) values(?,?,?)";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < a4.size(); i++) {
				ArrayList<Map<String, Object>> lists = (ArrayList<Map<String, Object>>)a4.get(i).get("_widget_1560134627996");
				for (int j = 0; j < lists.size(); j++) {
					if("".equals(getString(lists.get(j).get("_widget_1573520553730")))){
						continue;
					}
					pstmt.setString(1, lists.get(j).get("_widget_1573520553730").toString().replace("𡋾", "别"));
					pstmt.setString(2, lists.get(j).get("_widget_1560134628849").toString());
					pstmt.setString(3, a4.get(i).get("_id").toString());
					pstmt.addBatch();
				}
			}
			pstmt.executeBatch();
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * 处理推送来的数据
	 * 
	 * @param payload
	 *            推送的数据
	 */
	private static void handleData(final String payload) {
		Runnable process = new Runnable() {
			@Override
			public void run() {
				// 解析为json字符串
				JSONObject payloadJSON = JSONObject.parseObject(payload);
				String op = (String) payloadJSON.get("op");
				JSONObject data = (JSONObject) payloadJSON.get("data");
				// 新数据提交
				if (DATA_CREATE.equals(op)) {
					addServer(data);
				}
				 if (DATA_UPDATE.equals(op)) {
					 addServer(data);
				 }
			}
		};
		new Thread(process).start();
	}

	public static void addServer(JSONObject data) {
		try {
			Connection conn=null;
			ResultSet rs =null;
			JDYAPIUtils api = new JDYAPIUtils(APPID, ENTRYID, APIKEY);
			List<Object> lists = JSONObject.parseArray(data.getString("_widget_1560134627996"));//获取当前单据下的采购清单
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			conn.setAutoCommit(false);
			//无论是否有数据，先删除
			String sql = "delete from a4 where bdid=?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, data.getString("_id"));
			pstmt.executeUpdate();
			conn.commit();
			//添加数据
			sql = "insert into a4(zj,cgsl,bdid) values(?,?,?)";
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < lists.size(); i++) {
				if("".equals(getString(((Map)lists.get(i)).get("_widget_1573520553730")))){
					continue;
				}
				pstmt.setString(1, ((Map)lists.get(i)).get("_widget_1573520553730").toString().replace("𡋾", "别"));
				pstmt.setString(2, ((Map)lists.get(i)).get("_widget_1560134628849").toString());
				pstmt.setString(3, data.getString("_id"));
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.commit();
			List<Map<String, Object>> listMap=getListMap(lists);
			removeDuplicate(listMap);//按照（主键）产品+全信息+门店+手工单号+销售备注去重
			for (int i = 0; i < listMap.size(); i++) {
				String sql2 = "select * from a4 where zj=?";
				pstmt = (PreparedStatement) conn.prepareStatement(sql2);
				pstmt.setString(1, listMap.get(i).get("_widget_1576034485145").toString());
				rs = pstmt.executeQuery();
				int cgsl=0;
				while (rs.next()) {
					cgsl += rs.getInt("cgsl");
				}
				final List<Map<String, Object>> condList = new ArrayList<Map<String, Object>>();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("field", "_widget_1576034485145");
				map.put("type", "text");
				map.put("method", "eq");
				map.put("value", ((Map)lists.get(i)).get("_widget_1573520553730"));
				condList.add(map);
				Map<String, Object> filter = new HashMap<String, Object>() {
					{
						put("rel", "and");
						put("cond", condList);
					}
				};
				List<Map<String, Object>> a35 = api.getAllFormData(null, filter);
				Map<String, Object> rawData = new HashMap<String, Object>();
				//采购数量
				Map<String, Object> m1 = new HashMap<String, Object>();
				m1.put("value", cgsl);
				rawData.put("_widget_1577956557808", m1);
				//待采购数
				Map<String, Object> m2 = new HashMap<String, Object>();
				m2.put("value", getInteger(a35.get(0).get("_widget_1576034485288"))-cgsl-getInteger(a35.get(0).get("_widget_1576034485303")));
				rawData.put("_widget_1577956557767", m2);
				//采购产品显示
				Map<String, Object> m3 = new HashMap<String, Object>();
				m3.put("value", getInteger(a35.get(0).get("_widget_1576034485288"))-cgsl-getInteger(a35.get(0).get("_widget_1576034485303"))>0?0:1);
				rawData.put("_widget_1577956557604", m3);
				api.updateData(a35.get(0).get("_id").toString(), rawData);
			}
			close(rs, pstmt, conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getString(Object obj){
		if(obj==null){
			return "";
		}
		return obj.toString();
	}
	
	public static Integer getInteger(Object obj){
		if(obj==null || "".equals(obj.toString())){
			return 0;
		}
		return Integer.parseInt(obj.toString());
	}
	
	/**
	 * 
	 *<pre>
	 *<b>.</b>
	 *<b>Description:</b> 
	 *    取出当前合同下所有产品
	 *<b>Author:</b> yanwei
	 *<b>Date:</b> 2020年1月9日 上午9:34:14
	 *@param lists
	 *@return
	 *</pre>
	 */
	public static List<Map<String, Object>> getListMap(List<Object> lists){
		List<Map<String, Object>> arrayLists=new ArrayList<Map<String, Object>>();
		for (int i = 0; i < lists.size(); i++) {
			if("".equals(getString(((Map)lists.get(i)).get("_widget_1573520553730")))){
				continue;
			}
			Map<String, Object> map=new HashMap<String, Object>();
			map.put("_widget_1576034485145", getString(((Map)lists.get(i)).get("_widget_1573520553730")).replace("𡋾", "别"));
			arrayLists.add(map);
		}
		return arrayLists;
	}
	
	public static List<Map<String, Object>> removeDuplicate(List<Map<String, Object>> list) {
		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = list.size() - 1; j > i; j--) {
				if (list.get(j).get("_widget_1576034485145").toString().equals(list.get(i).get("_widget_1576034485145").toString())) {
					list.remove(j);
				}
			}
		}
		return list;
	}
	
	public static void close(ResultSet rs,PreparedStatement pstmt,Connection conn){
		if(rs!=null){
			try {
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(pstmt!=null){
			try {
				pstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(conn!=null){
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
