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
@RequestMapping(value = "/host")
public class HostController {

	private final String BASE_SERVICE = "/host";
	@Autowired
	private BasicController basicController;
	@Autowired
	private ServiceFactory serviceFactory;
	private static final BoCloudService SERVICE = BoCloudService.Cmp;

	/**
	 * 向集群中添加主机
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addHost", method = { RequestMethod.POST })
	public BsmResult addHost(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/addHost";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除集群中的主机,支持批量移除
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/removeHost", method = { RequestMethod.POST })
	public BsmResult removeHost(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/removeHost";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 添加主机：添加到数据库
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		BsmResult result = basicController.create(params, BASE_SERVICE, request, HostController.class.getSimpleName());
		return result;
	}

	/**
	 * 删除主机
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
	 * 查询主机列表
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
				HostController.class.getSimpleName());
		return result;
	}

	/**
	 * 修改主机
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		BsmResult result = basicController.modify(params, BASE_SERVICE, request, HostController.class.getSimpleName());
		return result;
	}

	/**
	 * 查询主机详情
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, HostController.class.getSimpleName());
	}

	/**
	 * 查询正常的主机且不在环境中
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryNormalHost", method = { RequestMethod.POST })
	public BsmResult queryNormalHost(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/queryNormalHost";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 查询不在环境中的主机
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryHostNotInEnv", method = { RequestMethod.POST })
	public BsmResult queryHostNotInEnv(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/queryHostNotInEnv";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询处于某一环境中的主机
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryHostInEnv", method = { RequestMethod.POST })
	public BsmResult queryHostInEnv(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/queryHostInEnv";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 环境中的节点调度
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/scheduleNode", method = { RequestMethod.POST })
	public BsmResult scheduleNode(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/scheduleNode";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
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
	public BsmResult monitorGpu(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/monitor/gpu";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

}
