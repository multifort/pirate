package com.bocloud.paas.common.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bocloud.common.http.HttpClient;
import com.bocloud.common.model.Result;

public class ExtendHttpClient extends HttpClient {

	private static Logger logger = LoggerFactory.getLogger(ExtendHttpClient.class);

	/**
	 * 文件传输
	 * 
	 * @param headers
	 * @param params
	 * @param url
	 * @param entity
	 *            文件
	 * @return
	 */
	public Result post(Map<String, Object> headers, Map<String, Object> params, String url, AbstractHttpEntity entity) {
		if (StringUtils.isEmpty(url)) {
			logger.error("Url is empty!");
			return new Result(false, "Empty URL");
		}
		logger.debug("Url is{} and Param is {}", url, params);
		url = assembleURI(url, params);
		httpPost = new HttpPost(url);
		buildHeader(httpPost, headers);
		httpPost.setConfig(config);
		httpPost.setEntity(entity);
		try {
			response = httpclient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			String responseContent = IOUtils.toString(httpEntity.getContent(), "utf-8");
			int status = response.getStatusLine().getStatusCode();
			result = resultBuilder.build(status);
			if (result.isSuccess()) {
				result.setMessage(responseContent);
			} else {
				logger.warn(responseContent);
			}
		} catch (IOException e) {
			logger.error("HttpClient IOException:", e);
			result = new Result(false, e.getMessage());
		} finally {
			close();
		}
		return result;
	}
	
	/**
	 * 带User-Agent的get请求
	 * 
	 * @param headers
	 * @param params
	 * @param url
	 * @param entity
	 *            文件
	 * @return
	 */
	public Result get(Map<String, Object> headers, Map<String, Object> params, String url) {
		if (StringUtils.isEmpty(url)) {
			logger.error("url is empty!");
			return new Result(false, "Empty URL");
		}
		logger.debug("url is{} and Param is {}", url, params);
		url = assembleURI(url, params);
		httpGet = new HttpGet(url);
		buildHeader(httpGet, headers);
		httpGet.setConfig(config);
		HttpClientBuilder builder = HttpClients.custom();
		builder.setUserAgent("Mozilla/5.0(Windows;U;Windows NT 5.1;en-US;rv:0.9.4)"); 
		CloseableHttpClient httpClient = builder.build();
		try {
			response = httpClient.execute(httpGet);
			processResp();
		} catch (IOException e) {
			logger.error("HttpClient IOException:", e);
			result = new Result(false, e.getMessage());
		} finally {
			close();
		}
		return result;
	}

	public Result put(Map<String, Object> headers, Map<String, Object> params, String url, AbstractHttpEntity entity) {
		if (StringUtils.isEmpty(url)) {
			logger.error("Url is empty!");
			return new Result(false, "Empty URL");
		}
		logger.debug("Url is{} and Param is {}", url, params);
		url = assembleURI(url, params);
		HttpPut httpPut = new HttpPut(url);
		buildHeader(httpPut, headers);
		httpPut.setConfig(config);
		httpPut.setEntity(entity);
		try {
			response = httpclient.execute(httpPut);
			int status = response.getStatusLine().getStatusCode();
			result = resultBuilder.build(status);
			if (result.isSuccess()) {
				result.setMessage("success");
			} else {
				logger.warn("http request failed");
			}
		} catch (IOException e) {
			logger.error("HttpClient IOException:", e);
			result = new Result(false, e.getMessage());
		} finally {
			close();
		}
		return result;
	}

	/**
	 * 带用户名密码的post
	 * 
	 * @param headers
	 * @param params
	 * @param uri
	 * @param username
	 * @param password
	 * @return
	 */
	public Result post(Map<String, Object> headers, Map<String, Object> params, URI uri, String username,
			String password) {
		Result result = new Result();
		// 设置账户密码
		HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(username, password));

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(targetHost, basicAuth);

		// Add AuthCache to the execution context
		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);
		
		HttpPost httpPost = new HttpPost(uri);
		if (null != params) {
			httpPost = (HttpPost) assembleURI(httpPost, params);
		}
		if (null != headers) {
			buildHeader(httpPost, headers);
		}
		try {
			CloseableHttpResponse response = httpclient.execute(httpPost, context);
			HttpEntity entity = response.getEntity();
			if (entity.getContentLength() > 0) {
				String responseContent = IOUtils.toString(entity.getContent(), "utf-8");
				result.setMessage(responseContent);
				logger.error(responseContent);
			} else {
				result.setSuccess(true);
				logger.info("创建jenkins凭证成功！");
			}
		} catch (Exception e) {
			logger.error("exec [" + uri + "] fail!", e);
			result.setMessage("执行[" + uri + "]请求异常！");
		}
		return result;
	}
	
	public HttpEntityEnclosingRequestBase assembleURI(HttpEntityEnclosingRequestBase http, Map<String, Object> params) {
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		for (String key : params.keySet()) {
			Object object = params.get(key);
			if (null == object) {
				continue;
			}
			parameters.add(new BasicNameValuePair(key, object.toString()));
		}
		try {
			http.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("[" + http.getURI() + "] assemble fail:\n", e);
		}
		return http;
	}
	
	private void processResp() throws UnsupportedOperationException, IOException {
		HttpEntity entity = response.getEntity();
		String content = IOUtils.toString(entity.getContent(), "utf-8");
		int status = response.getStatusLine().getStatusCode();
		result = resultBuilder.build(status);
		if (result.isSuccess()) {
			result.setMessage(content);
		} else {
			logger.warn(content);
		}
	}
}
