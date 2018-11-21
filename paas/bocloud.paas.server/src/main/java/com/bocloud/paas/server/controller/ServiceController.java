package com.bocloud.paas.server.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.paas.entity.ServiceAlarm;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.application.ContainerService;
import com.bocloud.paas.service.application.Service;
import com.bocloud.paas.service.repository.ImageService;

import io.fabric8.kubernetes.api.model.Quantity;

/**
 * 
 * @author caidongqing
 * @date 2017年7月17日
 */
@RestController
@RequestMapping("/service")
public class ServiceController {
	@Autowired
	private Service service;
	@Autowired
	private ContainerService containerService;
	@Autowired
	private ImageService imageService;

	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name = "服务列表查询")
	public BsmResult list(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		return service.list(applicationId);
	}

	@RequestMapping(value = "/detail", method = { RequestMethod.POST })
	@Log(name = "服务详情")
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		String serviceName = jsonObject.getString("name");
		return service.detail(applicationId, serviceName);
	}

	@RequestMapping(value = "/existed", method = { RequestMethod.POST })
	@Log(name = "校验服务是否存在")
	public BsmResult existed(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		String serviceName = jsonObject.getString("name");
		boolean existed = service.existed(applicationId, serviceName);
		if (true == existed) {
			return new BsmResult(false, "服务已存在, 请更换服务名称");
		} else {
			return new BsmResult(true, "服务不存在");
		}
	}

	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	@Log(name = "服务删除")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		List<String> serviceNames = JSON.parseArray(jsonObject.getString("name"), String.class);
		return service.remove(applicationId, serviceNames);
	}

	@RequestMapping(value = "/deploy", method = { RequestMethod.POST })
	@Log(name = "服务部署")
	public BsmResult deploy(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		if (jsonObject.containsKey("layoutId")) {
			Long layoutId = jsonObject.getLong("layoutId");
			return service.deploy(applicationId, layoutId);
		} else {
			return service.deploy(applicationId, jsonObject.get("deployment"));
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/scale", method = { RequestMethod.POST })
	@Log(name = "服务扩展")
	public BsmResult scale(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		String name = jsonObject.getString("name");
		if (jsonObject.containsKey("number")) {
			Integer number = jsonObject.getInteger("number");
			return service.scale(applicationId, name, number, user.getId());
		} else {
			Map<String, Quantity> limits = JSON.parseObject(jsonObject.get("limits").toString(), Map.class);
			Map<String, Quantity> request = JSON.parseObject(jsonObject.get("requests").toString(), Map.class);
			return service.scaleResource(applicationId, name, limits, request, user.getId());
		}
	}
	
	@RequestMapping(value = "/resource", method = { RequestMethod.POST })
	@Log(name="查看运行实例资源")
	public BsmResult getPodResource(@RequestParam(value = Common.PARAMS, required = false) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long id = jsonObject.getLong("id");
		String name = jsonObject.getString("name");
		return service.resource(id, name);
	}

	@RequestMapping(value = "/rolling", method = { RequestMethod.POST })
	@Log(name = "滚动升级")
	public BsmResult rolling(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		String serviceName = jsonObject.getString("name");
		String imageName = jsonObject.getString("image");
		return service.rolling(applicationId, serviceName, imageName, user.getId());
	}
	
	@RequestMapping(value = "/back", method = { RequestMethod.POST })
	@Log(name = "服务版本回滚")
	public BsmResult rollBack(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		String serviceName = jsonObject.getString("name");
		String paramInfo = jsonObject.getString("paramInfo");
		return service.rollBack(applicationId, serviceName, paramInfo, user.getId());
	}
	
	@RequestMapping(value = "/gplot", method = { RequestMethod.GET })
	@Log(name = "拓扑图")
	public BsmResult gplot(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		return service.gplot(jsonObject.getString("currentName"), jsonObject.getString("currentNamespace"));
	}
	
	@SuppressWarnings({ "unchecked", "static-access" })
	@RequestMapping(value = "/host", method = { RequestMethod.GET })
	@Log(name = "查询服务所在主机列表")
	public BsmResult hostList(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Map<String, String> labels = jsonObject.parseObject(jsonObject.getString("labels"), Map.class);
		return containerService.hostList(jsonObject.getLong("applicationId"), labels);
	}
	
	@RequestMapping(value = "/hpa", method = { RequestMethod.GET })
	@Log(name = "获取hpa信息")
	public BsmResult getHpa(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		return service.hpa(jsonObject.getLong("applicationId"), jsonObject.getString("name"));
	}
	
	@RequestMapping(value = "/hpa", method = { RequestMethod.POST })
	@Log(name = "设置弹性伸缩")
	public BsmResult createHpa(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		JSONObject object = JSONObject.parseObject(jsonObject.getString("hpa"));
		String name = jsonObject.getString("name");
		return service.createHpa(jsonObject.getLong("applicationId"), name, object, user.getId());
	}
	
	@RequestMapping(value = "/hpa", method = { RequestMethod.DELETE })
	@Log(name = "删除弹性伸缩")
	public BsmResult removeHpa(@RequestParam(value = Common.PARAMS, required = false) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		String name = jsonObject.getString("name");
		Long applicationId = jsonObject.getLong("applicationId");
		return service.removeHpa(applicationId, name);
	}
	
	@RequestMapping(value = "/list", method = { RequestMethod.GET })
	@Log(name = "服务依赖镜像列表查询")
	public BsmResult imageList(@RequestParam(value = Common.PARAMS, required = false) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		JSONArray array = JSONObject.parseArray(jsonObject.getString("images"));
		return imageService.list(array);
	}
	
	@RequestMapping(value = "/config", method = { RequestMethod.GET })
	@Log(name = "获取服务部署关联的配置项")
	public BsmResult getConfigMap(@RequestParam(value = Common.PARAMS, required = false) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		String name = jsonObject.getString("name");
		String resourceType = jsonObject.getString("resourceType");
		return service.configMap(applicationId, name, resourceType);
	}
	
	@RequestMapping(value = "/alarm", method = { RequestMethod.POST })
	@Log(name = "设置服务实例数告警策略")
	public BsmResult serviceAlarmSave(@RequestParam(value = Common.PARAMS, required = true) String params) {
		ServiceAlarm serviceAlarm = (ServiceAlarm) JSONObject.parseObject(params, ServiceAlarm.class);
		return service.serviceAlarmSave(serviceAlarm);
	}
	
	@RequestMapping(value = "/alarm", method = { RequestMethod.GET })
	@Log(name = "查询服务实例数告警策略详情")
	public BsmResult serviceAlarmDetail(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		String name = jsonObject.getString("name");
		return service.serviceAlarmDetail(applicationId, name);
	}
	
	@RequestMapping(value = "/update/alarm", method = { RequestMethod.POST })
	@Log(name = "修改服务实例数告警策略")
	public BsmResult serviceAlarmModify(@RequestParam(value = Common.PARAMS, required = true) String params) {
		ServiceAlarm serviceAlarm = (ServiceAlarm) JSONObject.parseObject(params, ServiceAlarm.class);
		return service.serviceAlarmModify(serviceAlarm);
	}
	
	@RequestMapping(value = "/list/job", method = { RequestMethod.POST })
	@Log(name = "批处理任务查询")
	public BsmResult listJob(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		return service.listJob(applicationId);
	}
	
	@RequestMapping(value = "/job", method = { RequestMethod.DELETE})
	@Log(name = "删除批处理任务")
	public BsmResult deleteJob(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long applicationId = jsonObject.getLong("applicationId");
		String jobName = jsonObject.getString("jobName");
		return service.deleteJob(applicationId, jobName);
	}

}
