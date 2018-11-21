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
import com.bocloud.paas.entity.LoadBalance;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.resource.LoadBalanceService;

@RestController
@RequestMapping("/loadBalance")
public class LoadBalanceController {

	@Autowired
	private LoadBalanceService loadBalanceService;

	/**
	 * 创建负载
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			LoadBalance loadBalance = JSONObject.parseObject(object.toJSONString(), LoadBalance.class);
			BsmResult result = loadBalanceService.create(loadBalance, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}

	}

	/**
	 * 更改负载
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			LoadBalance loadBalance = JSONObject.parseObject(object.toJSONString(), LoadBalance.class);
			BsmResult result = loadBalanceService.modify(loadBalance, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除负载
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Long> ids = JSON.parseArray(jsonObject.get("id").toString(), Long.class);
		return loadBalanceService.remove(ids, user.getId());
	}

	/**
	 * 查询负载
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
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		BsmResult result = loadBalanceService.list(page, rows, paramList, sorterMap, simple);
		return result;
	}

	/**
	 * 负载详细信息
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return loadBalanceService.detail(id);
	}
	
	@RequestMapping(value = "/listApps", method = { RequestMethod.POST })
	@Log(name="负载应用列表")
	public BsmResult listApps(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return loadBalanceService.listApps(page, rows, paramList, sorterMap, simple, user);
	}

	/**
	 * 检测负载名称是否已存在
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/checkName", method = { RequestMethod.GET })
	public BsmResult checkName(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String envName = jsonObject.getString("name");
			return loadBalanceService.checkName(envName, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}
}
