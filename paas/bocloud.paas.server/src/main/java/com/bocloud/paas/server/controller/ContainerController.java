package com.bocloud.paas.server.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.application.ContainerService;

/**
 * @author Zaney
 * @data:2017年3月9日
 * @describe:页面数据统计控制层
 */
@RestController
@RequestMapping("/container")
public class ContainerController {

	@Autowired
	private ContainerService containerService;

	/**
	 * 获取数据统计
	 *
	 * @return
	 */
	@RequestMapping(value = "/total", method = { RequestMethod.GET })
	@Log(name = "数据统计")
	public BsmResult total(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONObject.parseObject(params);
		if (null != object) {
			return containerService.total(object.getLong("id"));
		}
		return ResultTools.formatErrResult();
	}

	/**
	 * 获取资源列表
	 *
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name = "资源列表展示")
	public BsmResult listResource(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONObject.parseObject(params);
		if (null != object) {
			if (object.containsKey("appId")) {
				Map<String, String> labels = JSONObject.parseObject(object.getString("labels"), Map.class);
				return containerService.list(labels, Long.valueOf(object.getString("appId")));
			} else {
				return containerService.list(object.getString("resourceType"), 
						object.getString("resourceName"), Long.valueOf(object.getString("envId")), 
						object.getString("namespace"));
			}
		}
		return ResultTools.formatErrResult();
	}

	/**
	 * 获取节点下的pod
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/node/list", method = { RequestMethod.POST })
	@Log(name = "获取节点下的运行实例")
	public BsmResult listNodePod(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = true) String params) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		String nodeIp = null;
		Long envId = null;
		if (!ListTool.isEmpty(paramList)) {
			nodeIp = (String) paramList.get(0).getParam().get("ip");
			Object clusterIdObject = paramList.get(0).getParam().get("id");
			if (null != clusterIdObject && StringUtils.isNotBlank(clusterIdObject.toString())) {
				envId = Long.parseLong(paramList.get(0).getParam().get("id").toString());
			}
			return containerService.getNodePod(envId, nodeIp);
		}
		return ResultTools.formatErrResult();
	}

	/**
	 * 获取资源详情
	 *
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.POST })
	@Log(name = "资源详情")
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONObject.parseObject(params);
		if (null != object) {
			if (object.containsKey("appId")) {
				return containerService.detail(object.getString("resourceName"), object.getLong("appId"), object.getString("resourceType"));
			}
			return containerService.detail(object.getString("resourceType"), object.getString("namespace"),
					object.getString("resourceName"), object.getLong("id"));
		}
		return ResultTools.formatErrResult();
	}

	/**
	 * 获取资源事件
	 */
	@RequestMapping(value = "/event", method = { RequestMethod.POST })
	@Log(name = "资源事件")
	public BsmResult event(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONObject.parseObject(params);
		if (null != object) {
			if (object.containsKey("appId")) {
				return containerService.event(object.getString("resourceName"), object.getLong("appId"));
			}
			return containerService.event(object.getString("namespace"),
					object.getString("resourceName"), object.getLong("id"));
		}
		return ResultTools.formatErrResult();
	}

	/**
	 * 获取日志信息
	 * 
	 * @param params
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/log", method = { RequestMethod.GET })
	@Log(name = "日志信息")
	public BsmResult getLog(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONObject.parseObject(params);
		if (null != object) {
			if (object.containsKey("appId")) {
				return containerService.getLog(object.getString("resourceName"), object.getString("containerName"), 
						object.getString("status"), object.getLong("appId"), object.getInteger("line"));
			}
			return containerService.getLog(object.getString("namespace"), object.getString("resourceName"),
					object.getString("containerName"), object.getString("status"), object.getLong("id"), object.getInteger("line"));
		}
		return ResultTools.formatErrResult();
	}
	/**
	 * 获取资源yaml文件模板
	 * 
	 * @param params
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/template", method = { RequestMethod.GET })
	@Log(name = "模板信息")
	public BsmResult template(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONObject.parseObject(params);
		if (null != object) {
			return containerService.template(Long.valueOf(object.getString("applicationId")), 
					object.getString("name"));
		}
		return ResultTools.formatErrResult();
	}

}
