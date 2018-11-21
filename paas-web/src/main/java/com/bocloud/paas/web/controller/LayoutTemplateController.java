package com.bocloud.paas.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.ListHelper;
import com.bocloud.common.utils.MapTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

@RestController
@RequestMapping("/layout/template")
public class LayoutTemplateController {
	private static final String BASE_SERVICE = "/layout/template";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;
	@Autowired
	private BasicController basicController;

	/**
	 * 编排模板创建
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONObject.parseObject(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/create";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS,
					params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url,
					BoCloudMethod.CREATE, null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 列表
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(
			@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			HttpServletRequest request) {
		Map<String, Object> param = ListHelper.assembleParam(page, rows, params, sorter, simple);
        String url = BASE_SERVICE + "/list";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
        return service.invoke();
	}

	/**
	 * 更新
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(
			@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return basicController.modify(params, BASE_SERVICE, request, LayoutTemplateController.class.getSimpleName());
	}

	/**
	 * 删除
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.DELETE })
	public BsmResult remove(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		// 1、数据格式转换
		@SuppressWarnings("unchecked")
		Map<String, JSONArray> mapObj = JSONObject.parseObject(params, Map.class);
		JSONArray parseArray = mapObj.get("id");
		for (Object obj : parseArray) {
			String param = "{'id':" + Long.valueOf(obj.toString()) + "}";
			// 2、执行删除操作
			BsmResult removeResult = basicController.remove(param, BASE_SERVICE, request,
					LayoutTemplateController.class.getSimpleName());
			if (removeResult.isSuccess()) {
				continue;
			} else {
				return new BsmResult(false, "移除失败");
			}
		}
		return new BsmResult(true, "移除成功");
	}

	/**
	 * 详情
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.POST })
	public BsmResult detail(
			@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return  basicController.detail(params, BASE_SERVICE, request, LayoutTemplateController.class.getSimpleName());
	}
	
	@RequestMapping(value = "/getList", method = {RequestMethod.GET})
	public BsmResult getList(HttpServletRequest request) {
			String url = BASE_SERVICE + "/getList";
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LOAD, null, null,request);
			return service.invoke();
	}

}
