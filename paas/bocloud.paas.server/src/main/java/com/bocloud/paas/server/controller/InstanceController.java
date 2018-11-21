package com.bocloud.paas.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.application.InstanceService;

@RestController
@RequestMapping(value = "/instance")
public class InstanceController {

	private InstanceService instanceService;
	
	@RequestMapping(value = "/getPodResource", method = { RequestMethod.POST })
	@Log(name="查看运行实例资源")
	public BsmResult getPodResource(@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = "appId", required = true) Long appId,
			@RequestParam(value = "namespace", required = true) String namespace,
			@RequestParam(value = "resourceName", required = true) String resourceName,
			@RequestParam(value = "resourceType", required = true) String resourceType) {
		if (StringUtils.isBlank(resourceName) || StringUtils.isBlank(resourceType)) {
			return ResultTools.formatErrResult();
		}
		return instanceService.getPodResource(appId, namespace, resourceName, resourceType);
	}
	
	@RequestMapping(value = "/getPodStatus", method = { RequestMethod.POST })
	@Log(name="查看运行实例的状态")
	public BsmResult getPodStatus(@RequestParam(value = Common.PARAMS, required = true) String params,
			@RequestParam(value = "appId", required = true) Long appId,
			@RequestParam(value = "namespace", required = true) String namespace,
			@RequestParam(value = "name", required = true) String name) {
		return instanceService.getPodStatus(appId, namespace, name);
	}
	
	/**
	 * 获取日志信息
	 * 
	 * @param params
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/log", method = { RequestMethod.GET })
	public BsmResult getLog(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONObject.parseObject(params);
		if (null != object) {
			// 判断 当第一次没有集群存在的时候，会存在id为空的情况
			return instanceService.getLog(object.getString("namespace"), object.getString("resourceName"),
					object.getString("containerName"), object.getString("status"), object.getLong("id"));
		}
		return ResultTools.formatErrResult();
	}
}
