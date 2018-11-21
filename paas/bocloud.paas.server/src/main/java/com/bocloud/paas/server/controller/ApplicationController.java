package com.bocloud.paas.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Monitor;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.application.ApplicationService;

import io.fabric8.kubernetes.api.model.Quantity;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {
	@Autowired
	private ApplicationService applicationService;

	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name="应用创建")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Application application = JSONObject.parseObject(object.toJSONString(), Application.class);
			return applicationService.create(user, application);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name="应用列表查询")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return applicationService.list(page, rows, paramList, sorterMap, simple, user);
	}
	
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name="应用信息修改")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Application application = JSONObject.parseObject(object.toJSONString(), Application.class);
			return applicationService.modify(application, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	@Log(name="应用详情")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return applicationService.detail(id);
	}

	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	@Log(name="删除应用")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Long> ids = JSON.parseArray(jsonObject.get("ids").toString(), Long.class);
		return applicationService.remove(ids);
	}
	/**
	 * 监控
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/monitor", method = { RequestMethod.GET })
	@Log(name="获取监控数据")
	public BsmResult kubernetesMonitor(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Monitor monitor = JSONObject.parseObject(object.toJSONString(), Monitor.class);
			return applicationService.monitor(monitor);
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 创建ingress
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/ingress", method = { RequestMethod.POST })
	@Log(name="服务暴露")
	public BsmResult createIngress(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONTools.isJSONObj(params);
		Long applicationId = object.getLong("applicationId");
		String serviceName = object.getString("serviceName");
		int servicePort = object.getIntValue("port");
		return applicationService.createIngress(applicationId, serviceName, servicePort);
	}
	
	/**
	 * 获取所有已暴露的服务
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/ingress", method = { RequestMethod.GET })
	@Log(name="获取所有已暴露的服务")
	public BsmResult getIngress(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONTools.isJSONObj(params);
		Long applicationId = object.getLong("applicationId");
		return applicationService.getIngress(applicationId);
	}
	
	/**
	 * 获取服务端口
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/service/port", method = { RequestMethod.GET })
	@Log(name="获取服务端口")
	public BsmResult ingress(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONTools.isJSONObj(params);
		Long applicationId = object.getLong("applicationId");
		String serviceName = object.getString("serviceName");
		String type = object.getString("type");
		return applicationService.getServicePort(applicationId, type, serviceName);
	}
	
	@RequestMapping(value = "/resource", method = { RequestMethod.GET })
	@Log(name="获取应用资源")
	public BsmResult getResource(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject object = JSONTools.isJSONObj(params);
		Long applicationId = object.getLong("applicationId");
		return applicationService.getResource(applicationId);
	}
	
	@RequestMapping(value = "/topology", method = { RequestMethod.GET })
	@Log(name="获取应用拓扑图信息")
	public BsmResult applicationTopology(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		Long applicationId = object.getLong("applicationId");
		return applicationService.applicationTopology(applicationId);
	}
	
	@RequestMapping(value = "/ingress", method = { RequestMethod.DELETE })
	@Log(name="删除服务暴露出的路径")
	public BsmResult deleteServiceIngress(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		Long applicationId = object.getLong("applicationId");
		String svcName = object.getString("svcName");
		return applicationService.deleteServiceIngress(applicationId, svcName);
	}


	/**
	 * 获取饼状图数据信息
	 * @param user
	 * @return
	 */
	/*@RequestMapping(value = "/data", method = { RequestMethod.GET })
	public BsmResult data(@Value(Common.REQ_USER) RequestUser user){
		return applicationService.getData(user);
	}*/
	
	// 应用实例模板化，暂时放在这儿，以后移动到应用实例Controller
	@RequestMapping(value = "/templatable", method = { RequestMethod.POST })
	@Log(name = "应用实例转换成模板")
	public BsmResult templatable(@RequestParam(value = Common.PARAMS, required = false) String params, @Value(Common.REQ_USER) RequestUser user) {
		 JSONObject jsonObject = JSONTools.isJSONObj(params);
		 Long id = null;
		 String namespace = null;
		 String name = null;
		 if(null != jsonObject){
			  id = Long.valueOf(jsonObject.get("id").toString());
			  namespace = jsonObject.getString("namespace");
			  name = jsonObject.getString("name");
		 }
		return applicationService.templatable(id,namespace,name,user.getId());
	}
	
	//===========================  资源配额 ======================================//
	@RequestMapping(value = "/quota", method = {RequestMethod.POST})
	@Log(name = "创建资源配额对象")
	public BsmResult createResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		JSONObject hardObject = jsonObject.getJSONObject("hard");
		JSONObject limitRangeObject = jsonObject.getJSONObject("limitRange");
		return applicationService.createResourceQuota(applicationId, hardObject, limitRangeObject);
	}
	
	@RequestMapping(value = "/quota", method = {RequestMethod.GET})
	@Log(name = "获取资源配额对象信息")
	public BsmResult detailResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		return applicationService.detailResourceQuota(applicationId);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/quota", method = {RequestMethod.POST})
	@Log(name = "修改资源配额对象")
	public BsmResult modifyResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		Map<String, Quantity> hardMap = JSON.parseObject(jsonObject.get("hard").toString(), Map.class);
		JSONObject limitRangeObject = jsonObject.getJSONObject("limitRange");
		return applicationService.modifyResourceQuota(applicationId, hardMap, limitRangeObject);
	}
	
	@RequestMapping(value = "/quota/used", method = {RequestMethod.GET})
	@Log(name = "获取被使用的资源配额")
	public BsmResult getUsedResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		return applicationService.getUsedResourceQuota(applicationId);
	}
	
	@RequestMapping(value = "/quota", method = {RequestMethod.DELETE})
	@Log(name = "删除资源配额")
	public BsmResult deleteResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		return applicationService.deleteResourceQuota(applicationId);
	}
	
}
