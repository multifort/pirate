package com.bocloud.paas.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.paas.entity.ApplicationStore;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.application.ApplicationStoreService;

@RestController
@RequestMapping("/application/store")
public class ApplicationStoreController {
	@Autowired
	private ApplicationStoreService applicationStoreService;
	
	@RequestMapping(value = "/list", method = { RequestMethod.GET })
	@Log(name="应用商店组件列表查询")
	public BsmResult list(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		String name = jsonObject.getString("name");
		return applicationStoreService.list(name);
	}
	
	@RequestMapping(value = "/tempalate", method = { RequestMethod.GET })
	@Log(name = "应用商店模板内容")
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long storeId = jsonObject.getLong("storeId");
		String deployType = jsonObject.getString("deployType");
		return applicationStoreService.readTemplate(storeId, deployType);
	}
	
	@RequestMapping(value = "/deploy", method = { RequestMethod.POST })
	@Log(name = "应用商店模板内容")
	public BsmResult deploy(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long envId = jsonObject.getLong("envId");
		Long storeId = jsonObject.getLong("storeId");
		Long applicationId = jsonObject.getLong("applicationId");
		String deployType = jsonObject.getString("deployType");
		JSONObject template = JSONObject.parseObject(jsonObject.getString("template"));
		return applicationStoreService.deploy(envId, applicationId, storeId, template, deployType, user);
	}
	
	@RequestMapping(value = "/upload", method = { RequestMethod.POST })
	@Log(name = "应用商店商品文件上传")
	public BsmResult deploy(@RequestParam(value = Common.PARAMS, required = true) String params) {
		ApplicationStore applicationStore = JSONObject.parseObject(params, ApplicationStore.class);
		return applicationStoreService.upload(applicationStore);
	}
	
	@RequestMapping(value = "/upgrade", method = { RequestMethod.POST })
	@Log(name = "应用商店商品文件升级")
	public BsmResult versionUpgrade(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long storeId = jsonObject.getLong("storeId");
		Long imageId = jsonObject.getLong("imageId");
		String deployType = jsonObject.getString("deployType");
		return applicationStoreService.versionUpgrade(storeId, imageId, deployType);
	}
	
	@RequestMapping(value = "/down", method = { RequestMethod.POST })
	@Log(name = "应用商店商品下架")
	public BsmResult down(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long storeId = jsonObject.getLong("storeId");
		return applicationStoreService.down(storeId);
	}
	
	@RequestMapping(value = "/image", method = { RequestMethod.GET })
	@Log(name = "获取模板中镜像的名称")
	public BsmResult getTemplateImage(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long storeId = jsonObject.getLong("storeId");
		String deployType = jsonObject.getString("deployType");
		return applicationStoreService.getTemplateImage(storeId, deployType);
	}
	
	@RequestMapping(value = "/template", method = { RequestMethod.POST })
	@Log(name = "根据镜像制作模板")
	public BsmResult makeTemplate(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long imageId = jsonObject.getLong("imageId");
		JSONObject object = jsonObject.getJSONObject("jsonObject");
		return applicationStoreService.makeTemplate(imageId, object);
	}
	
	@RequestMapping(value = "/service", method = { RequestMethod.GET })
	@Log(name = "获取服务名称")
	public BsmResult getService(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long envId = jsonObject.getLong("envId");
		Long applicationId = jsonObject.getLong("applicationId");
		String type = jsonObject.getString("type");
		return applicationStoreService.getZookeeper(envId, applicationId, type, user);
	}
	
	@RequestMapping(value = "/image/port", method = { RequestMethod.GET })
	@Log(name = "获取组件模板的镜像内部端口")
	public BsmResult getImagePort(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long storeId = jsonObject.getLong("storeId");
		String deployType = jsonObject.getString("deployType");
		return applicationStoreService.getImagePort(storeId, deployType);
	}
	
}
