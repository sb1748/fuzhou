package com.push.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


public class HttpUtil {
	
	
    /**
	 * GET---无参测试
	 *
	 * @date 2018年7月13日 下午4:18:50
	 */
	public void doGet() {
		// 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		// 创建Get请求
		HttpGet httpGet = new HttpGet("http://47.104.1.200:8082/jxszj_zhmd/jxszj/getKhlbList?page=1&rows=30");
 
		// 响应模型
		CloseableHttpResponse response = null;
		try {
			// 由客户端执行(发送)Get请求
			response = httpClient.execute(httpGet);
			// 从响应模型中获取响应实体
			HttpEntity responseEntity = response.getEntity();
			System.out.println("响应状态为:" + response.getStatusLine());
			if (responseEntity != null) {
				System.out.println("响应内容长度为:" + responseEntity.getContentLength());
				System.out.println("响应内容为:" + EntityUtils.toString(responseEntity));
			}
		}  catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 释放资源
				if (httpClient != null) {
					httpClient.close();
				}
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	/**
	 * POST---有参测试(对象参数)
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws UnsupportedEncodingException 
	 *
	 * @date 2018年7月13日 下午4:18:50
	 */
	public String doPost(String url,Object obj,String token) throws ClientProtocolException, IOException{
 
		// 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		// 创建Post请求
		HttpPost httpPost = new HttpPost(url);
		// 我这里利用阿里的fastjson，将Object转换为json字符串;
		// (需要导入com.alibaba.fastjson.JSON包)
		String jsonString = JSON.toJSONString(obj);
 
		StringEntity entity = new StringEntity(jsonString, "UTF-8");
 
		// post请求是将参数放在请求体里面传过去的;这里将entity放入post请求体中
		httpPost.setEntity(entity);
		httpPost.setHeader("Content-Type", "application/json;charset=utf8");
		httpPost.setHeader("token", token);
		//响应模型     
		CloseableHttpResponse response=null;
		String jsonData="";
		try {
			//由客户端执行(发送)Post请求
			response = httpClient.execute(httpPost);
			// 从响应模型中获取响应实体
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {
				jsonData=EntityUtils.toString(responseEntity);
			}
		} finally {
			// 释放资源
			if (httpClient != null) {
				httpClient.close();
			}
			if (response != null) {
				response.close();
			}
		}
		return jsonData;
	}

	
//	public static void main(String[] args) {
//		try {
//			Map<String, Object> loginMap=new HashMap<String,Object>();
//			loginMap.put("username", "13896208818");
//			loginMap.put("pwd", "20190808");
//			String response=new HttpUtil().doPost("https://gis.canc.com.cn:9444/crmapi/sys/userAuth/login",loginMap,null);
//			System.out.println(response);
//			if(!("".equals(response))){
//				JSONObject jsonObject=JSONObject.parseObject(response);
//				if("0".equals(jsonObject.get("code").toString()) || "200".equals(jsonObject.get("code").toString())){
//					String token=jsonObject.get("token").toString();
//					Map<String, Object> orderMap = new HashMap<String, Object>();
//					orderMap.put("cusId", "2b5fae85-10b2-4cb1-aa53-a9c1e37a474e");
//					orderMap.put("sumTotalMoney", "1000");
//					orderMap.put("officeName", "二郎居然金牌厨柜店");
//					String response1 = new HttpUtil().doPost("https://gis.canc.com.cn:9444/crmapi/sys/userAuth/crm/pushContract",orderMap, token);
//					System.out.println(response1);
//					JSONObject jsonObject1 = JSONObject.parseObject(response1);
//					System.out.println(response1);
//				}
//			}
//		} catch (Exception e) {
//			System.out.println("-----------"+e.getMessage());
//			e.printStackTrace();
//		}
//	}
}
