package com.bocloud.paas.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.JenkinsCredential;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.environment.JenkinsCredentialService;

@RestController
@RequestMapping("/jenkinsCredential")
public class JenkinsCredentialController {

	@Autowired
	private JenkinsCredentialService jenkinsCredentialService;

	/**
	 * 创建jenkins凭证
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "创建jenkins凭证")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			JenkinsCredential jenkinsCredential = JSONObject.parseObject(object.toJSONString(),
					JenkinsCredential.class);
			BsmResult result = jenkinsCredentialService.create(jenkinsCredential, user.getId());
			return result;
		}
		return ResultTools.formatErrResult();

	}

	/**
	 * 修改jenkins凭证
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "修改jenkins凭证")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			JenkinsCredential jenkinsCredential = JSONObject.parseObject(object.toJSONString(),
					JenkinsCredential.class);
			BsmResult result = jenkinsCredentialService.modify(jenkinsCredential, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除jenkins凭证
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	@Log(name = "删除jenkins凭证")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Long> ids = JSON.parseArray(jsonObject.get("id").toString(), Long.class);
		return jenkinsCredentialService.remove(ids, user.getId());
	}

	/**
	 * 查询jenkins凭证
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name = "查询jenkins凭证列表")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		BsmResult result = jenkinsCredentialService.list(page, rows, paramList, sorterMap, simple);
		return result;
	}

	/**
	 * 按照凭证id查询jenkins凭证
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/queryByCredentialId", method = { RequestMethod.GET })
	@Log(name = "按照凭证id查询jenkins凭证")
	public BsmResult queryByCredentialId(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String credentialId = jsonObject.getString("credentialId");
			return jenkinsCredentialService.queryByCredentialId(credentialId);
		}
		return ResultTools.formatErrResult();
	}

	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	@Log(name = "jenkins凭证详情")
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = false) String params,
			@Value(Common.REQ_USER) RequestUser user) {

		JSONObject jsonObject = JSONTools.isJSONObj(params);

		if (null != jsonObject) {
			Long id = Long.parseLong(jsonObject.get("id").toString());
			if (null != jsonObject.get("id")) {
				return jenkinsCredentialService.detail(id);
			}
		}
		return ResultTools.formatErrResult();
	}

	@RequestMapping(value = "/queryCredentialId", method = { RequestMethod.GET })
	@Log(name = "查询jenkins凭证id")
	public BsmResult queryCredentialId(@RequestParam(required = true) String username,
			@RequestParam(required = false) String password, @Value(Common.REQ_USER) RequestUser user) {
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			return jenkinsCredentialService.queryCredentialId(username, password, user.getId());
		}
		return ResultTools.formatErrResult();
	}

}
