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
import com.bocloud.paas.server.interceptor.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.paas.entity.ConfigManage;
import com.bocloud.paas.service.application.ConfigManageService;

/**
 * describe: 配置管理server控制层
 * @author Zaney
 * @data 2017年10月17日
 */
@RestController
@RequestMapping("/config/manage")
public class ConfigManageController {
	@Autowired
	private ConfigManageService configManageService;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/config", method = { RequestMethod.POST })
	@Log(name="配置实例创建")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject object = JSONTools.isJSONObj(params);
		ConfigManage configManage = JSONObject.parseObject(object.getString("configManage"), ConfigManage.class);
		Map<String, String> dataMap = JSONObject.parseObject(object.getString("dataMap"), Map.class);
		return configManageService.create(configManage, dataMap, user);
	}
	
	@RequestMapping(value = "/config", method = {RequestMethod.GET})
	@Log(name = "配置实例列表查询")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			@Value(Common.REQ_USER) RequestUser user){
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return configManageService.list(page, rows, paramList, sorterMap, simple, user);
	}
	
	@RequestMapping(value = "/{id}/config", method = {RequestMethod.GET})
	@Log(name = "获取配置实例详情")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id){
		return configManageService.detail(id);
	}
	
	@RequestMapping(value = "/config", method = {RequestMethod.DELETE})
	@Log(name = "删除配置实例")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Long> ids = JSON.parseArray(jsonObject.get("ids").toString(), Long.class);
		return configManageService.remove(ids);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/config", method = {RequestMethod.POST})
	@Log(name = "修改配置实例")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject object = JSONTools.isJSONObj(params);
		Long id = object.getLong("id");
		String remark = object.getString("remark");
		Map<String, String> dataMap = JSONObject.parseObject(object.getString("dataMap"), Map.class);
		return configManageService.modify(id, remark, dataMap, user);
	}
	
}
