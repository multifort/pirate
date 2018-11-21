package com.bocloud.paas.web.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.ListHelper;
import com.bocloud.common.utils.MapTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

/**
 * @author Zaney
 * @data:2017年3月9日
 * @describe:容器管理页面信息展示
 */
@RestController
@RequestMapping("/container")
public class ContainerController {
	private final String BASE_SERVICE = "/container";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;

	/**
	 * 各资源数据统计
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/total", method = { RequestMethod.GET })
	public BsmResult total(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/total";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 资源列表展示
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult listResource(@RequestParam(value = Common.PARAMS, required = true) String params, 
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/list";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
				request);
		return service.invoke();
	}

	/**
	 * 节点下的pod列表展示
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/node/list", method = { RequestMethod.POST })
	public BsmResult listNodePod(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = true) String params, HttpServletRequest request) {
		String url = BASE_SERVICE + "/node/list";
		Map<String, Object> paramMap = ListHelper.assembleParam(page, rows, params, null, null);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 资源详情
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.POST })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/detail";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 资源事件
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/event", method = { RequestMethod.POST })
	public BsmResult event(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = true) String params, HttpServletRequest request) {
		String url = BASE_SERVICE + "/event";
		Map<String, Object> paramMap = ListHelper.assembleParam(page, rows, params, null, null);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 获取日志信息
	 * 
	 * @param params
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/log", method = { RequestMethod.GET })
	public BsmResult getLog(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/log";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 获取资源yaml文件模板
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/template", method = { RequestMethod.GET })
	public BsmResult template(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/template";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
}
