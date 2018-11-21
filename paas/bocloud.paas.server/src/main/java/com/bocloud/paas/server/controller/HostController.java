package com.bocloud.paas.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
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
import com.bocloud.paas.entity.Host;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.resource.HostService;

@RestController
@RequestMapping("/host")
public class HostController {

	@Autowired
	private HostService hostService;

	/**
	 * 添加主机到环境
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/addHost", method = { RequestMethod.POST })
	@Log(name = "添加主机到环境")
	public BsmResult addHost(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			Host host = JSONObject.parseObject(jsonObject.toJSONString(), Host.class);
			BsmResult result = hostService.addHost(host, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除集群中的主机
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/removeHost", method = { RequestMethod.POST })
	@Log(name = "删除环境中主机")
	public BsmResult removeHost(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			List<Long> ids = JSON.parseArray(jsonObject.get("ids").toString(), Long.class);
			return hostService.removeHost(ids, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 添加主机：添加到数据库
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "添加主机")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Host host = JSONObject.parseObject(object.toJSONString(), Host.class);
			BsmResult result = hostService.create(host, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除主机
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	@Log(name = "删除主机")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			List<Long> ids = JSON.parseArray(jsonObject.get("id").toString(), Long.class);
			return hostService.remove(ids, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询主机
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
	@Log(name = "查询主机列表")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		BsmResult result = hostService.list(page, rows, paramList, sorterMap, simple, user.getId());
		return result;
	}

	/**
	 * 更改主机
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "更改主机")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Host host = JSONObject.parseObject(object.toJSONString(), Host.class);
			BsmResult result = hostService.modify(host, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 主机详细信息
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	@Log(name = "主机详情")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return hostService.detail(id);
	}

	/**
	 * 查询状态正常且不在环境中的主机
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/queryNormalHost", method = { RequestMethod.POST })
	@Log(name = "查询状态正常且不在环境中的主机")
	public BsmResult queryNormalHost(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		return hostService.queryNormalHost(null, user.getId());
	}

	/**
	 * 查询不在环境中的主机
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/queryHostNotInEnv", method = { RequestMethod.POST })
	@Log(name = "获取不在环境中的主机")
	public BsmResult queryHostNotInEnv(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			// 获取环境id
			String envId = jsonObject.getString("envId");
			if(!StringUtils.isEmpty(envId)){
				return hostService.queryHostNotInEnv(Long.parseLong(envId), user.getId());
			}else{
				return hostService.queryHostNotInEnv(null, user.getId());
			}
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询处于某一环境中的主机
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/queryHostInEnv", method = { RequestMethod.POST })
	@Log(name = "查询处于特定环境中的主机")
	public BsmResult queryHostInEnv(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			// 获取环境id
			String envId = jsonObject.getString("id");
			return hostService.queryHostInEnv(Long.parseLong(envId), user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 环境中节点调度
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/scheduleNode", method = { RequestMethod.POST })
	@Log(name = "节点调度")
	public BsmResult scheduleNode(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String id = jsonObject.getString("id");
			return hostService.scheduleNode(Long.parseLong(id), user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 监控GPU
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/monitor/gpu", method = { RequestMethod.GET })
	@Log(name = "监控GPU")
	public BsmResult monitorGpu(@RequestParam(value = Common.PARAMS, required = true) String params) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			Long id = jsonObject.getLong("id");
			String num = jsonObject.getString("num");
			String timeUnit = jsonObject.getString("timeUnit");
			return hostService.monitorGpu(id, num, timeUnit);
		} else {
			return ResultTools.formatErrResult();
		}
	}

}
