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
@RequestMapping(value = "/dict")
public class DictionaryController {

	private final String BASE_SERVICE = "/dict";
	@Autowired
	private BasicController basicController;
	@Autowired
	private ServiceFactory serviceFactory;
	private static final BoCloudService SERVICE = BoCloudService.Cmp;

	/**
	 * 数据字典：创建
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		BsmResult result = basicController.create(params, BASE_SERVICE,
				request, DictionaryController.class.getSimpleName());
		return result;
	}

	/**
	 * 数据字典：修改
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		BsmResult result = basicController.modify(params, BASE_SERVICE,
				request, DictionaryController.class.getSimpleName());
		return result;
	}

	/**
	 * 数据字典：删除
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/remove";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,
					params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,
					BoCloudMethod.OPERATE, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 数据字典：查询
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.GET })
	public BsmResult list(
			@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			HttpServletRequest request) {
		BsmResult result = basicController.list(page, rows, params, sorter,
				simple, BASE_SERVICE, request,
				DictionaryController.class.getSimpleName());
		return result;
	}

	/**
	 * 校验数据字典key是否存在
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/checkKey", method = { RequestMethod.GET })
	public BsmResult checkKey(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/checkKey";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,
					params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,
					BoCloudMethod.BASIC, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 参数页面展示
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/statistic", method = { RequestMethod.GET })
	public BsmResult statistic(
			@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/statistic";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url,
				BoCloudMethod.BASIC, null, null, request);
		return service.invoke();
	}

	/**
	 * 单个或者多个参数的修改
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/batchModify", method = { RequestMethod.POST })
	public BsmResult batchModify(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/batchModify";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,
					params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,
					BoCloudMethod.OPERATE, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 参数详情
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/detail";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,
					params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,
					BoCloudMethod.BASIC, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 获取参数新增模板
	 * @return
	 */
	@RequestMapping(value = "/template", method = { RequestMethod.GET })
	public BsmResult template(HttpServletRequest request) {
		String url = BASE_SERVICE + "/template";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url,
				BoCloudMethod.BASIC, null, null, request);
		return service.invoke();
	}

}
