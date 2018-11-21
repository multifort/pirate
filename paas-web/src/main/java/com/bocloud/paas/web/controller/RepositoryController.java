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
import com.bocloud.paas.web.controller.BasicController;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.ListHelper;
import com.bocloud.common.utils.MapTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

@RestController
@RequestMapping("/registry")
public class RepositoryController {
	private final String BASE_SERVICE = "/registry";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private BasicController basicController;
	@Autowired
	private ServiceFactory serviceFactory;

	/**
	 * 创建仓库
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.create(params, BASE_SERVICE, request, RepositoryController.class.getSimpleName());
	}

	/**
	 * 更新
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.modify(params, BASE_SERVICE, request, RepositoryController.class.getSimpleName());
	}

	/**
	 * 列表展示
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
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				RepositoryController.class.getSimpleName());
	}
	
	/**
	 * 获取流程管控模块推送镜像获取仓库地址信息
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
	public BsmResult listAddress(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		Map<String, Object> param = ListHelper.assembleParam(page, rows, params, sorter, simple);
        String url = BASE_SERVICE + "/list";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, param, request);
        return service.invoke();
	}

	/**
	 * 删除
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		// 1、数据格式转换
		@SuppressWarnings("unchecked")
		Map<String, JSONArray> mapObj = JSONObject.parseObject(params, Map.class);
		JSONArray parseArray = mapObj.get("id");
		for (Object obj : parseArray) {
			String param = "{'id':" + Long.valueOf(obj.toString()) + "}";
			// 2、执行删除操作
			BsmResult removeResult = basicController.remove(param, BASE_SERVICE, request,
					RepositoryController.class.getSimpleName());
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
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, RepositoryController.class.getSimpleName());
	}

	/**
	 * 统计
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/count", method = { RequestMethod.POST })
	public BsmResult count(HttpServletRequest request) {
		String url = BASE_SERVICE + "/count";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null, request);
		return service.invoke();
	}

	/**
	 * 获取仓库的镜像信息
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/images", method = { RequestMethod.POST })
	public BsmResult images(
			@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params, HttpServletRequest request) {
		String url = BASE_SERVICE + "/images";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		paramMap.put("page", page);
		paramMap.put("rows", rows);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * master namespace、username校验
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/master/check", method = { RequestMethod.POST })
	public BsmResult checkMaster(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/master/check";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap, request);
		return service.invoke();
	}

	
	/**
	 * 仓库镜像同步
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sycn/image", method = { RequestMethod.GET })
	public BsmResult gc(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/sycn/image";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
}
