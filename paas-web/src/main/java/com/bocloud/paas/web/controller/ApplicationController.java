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
import com.bocloud.paas.web.controller.BasicController;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {
	private final String BASE_SERVICE = "/application";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private BasicController basicController;
	@Autowired
	private ServiceFactory serviceFactory;

	/**
	 * 创建应用
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.create(params, BASE_SERVICE, request, ApplicationController.class.getSimpleName());
	}

	/**
	 * 更新
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.modify(params, BASE_SERVICE, request, ApplicationController.class.getSimpleName());
	}

	/**
	 * 列表展示
	 * 
	 * @author zjm
	 * @date 2017年3月17日
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
			@RequestParam(value = Common.ROW, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				ApplicationController.class.getSimpleName());
	}

	/**
	 * 根据编排获取应用
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/listByLayout", method = { RequestMethod.POST })
	public BsmResult listByLayout(
			@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROW, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				ApplicationController.class.getSimpleName(), "/listByLayout");
	}
	
	/**
	 * 删除
	 * 
	 * @author zjm
	 * @date 2017年3月17日
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
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 详情
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, ApplicationController.class.getSimpleName());
	}


	/**
	 * 回滚
	 * 
	 * @author zjm
	 * @date 2017年4月6日
	 *
	 * @param params
	 * @param imageName
	 * @param appId
	 * @param resourceName
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/rollback", method = { RequestMethod.POST })
	public BsmResult rollback(@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = "appId", required = true) Long appId,
			@RequestParam(value = "namespace", required = true) String namespace,
			@RequestParam(value = "resourceName", required = true) String resourceName, HttpServletRequest request) {
		String url = BASE_SERVICE + "/rollback";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		paramMap.put("appId", appId);
		paramMap.put("namespace", namespace);
		paramMap.put("resourceName", resourceName);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap, request);
		return service.invoke();
	}

	
	/**
	 * 获取饼状图数据信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/data", method = { RequestMethod.GET })
	public BsmResult data(HttpServletRequest request){
		String url = BASE_SERVICE + "/data";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null, request);
		return service.invoke();
	}
	/**
	 * 监控
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/monitor", method = { RequestMethod.GET })
	public BsmResult kubernetesMonitor(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/monitor";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 创建ingress
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/ingress", method = { RequestMethod.POST })
	public BsmResult createIngress(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			String url = BASE_SERVICE + "/ingress";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 获取所有已暴露的服务
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/ingress", method = { RequestMethod.GET })
	public BsmResult getIngress(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			String url = BASE_SERVICE + "/ingress";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 获取服务端口
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/service/port", method = { RequestMethod.GET })
	public BsmResult ingress(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			String url = BASE_SERVICE + "/service/port";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 获取应用资源
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/resource", method = { RequestMethod.GET })
	public BsmResult getResource(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			String url = BASE_SERVICE + "/resource";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 应用实例模板化，暂时放在这儿，以后移动到应用实例Controller
	 */
	@RequestMapping(value = "/templatable", method = { RequestMethod.POST })
	public BsmResult templatable(
			@RequestParam(value = Common.PARAMS, required = true) String params,HttpServletRequest request) {
		
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/templatable";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,BoCloudMethod.CREATE, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 获取应用拓扑图信息
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
	
	/**
	 * 删除服务暴露出的路径
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/ingress", method = { RequestMethod.DELETE })
	public BsmResult deleteServiceIngress(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/ingress";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,BoCloudMethod.REMOVE, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	//============================== 资源配额 =============================//
	/**
	 * 创建资源配额对象
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/quota", method = {RequestMethod.POST})
	public BsmResult createResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params, 
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/quota";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,BoCloudMethod.OPERATE, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 获取资源配额对象信息
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/quota", method = {RequestMethod.GET})
	public BsmResult detailResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params, 
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/quota";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,BoCloudMethod.BASIC, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 修改资源配额对象
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/update/quota", method = {RequestMethod.POST})
	public BsmResult modifyResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params, 
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/update/quota";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,BoCloudMethod.OPERATE, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 获取被使用的资源配额
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/quota/used", method = {RequestMethod.GET})
	public BsmResult getUsedResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params, 
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/quota/used";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,BoCloudMethod.BASIC, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 删除资源配额
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/quota", method = {RequestMethod.DELETE})
	public BsmResult deleteResourceQuota(@RequestParam(value = Common.PARAMS, required = true) String params, 
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/quota";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,BoCloudMethod.REMOVE, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
}
