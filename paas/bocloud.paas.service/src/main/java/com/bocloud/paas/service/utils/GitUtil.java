package com.bocloud.paas.service.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Result;
import com.bocloud.common.utils.MapTools;
import com.bocloud.paas.common.util.ExtendHttpClient;

public class GitUtil {
	private static Logger logger = LoggerFactory.getLogger(GitUtil.class);

	private ExtendHttpClient httpClient;
	private final String GITLAB_API_VERSION = "/api/v4";

	public GitUtil(ExtendHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public BsmResult getBranchesByGitHub(String username, String projectName) {
		BsmResult bsmResult = new BsmResult();
		List<String> branches = new LinkedList<String>();
		Result result = httpClient.get("https://api.github.com/repos/" + username + "/" + projectName + "/branches");
		if (!result.isSuccess()) {
			bsmResult.setMessage("获取project [" + projectName + "]分支信息失败, 请检查用户名和项目信息！");
			logger.error("get project [" + projectName
					+ "] branches info fail, please check the username and the project info!");
			return bsmResult;
		}
		JSONArray jsonArray = JSONArray.parseArray(result.getMessage());
		for (Object object : jsonArray) {
			JSONObject jsonObject = JSONObject.parseObject(object.toString());
			if (null != jsonObject.get("name")) {
				branches.add(jsonObject.get("name").toString());
			}
		}
		bsmResult.setSuccess(true);
		bsmResult.setData(branches);
		return bsmResult;
	}

	public BsmResult getBranchesByMaYun(String username, String projectName, String accessToken) {
		BsmResult bsmResult = new BsmResult();
		List<String> branches = new LinkedList<String>();
		String url = "http://git.oschina.net/api/v5/repos/" + username + "/" + projectName + "/branches?access_token=" + accessToken;
		Result result =  httpClient.get(null, null, url);
		if (!result.isSuccess()) {
			bsmResult.setMessage("获取[" + url + "] project [" + projectName + "]分支信息失败, 请检查信息！");
			logger.error("get [" + url + "] project [" + projectName + "] branches info fail, please check the info!");
			return bsmResult;
		}
		JSONArray jsonArray = JSONArray.parseArray(result.getMessage());
		for (Object object : jsonArray) {
			JSONObject jsonObject = JSONObject.parseObject(object.toString());
			if (null != jsonObject.get("name")) {
				branches.add(jsonObject.get("name").toString());
			}
		}
		bsmResult.setSuccess(true);
		bsmResult.setData(branches);
		return bsmResult;
	}

	private BsmResult getGitLabPrivateToken(String url, String username, String password) {
		BsmResult bsmResult = new BsmResult();
		Map<String, Object> params = MapTools.simpleMap("login", username);
		params.put("password", password);
		Result result = httpClient.post(null, params, url + GITLAB_API_VERSION + "/session");
		if (!result.isSuccess()) {
			bsmResult.setMessage("获取[" + url + ", " + username + ", " + password + "]账户信息失败, 请检查地址和账户！");
			logger.error("get [" + url + ", " + username + ", " + password
					+ "] account info fail, please check the address and the account!");
			return bsmResult;
		}
		JSONObject jsonObject = JSONObject.parseObject(result.getMessage());
		if (null != jsonObject.get("private_token")) {
			bsmResult.setSuccess(true);
			bsmResult.setData(jsonObject.get("private_token"));
		} else {
			bsmResult.setMessage("获取[" + url + ", " + username + ", " + password + "]账户信息失败, 请检查地址和账户！");
			logger.error("get [" + url + ", " + username + ", " + password
					+ "] account info fail, please check the address and the account!");
		}
		return bsmResult;
	}

	private BsmResult getGitLabProjectId(String url, String username, String password, String projectName) {
		BsmResult bsmResult = new BsmResult();
		String privateToken = getGitLabPrivateToken(url, username, password).getData().toString();
		if (StringUtils.isBlank(privateToken)) {
			return bsmResult;
		}
		Result result = httpClient.get(url + GITLAB_API_VERSION + "/projects/" + "?private_token=" + privateToken);
		if (!result.isSuccess()) {
			bsmResult.setMessage("获取[" + url + "] project [" + projectName + "]项目信息失败, 请检查项目信息！");
			logger.error("get [" + url + "] project [" + projectName
					+ "] project info fail, please check the project info!");
			return bsmResult;
		}
		JSONArray jsonArray = JSONArray.parseArray(result.getMessage());
		for (Object object : jsonArray) {
			JSONObject jsonObject = JSONObject.parseObject(object.toString());
			if (null != jsonObject.get("name") && projectName.equals(jsonObject.get("name"))) {
				if (null != jsonObject.get("id")) {
					bsmResult.setSuccess(true);
					bsmResult.setData(jsonObject.get("id"));
				} else {
					bsmResult.setMessage("获取[" + url + "] project [" + projectName + "]项目信息失败, 请检查git项目！");
					logger.error("get [" + url + "] project [" + projectName
							+ "] project info fail, please check the git project!");
				}
			}
		}
		return bsmResult;
	}

	public BsmResult getBranchesByGitLab(String url, String username, String password, String projectName) {
		BsmResult bsmResult = new BsmResult();
		List<String> branches = new LinkedList<String>();
		String privateToken = getGitLabPrivateToken(url, username, password).getData().toString();
		if (StringUtils.isBlank(privateToken)) {
			return bsmResult;
		}
		String projectId = getGitLabProjectId(url, username, password, projectName).getData().toString();
		if (StringUtils.isBlank(projectId)) {
			return bsmResult;
		}
		Result result = httpClient.get(url + GITLAB_API_VERSION + "/projects/" + projectId
				+ "/repository/branches?private_token=" + privateToken);
		if (!result.isSuccess()) {
			bsmResult.setMessage("获取[" + url + "] project [" + projectName + "]分支信息失败, 请检查信息！");
			logger.error("get [" + url + "] project [" + projectName + "] branches info fail, please check the info!");
			return bsmResult;
		}
		JSONArray jsonArray = JSONArray.parseArray(result.getMessage());
		for (Object object : jsonArray) {
			JSONObject jsonObject = JSONObject.parseObject(object.toString());
			if (null != jsonObject.get("name")) {
				branches.add(jsonObject.get("name").toString());
			}
		}
		bsmResult.setSuccess(true);
		bsmResult.setData(branches);
		return bsmResult;
	}

	public BsmResult getBranches(String url, String username, String password, String projectName) {
		BsmResult bsmResult = new BsmResult();
		if (url.contains("://github.com")) {
			bsmResult = getBranchesByGitHub(username, projectName);
		} else {
			if (StringUtils.isBlank(password)) {
				bsmResult.setMessage("gitlab类型项目需要填写密码！");
				return bsmResult;
			}
			bsmResult = getBranchesByGitLab(url, username, password, projectName);
		}
		return bsmResult;
	}

}
