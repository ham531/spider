package com.miexam.spider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpUtil {
	private static Logger logger = Logger.getLogger(HttpUtil.class);
	private static final int SOCKET_TIME_OUT = 60000; // 设置读取超时
	private static final int CONNECT_TIME_OUT = 60000; // 设置连接超时

	/**
	 * 构建唯一会话Id
	 * 
	 * @return
	 */
	public static String getSessionId() {
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		return str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23)
				+ str.substring(24);
	}

	public static String doPost(String url, Map<String, String> params) {
		String returnString = "";
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpEntity entity = new UrlEncodedFormEntity(createParam(params), Consts.UTF_8);
		try {
			HttpPost httpPost = new HttpPost(url);

			httpPost.setConfig(getRequestConfig());

			httpPost.setHeader("Accept", "application/xml, text/xml, */*; q=0.01");
			httpPost.setHeader("Accept-Encoding", "gzip, deflate");
			httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
			httpPost.setHeader("Connection", "keep-alive");
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			httpPost.setHeader("SessionId", getSessionId());
			// httpPost.setHeader("Host", "www.mof.gov.cn");
			// httpPost.setHeader("Origin", "http://www.mof.gov.cn");
			// httpPost.setHeader("Referer",
			// "http://www.mof.gov.cn/was5/web/czb/wassearch.jsp");
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
			httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
			httpPost.setEntity(entity);

			response = client.execute(httpPost);
			returnString = EntityUtils.toString(response.getEntity(), "UTF-8");

			EntityUtils.consume(response.getEntity()); // 关闭请求
			return returnString;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnString;
	}

	/**
	 * 设置请求的参数值
	 * 
	 * @return
	 */
	private static RequestConfig getRequestConfig() {
		return RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIME_OUT).build();
	}

	/**
	 * 设置参数列表
	 * 
	 * @param param
	 * @return
	 */
	private static List<NameValuePair> createParam(Map<String, String> params) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (params != null) {
			for (String k : params.keySet()) {
				nvps.add(new BasicNameValuePair(k, params.get(k).toString()));
			}
		}
		return nvps;
	}
	/**
	 * post请求（用于请求json格式的参数）
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String doPost(String url, String params) throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);// 创建httpPost
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json");
		String charSet = "UTF-8";
		StringEntity entity = new StringEntity(params, charSet);
		httpPost.setEntity(entity);
		CloseableHttpResponse response = null;

		try {

			response = httpclient.execute(httpPost);
			StatusLine status = response.getStatusLine();
			int state = status.getStatusCode();
			if (state == HttpStatus.SC_OK) {
				HttpEntity responseEntity = response.getEntity();
				String jsonString = EntityUtils.toString(responseEntity);
				return jsonString;
			} else {
				logger.error("请求返回:" + state + "(" + url + ")");
			}
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
