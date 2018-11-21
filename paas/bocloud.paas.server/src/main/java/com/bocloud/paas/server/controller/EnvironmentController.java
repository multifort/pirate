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
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.Host;
import com.bocloud.paas.model.Cluster;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.environment.EnvironmentService;

@RestController
@RequestMapping("/environment")
public class EnvironmentController {

	@Autowired
	private EnvironmentService environmentService;

	/**
	 * 创建环境
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "创建环境")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Environment environment = JSONObject.parseObject(object.toJSONString(), Environment.class);
			BsmResult result = environmentService.create(environment, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}

	}

	/**
	 * 更改环境
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "更改环境")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Environment environment = JSONObject.parseObject(object.toJSONString(), Environment.class);
			BsmResult result = environmentService.modify(environment, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除环境
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	@Log(name = "删除环境")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Long> ids = JSON.parseArray(jsonObject.get("id").toString(), Long.class);
		return environmentService.remove(ids, user.getId());
	}

	/**
	 * 查询环境
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
	@Log(name = "查询环境")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		BsmResult result = environmentService.list(page, rows, paramList, sorterMap, simple, user.getId());
		return result;
	}

	/**
	 * 环境详细信息
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	@Log(name = "查询环境详情")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return environmentService.detail(id);
	}

	/**
	 * 环境操作
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/operate", method = { RequestMethod.POST })
	@Log(name = "环境操作")
	public BsmResult operate(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			Environment environment = JSONObject.parseObject(jsonObject.toString(), Environment.class);
			return environmentService.operate(environment, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 集群已存在，接管集群
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/receiveCluster", method = { RequestMethod.POST })
	@Log(name = "接管集群")
	public BsmResult receiveCluster(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			Host master = JSONObject.parseObject(jsonObject.toString(), Host.class);
			return environmentService.receiveCluster(master, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 向通过平台创建的集群中添加节点
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/addNode", method = { RequestMethod.POST })
	@Log(name = "添加节点")
	public BsmResult addNode(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			Host host = JSONObject.parseObject(jsonObject.toString(), Host.class);
			return environmentService.addNode(host, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	@RequestMapping(value = "/deleteNode", method = {RequestMethod.POST})
	@Log(name = "删除节点")
	public BsmResult deleteNode(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			Host host = JSONObject.parseObject(jsonObject.toString(), Host.class);
			return environmentService.deleteNode(host, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询可添加节点环境
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/queryNormalEnv", method = { RequestMethod.POST })
	@Log(name = "查询正常环境")
	public BsmResult queryNormalEnv(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		return environmentService.queryNormalEnv(user.getId());
	}

	/**
	 * 在裸机上部署kubernetes集群
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/createKubernetesCluser", method = { RequestMethod.POST })
	@Log(name = "创建k8s集群")
	public BsmResult createKubernetesCluser(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
//			List<Host> hosts = JSON.parseArray(jsonObject.get("hosts").toString(), Host.class);
			Cluster  cluster = JSON.parseObject(jsonObject.toString(), Cluster.class);
			return environmentService.createKubernetesCluser(cluster, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/getNameSpace", method = { RequestMethod.POST })
	@Log(name = "获取命名空间")
	public BsmResult nameSpace(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long id = null;
		if (null != jsonObject) {
			id = jsonObject.getLong("id");
			return environmentService.nameSpace(id, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/getMonitorUrl", method = { RequestMethod.POST })
	@Log(name = "获取监控地址")
	public BsmResult monitorUrl(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		Long id = null;
		if (null != jsonObject) {
			id = jsonObject.getLong("id");
			return environmentService.monitorUrl(id, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	@RequestMapping(value = "/topology", method = { RequestMethod.GET })
	@Log(name="获取环境拓扑图信息")
	public BsmResult envTopology(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		Long envId = object.getLong("envId");
		return environmentService.envTopology(envId);
	}

}
