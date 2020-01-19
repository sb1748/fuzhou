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
 *    A3.1推送到A35
 * <b>Author:</b> yanwei
 * <b>Date:</b> 2019年8月2日上午10:06:38
 * </pre>
 */
public class Server {

	// 简道云（采用数据推送）
	private static final String SECRET = "6jKxkZg0A5sDdCB44jmgi0u5";
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
		HttpServer server = HttpServer.create(new InetSocketAddress(3032), 0);
		server.createContext("/callbackA3_1", new HttpHandler() {
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
		
		
		//===========================部署到服务器上之前，先注释掉上面的代码，执行下面的代码，执行完后再注释掉下面的代码，把上面的代码注释去掉==========================
		try {
			String A3_1_APPID = "5ca877ccf99f2279a9dd6d14";
			String A3_1_ENTRYID = "5da13fe452d0340006420aaa";
			String APIKEY = "gyDVrjxlqwalw01Dx0UYjXj4PqLGDyOl";
			JDYAPIUtils api = new JDYAPIUtils(A3_1_APPID, A3_1_ENTRYID, APIKEY);
			List<Map<String, Object>> a3_1 = api.getAllFormData(null, null);
			Connection conn=null;
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			conn.setAutoCommit(false);
			//添加数据
			String sql = "insert into a3_1(qxx,sgdh,cpbm,cpqm,xsbz,xsmc,zj,dhsl,thsl,dhftje,thftje,ddzpbj,bdid) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < a3_1.size(); i++) {
				ArrayList<Map<String, Object>> lists = (ArrayList<Map<String, Object>>)a3_1.get(i).get("_widget_1545988304733");
				for (int j = 0; j < lists.size(); j++) {
					String qxx=((Map)lists.get(j)).get("_widget_1558495976018")+"-"+((Map)lists.get(j)).get("_widget_1559206739451");
					pstmt.setString(1, qxx.replace("𡋾", "别"));
					pstmt.setString(2, lists.get(j).get("_widget_1551859128121").toString());
					pstmt.setString(3, lists.get(j).get("_widget_1546527150734").toString());
					pstmt.setString(4, lists.get(j).get("_widget_1546132677648").toString());
					pstmt.setString(5, lists.get(j).get("_widget_1551671731631").toString());
					pstmt.setString(6, lists.get(j).get("_widget_1558852228099").toString());
					String zj=getString(lists.get(j).get("_widget_1546132677648"))+lists.get(j).get("_widget_1558495976018")+"-"+lists.get(j).get("_widget_1559206739451")+getString(lists.get(j).get("_widget_1551859128121"))+getString(lists.get(j).get("_widget_1551671731631"));
					pstmt.setString(7, zj.replace("𡋾", "别"));
					pstmt.setInt(8, getInteger(lists.get(j).get("_widget_1546140576740")));
					pstmt.setInt(9, getInteger(lists.get(j).get("_widget_1564381839630")));
					pstmt.setDouble(10, getDouble(lists.get(j).get("_widget_1572746817579")));
					pstmt.setDouble(11, getDouble(lists.get(j).get("_widget_1571802785809")));
					pstmt.setDouble(12, getDouble(lists.get(j).get("_widget_1558423889365")));
					pstmt.setString(13, a3_1.get(i).get("_id").toString());
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
			ResultSet rs=null;
			List<Object> lists = JSONObject.parseArray(data.getString("_widget_1545988304733"));//获取当前单据下的商品明细
			JDYAPIUtils api = new JDYAPIUtils(APPID, ENTRYID, APIKEY);
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			conn.setAutoCommit(false);
			//无论是否有数据，先删除
			String sql = "delete from a3_1 where bdid=?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, data.getString("_id"));
			pstmt.executeUpdate();
			conn.commit();
			//添加数据
			sql = "insert into a3_1(qxx,sgdh,cpbm,cpqm,xsbz,xsmc,zj,dhsl,thsl,dhftje,thftje,ddzpbj,bdid) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < lists.size(); i++) {
				String qxx=((Map)lists.get(i)).get("_widget_1558495976018")+"-"+((Map)lists.get(i)).get("_widget_1559206739451");
				pstmt.setString(1, qxx.replace("𡋾", "别"));
				pstmt.setString(2, ((Map)lists.get(i)).get("_widget_1551859128121").toString());
				pstmt.setString(3, ((Map)lists.get(i)).get("_widget_1546527150734").toString());
				pstmt.setString(4, ((Map)lists.get(i)).get("_widget_1546132677648").toString());
				pstmt.setString(5, ((Map)lists.get(i)).get("_widget_1551671731631").toString());
				pstmt.setString(6, ((Map)lists.get(i)).get("_widget_1558852228099").toString());
				String zj=getString(((Map)lists.get(i)).get("_widget_1546132677648"))+((Map)lists.get(i)).get("_widget_1558495976018")+"-"+((Map)lists.get(i)).get("_widget_1559206739451")+getString(((Map)lists.get(i)).get("_widget_1551859128121"))+getString(((Map)lists.get(i)).get("_widget_1551671731631"));
				pstmt.setString(7, zj.replace("𡋾", "别"));
				pstmt.setInt(8, getInteger(((Map)lists.get(i)).get("_widget_1546140576740")));
				pstmt.setInt(9, getInteger(((Map)lists.get(i)).get("_widget_1564381839630")));
				pstmt.setDouble(10, getDouble(((Map)lists.get(i)).get("_widget_1572746817579")));
				pstmt.setDouble(11, getDouble(((Map)lists.get(i)).get("_widget_1571802785809")));
				pstmt.setDouble(12, getDouble(((Map)lists.get(i)).get("_widget_1558423889365")));
				pstmt.setString(13, data.getString("_id"));
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.commit();
			List<Map<String, Object>> listMap=getListMap(lists);
			removeDuplicate(listMap);//按照（主键）产品+全信息+门店+手工单号+销售备注去重
			for (int i = 0; i < listMap.size(); i++) {
				String sql2 = "select * from a3_1 where zj=?";
				pstmt = (PreparedStatement) conn.prepareStatement(sql2);
				pstmt.setString(1, listMap.get(i).get("_widget_1576034485145").toString());
				rs = pstmt.executeQuery();
				int dhsl=0;
				int thsl=0;
				double dhftje=0.0;
				double thftje=0.0;
				double ddzpbj=0.0;
				int index=0;
				while (rs.next()) {
					dhsl += rs.getInt("dhsl");
					thsl += rs.getInt("thsl");
					dhftje +=rs.getDouble("dhftje");
					thftje +=rs.getDouble("thftje");
					ddzpbj +=rs.getDouble("ddzpbj");
					index++;
				}
				final List<Map<String, Object>> condList = new ArrayList<Map<String, Object>>();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("field", "_widget_1576034485145");
				map.put("type", "text");
				map.put("method", "eq");
				map.put("value", listMap.get(i).get("_widget_1576034485145").toString());
				condList.add(map);
				Map<String, Object> filter = new HashMap<String, Object>() {
					{
						put("rel", "and");
						put("cond", condList);
					}
				};
				List<Map<String, Object>> a35 = api.getAllFormData(null, filter);
				if(a35.size()==0){
					addMap(listMap.get(i),api,dhsl,thsl,dhftje,thftje,ddzpbj,index);
				}else{
					updateMap(listMap.get(i),api,a35,dhsl,thsl,dhftje,thftje,ddzpbj,index);
				}
			}
			close(rs, pstmt, conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addMap(Map<String,Object> map,JDYAPIUtils api,int dhsl,int thsl,double dhftje,double thftje,double ddzpbj,int index){
		Map<String, Object> rawData = new HashMap<String, Object>();
		//客户全信息+门店
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("value", map.get("_widget_1577956559789"));
		rawData.put("_widget_1577956559789", m1);
		//手工单号
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("value", map.get("_widget_1576034484916"));
		rawData.put("_widget_1576034484916", m2);
		//产品编码
		Map<String, Object> m3 = new HashMap<String, Object>();
		m3.put("value", map.get("_widget_1576034484931"));
		rawData.put("_widget_1576034484931", m3);
		//产品全名
		Map<String, Object> m4 = new HashMap<String, Object>();
		m4.put("value", map.get("_widget_1576034485115"));
		rawData.put("_widget_1576034485115", m4);
		//销售备注
		Map<String, Object> m5 = new HashMap<String, Object>();
		m5.put("value", map.get("_widget_1576034485130"));
		rawData.put("_widget_1576034485130", m5);
		//销售名称
		Map<String, Object> m6 = new HashMap<String, Object>();
		m6.put("value", map.get("_widget_1578364140953"));
		rawData.put("_widget_1578364140953", m6);
		//（主键）产品+全信息+门店+手工单号+销售备注
		Map<String, Object> m7 = new HashMap<String, Object>();
		m7.put("value", map.get("_widget_1576034485145"));
		rawData.put("_widget_1576034485145", m7);
		//A3合同数量
		Map<String, Object> m8 = new HashMap<String, Object>();
		m8.put("value", 0);
		rawData.put("_widget_1577956559011", m8);
		//A3.1订货数量
		Map<String, Object> m9 = new HashMap<String, Object>();
		m9.put("value", dhsl);
		rawData.put("_widget_1577956559026", m9);
		//A3.1退货数量（填负数）
		Map<String, Object> m10 = new HashMap<String, Object>();
		m10.put("value", thsl);
		rawData.put("_widget_1577956559093", m10);
		//销售数量
		Map<String, Object> m11 = new HashMap<String, Object>();
		m11.put("value", dhsl+thsl);
		rawData.put("_widget_1576034485288", m11);
		//采购数量
		Map<String, Object> m12 = new HashMap<String, Object>();
		m12.put("value", 0);
		rawData.put("_widget_1577956557808", m12);
		//实发数量
		Map<String, Object> m13 = new HashMap<String, Object>();
		m13.put("value", 0);
		rawData.put("_widget_1576034485303", m13);
		//待发货数
		Map<String, Object> m14 = new HashMap<String, Object>();
		m14.put("value", dhsl+thsl);
		rawData.put("_widget_1576034485318", m14);
		//待采购数
		Map<String, Object> m15 = new HashMap<String, Object>();
		m15.put("value", dhsl+thsl);
		rawData.put("_widget_1577956557767", m15);
		//A3分摊金额
		Map<String, Object> m16 = new HashMap<String, Object>();
		m16.put("value", 0.0);
		rawData.put("_widget_1577956558522", m16);
		//A3.1订货分摊金额
		Map<String, Object> m17 = new HashMap<String, Object>();
		m17.put("value", dhftje);
		rawData.put("_widget_1577956558537", m17);
		//A3.1退货分摊金额
		Map<String, Object> m18 = new HashMap<String, Object>();
		m18.put("value", thftje);
		rawData.put("_widget_1577956558578", m18);
		//合同单品总金额
		Map<String, Object> m19 = new HashMap<String, Object>();
		m19.put("value", dhftje+thftje);
		rawData.put("_widget_1577956558455", m19);
		//A5实际销售金额
		Map<String, Object> m20 = new HashMap<String, Object>();
		m20.put("value", 0.0);
		rawData.put("_widget_1577956557301", m20);
		//合同单品未出总金额
		Map<String, Object> m21 = new HashMap<String, Object>();
		m21.put("value", dhftje+thftje);
		rawData.put("_widget_1576034485467", m21);
		//出库产品显示
		Map<String, Object> m22 = new HashMap<String, Object>();
		m22.put("value", dhsl+thsl>0?0:1);
		rawData.put("_widget_1576034485482", m22);
		//采购产品显示
		Map<String, Object> m23 = new HashMap<String, Object>();
		m23.put("value", dhsl+thsl>0?0:1);
		rawData.put("_widget_1577956557604", m23);
		//A3单定制品标价
		Map<String, Object> m24 = new HashMap<String, Object>();
		m24.put("value", 0.0);
		rawData.put("_widget_1576034485536", m24);
		//A3.1单定制品标价
		Map<String, Object> m25 = new HashMap<String, Object>();
		m25.put("value", ddzpbj/index);
		rawData.put("_widget_1578466166449", m25);
		//A3流程结束标记
		Map<String, Object> m26 = new HashMap<String, Object>();
		m26.put("value",0);
		rawData.put("_widget_1579242428750", m26);
		api.createData(rawData);
	}
	
	public static void updateMap(Map<String,Object> map,JDYAPIUtils api,List<Map<String, Object>> a35,int dhsl,int thsl,double dhftje,double thftje,double ddzpbj,int index){
		Map<String, Object> rawData = new HashMap<String, Object>();
		//客户全信息+门店
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("value", map.get("_widget_1577956559789"));
		rawData.put("_widget_1577956559789", m1);
		//手工单号
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("value", map.get("_widget_1576034484916"));
		rawData.put("_widget_1576034484916", m2);
		//产品编码
		Map<String, Object> m3 = new HashMap<String, Object>();
		m3.put("value", map.get("_widget_1576034484931"));
		rawData.put("_widget_1576034484931", m3);
		//产品全名
		Map<String, Object> m4 = new HashMap<String, Object>();
		m4.put("value", map.get("_widget_1576034485115"));
		rawData.put("_widget_1576034485115", m4);
		//销售备注
		Map<String, Object> m5 = new HashMap<String, Object>();
		m5.put("value", map.get("_widget_1576034485130"));
		rawData.put("_widget_1576034485130", m5);
		//销售名称
		Map<String, Object> m6 = new HashMap<String, Object>();
		m6.put("value", map.get("_widget_1578364140953"));
		rawData.put("_widget_1578364140953", m6);
		//（主键）产品+全信息+门店+手工单号+销售备注
		Map<String, Object> m7 = new HashMap<String, Object>();
		m7.put("value", map.get("_widget_1576034485145"));
		rawData.put("_widget_1576034485145", m7);
		//A3合同数量
		Map<String, Object> m8 = new HashMap<String, Object>();
		m8.put("value", a35.get(0).get("_widget_1577956559011"));
		rawData.put("_widget_1577956559011", m8);
		//A3.1订货数量
		Map<String, Object> m9 = new HashMap<String, Object>();
		m9.put("value", dhsl);
		rawData.put("_widget_1577956559026", m9);
		//A3.1退货数量（填负数）
		Map<String, Object> m10 = new HashMap<String, Object>();
		m10.put("value", thsl);
		rawData.put("_widget_1577956559093", m10);
		//销售数量
		Map<String, Object> m11 = new HashMap<String, Object>();
		m11.put("value", getInteger(a35.get(0).get("_widget_1577956559011"))+dhsl+thsl);
		rawData.put("_widget_1576034485288", m11);
		//采购数量
		Map<String, Object> m12 = new HashMap<String, Object>();
		m12.put("value", a35.get(0).get("_widget_1577956557808"));
		rawData.put("_widget_1577956557808", m12);
		//实发数量
		Map<String, Object> m13 = new HashMap<String, Object>();
		m13.put("value", a35.get(0).get("_widget_1576034485303"));
		rawData.put("_widget_1576034485303", m13);
		//待发货数
		Map<String, Object> m14 = new HashMap<String, Object>();
		m14.put("value", getInteger(a35.get(0).get("_widget_1577956559011"))+dhsl+thsl-getInteger(a35.get(0).get("_widget_1576034485303")));
		rawData.put("_widget_1576034485318", m14);
		//待采购数
		Map<String, Object> m15 = new HashMap<String, Object>();
		m15.put("value", getInteger(a35.get(0).get("_widget_1577956559011"))+dhsl+thsl-getInteger(a35.get(0).get("_widget_1577956557808"))-getInteger(a35.get(0).get("_widget_1576034485303")));
		rawData.put("_widget_1577956557767", m15);
		//A3分摊金额
		Map<String, Object> m16 = new HashMap<String, Object>();
		m16.put("value", a35.get(0).get("_widget_1577956558522"));
		rawData.put("_widget_1577956558522", m16);
		//A3.1订货分摊金额
		Map<String, Object> m17 = new HashMap<String, Object>();
		m17.put("value", dhftje);
		rawData.put("_widget_1577956558537", m17);
		//A3.1退货分摊金额
		Map<String, Object> m18 = new HashMap<String, Object>();
		m18.put("value", thftje);
		rawData.put("_widget_1577956558578", m18);
		//合同单品总金额
		Map<String, Object> m19 = new HashMap<String, Object>();
		m19.put("value", getDouble(a35.get(0).get("_widget_1577956558522"))+dhftje+thftje);
		rawData.put("_widget_1577956558455", m19);
		//A5实际销售金额
		Map<String, Object> m20 = new HashMap<String, Object>();
		m20.put("value", a35.get(0).get("_widget_1577956557301"));
		rawData.put("_widget_1577956557301", m20);
		//合同单品未出总金额
		Map<String, Object> m21 = new HashMap<String, Object>();
		m21.put("value", getDouble(a35.get(0).get("_widget_1577956558522"))+dhftje+thftje-getDouble(a35.get(0).get("_widget_1577956557301")));
		rawData.put("_widget_1576034485467", m21);
		//出库产品显示
		Map<String, Object> m22 = new HashMap<String, Object>();
		m22.put("value", getInteger(a35.get(0).get("_widget_1577956559011"))+dhsl+thsl-getInteger(a35.get(0).get("_widget_1576034485303"))>0?0:1);
		rawData.put("_widget_1576034485482", m22);
		//采购产品显示
		Map<String, Object> m23 = new HashMap<String, Object>();
		m23.put("value", getInteger(a35.get(0).get("_widget_1577956559011"))+dhsl+thsl-getInteger(a35.get(0).get("_widget_1577956557808"))-getInteger(a35.get(0).get("_widget_1576034485303"))>0?0:1);
		rawData.put("_widget_1577956557604", m23);
		//A3单定制品标价
		Map<String, Object> m24 = new HashMap<String, Object>();
		m24.put("value", a35.get(0).get("_widget_1576034485536"));
		rawData.put("_widget_1576034485536", m24);
		//A3.1单定制品标价
		Map<String, Object> m25 = new HashMap<String, Object>();
		m25.put("value", ddzpbj/index);
		rawData.put("_widget_1578466166449", m25);
		//A3流程结束标记
		Map<String, Object> m26 = new HashMap<String, Object>();
		m26.put("value",a35.get(0).get("_widget_1579242428750"));
		rawData.put("_widget_1579242428750", m26);
		api.updateData(a35.get(0).get("_id").toString(), rawData);
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
			Map<String, Object> map=new HashMap<String, Object>();
			String qxx=((Map)lists.get(i)).get("_widget_1558495976018")+"-"+((Map)lists.get(i)).get("_widget_1559206739451");
			map.put("_widget_1577956559789", qxx.replace("𡋾", "别"));//客户全信息+门店
			map.put("_widget_1576034484916", ((Map)lists.get(i)).get("_widget_1551859128121"));//手工单号
			map.put("_widget_1576034484931", ((Map)lists.get(i)).get("_widget_1546527150734"));//产品编码
			map.put("_widget_1576034485115", ((Map)lists.get(i)).get("_widget_1546132677648"));//产品全名
			map.put("_widget_1576034485130", ((Map)lists.get(i)).get("_widget_1551671731631"));//销售备注
			map.put("_widget_1578364140953", ((Map)lists.get(i)).get("_widget_1558852228099"));//销售名称
			String zj=getString(((Map)lists.get(i)).get("_widget_1546132677648"))+((Map)lists.get(i)).get("_widget_1558495976018")+"-"+((Map)lists.get(i)).get("_widget_1559206739451")+getString(((Map)lists.get(i)).get("_widget_1551859128121"))+getString(((Map)lists.get(i)).get("_widget_1551671731631"));
			map.put("_widget_1576034485145", zj.replace("𡋾", "别"));//（主键）产品+全信息+门店+手工单号+销售备注
			map.put("_widget_1577956559026", ((Map)lists.get(i)).get("_widget_1546140576740"));//A3.1订货数量
			map.put("_widget_1577956559093", ((Map)lists.get(i)).get("_widget_1564381839630"));//A3.1退货数量（填负数）
			map.put("_widget_1577956558537", ((Map)lists.get(i)).get("_widget_1572746817579"));//A3.1订货分摊金额
			map.put("_widget_1577956558578", ((Map)lists.get(i)).get("_widget_1571802785809"));//A3.1退货分摊金额
			map.put("_widget_1578466166449", ((Map)lists.get(i)).get("_widget_1558423889365"));//A3.1单定制品标价
			arrayLists.add(map);
		}
		return arrayLists;
	}
	
	public static Integer getInteger(Object obj){
		if(obj==null || "".equals(obj.toString())){
			return 0;
		}
		return Integer.parseInt(obj.toString());
	}
	
	public static Double getDouble(Object obj){
		if(obj==null || "".equals(obj.toString())){
			return 0.0;
		}
		return Double.parseDouble(obj.toString());
	}
	
	public static String getString(Object obj){
		if(obj==null){
			return "";
		}
		return obj.toString();
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
