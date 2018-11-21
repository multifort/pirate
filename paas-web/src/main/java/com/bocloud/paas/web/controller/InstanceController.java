package com.bocloud.paas.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.MapTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

@RestController
@RequestMapping(value = "/instance")
public class InstanceController {

	private final String BASE_SERVICE = "/instance";
	@Autowired
	private ServiceFactory serviceFactory;
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	
	/**
	 * 获取组件pod资源
	 * 
	 * @param params
	 * @param appId
	 * @param resourceName
	 * @param namespace
	 * @param resourceType
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getPodResource", method = { RequestMethod.POST })
	public BsmResult getPodResource(@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = "appId", required = true) String appId,
			@RequestParam(value = "resourceName", required = true) String resourceName,
			@RequestParam(value = "namespace", required = true) String namespace,
			@RequestParam(value = "resourceType", required = true) String resourceType, HttpServletRequest request) {
		String url = BASE_SERVICE + "/getPodResource";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		paramMap.put("resourceName", resourceName);
		paramMap.put("namespace", namespace);
		paramMap.put("resourceType", resourceType);
		paramMap.put("appId", appId);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap, request);
		return service.invoke();
	}
	
	/**
	 * 获取pod状态
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getPodStatus", method = { RequestMethod.POST })
	public BsmResult getPodStatus(@RequestParam(value = Common.PARAMS, required = true) String params,
			@RequestParam(value = "appId", required = true) Long appId,
			@RequestParam(value = "namespace", required = true) String namespace, HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/getPodStatus";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			paramMap.put("appId", appId);
			paramMap.put("namespace", namespace);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 获取日志信息
	 * 
	 * @param params
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/log", method = { RequestMethod.GET })
	public BsmResult getLog(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/log";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
}
