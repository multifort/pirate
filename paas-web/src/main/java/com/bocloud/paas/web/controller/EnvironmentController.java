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
@RequestMapping(value = "/environment")
public class EnvironmentController {

	private final String BASE_SERVICE = "/environment";
	@Autowired
	private BasicController basicController;
	@Autowired
	private ServiceFactory serviceFactory;
	private static final BoCloudService SERVICE = BoCloudService.Cmp;

	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		BsmResult result = basicController.create(params, BASE_SERVICE, request,
				EnvironmentController.class.getSimpleName());
		return result;
	}

	/**
	 * 环境更改：更改描述及名称
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		BsmResult result = basicController.modify(params, BASE_SERVICE, request,
				EnvironmentController.class.getSimpleName());
		return result;
	}

	/**
	 * 环境的删除，备注：单个删除和批量删除都是这个方法
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/remove";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询环境
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		BsmResult result = basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				EnvironmentController.class.getSimpleName());
		return result;
	}

	/**
	 * 查询环境的详细信息
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, EnvironmentController.class.getSimpleName());
	}

	/**
	 * 环境操作：更改环境状态
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/operate", method = { RequestMethod.POST })
	public BsmResult operate(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/operate";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 集群已存在，接管集群
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/receiveCluster", method = { RequestMethod.POST })
	public BsmResult receiveCluster(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/receiveCluster";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 向通过平台创建的环境中添加节点
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addNode", method = { RequestMethod.POST })
	public BsmResult addNode(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/addNode";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	@RequestMapping(value = "/deleteNode", method = {RequestMethod.POST})
	public BsmResult deleteNode(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/deleteNode";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询状态正常的环境
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryNormalEnv", method = { RequestMethod.POST })
	public BsmResult queryNormalEnv(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/queryNormalEnv";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 在裸机上搭建kubernetes集群
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/createKubernetesCluser", method = { RequestMethod.POST })
	public BsmResult createKubernetesCluser(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/createKubernetesCluser";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询环境下的命名空间
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getNameSpace", method = { RequestMethod.POST })
	public BsmResult nameSpace(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/getNameSpace";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 获取监控url
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getMonitorUrl", method = { RequestMethod.POST })
	public BsmResult monitorUrl(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/getMonitorUrl";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 获取环境拓扑图信息
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/topology", method = { RequestMethod.GET })
	public BsmResult applicationTopology(@RequestParam(value = Common.PARAMS, required = true) String params, 
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/topology";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,BoCloudMethod.BASIC, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

}
