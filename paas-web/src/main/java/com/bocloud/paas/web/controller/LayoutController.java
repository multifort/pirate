package com.bocloud.paas.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.web.controller.BasicController;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.ListHelper;
import com.bocloud.common.utils.MapTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/layout")
public class LayoutController {
	private static final String BASE_SERVICE = "/layout";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;
	@Autowired
	private BasicController basicController;

	/**
	 * 编排文件创建
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		if (null != obj) {
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, BASE_SERVICE + "/create", BoCloudMethod.CREATE,
					null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 列表
	 *
	 * @param page
	 *            当前页码
	 * @param rows
	 *            页面数据大小
	 * @param params
	 *            查询参数
	 * @param sorter
	 *            排序参数，例如：{"name":0|1,"password":0|1},0表示增序，1表示降序
	 * @param simple
	 *            简单查询标记，只有true和false,为false时返回资源池的详细信息，为true时只返回id和name值
	 * @param request
	 *            请求对象
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				LayoutController.class.getSimpleName());
	}

	/**
	 * 按照应用id查询编排列表
	 *
	 * @author zjm
	 * @date 2017年3月23日
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.GET })
	public BsmResult listByAppId(
			@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@RequestParam(value = "appId", required = true) Long appId, HttpServletRequest request) {
		Map<String, Object> param = ListHelper.assembleParam(page, rows, params, sorter, simple);
        String url = BASE_SERVICE + "/list";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, param, request);
        return service.invoke();
	}

	/**
	 * 更新
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return basicController.modify(params, BASE_SERVICE, request, LayoutController.class.getSimpleName());
	}

	/**
	 * 删除
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
					LayoutController.class.getSimpleName());
			if (removeResult.isSuccess()) {
				continue;
			} else {
				return new BsmResult(false, "移除失败");
			}
		}
		return new BsmResult(true, "移除成功");
	}

	/**
	 * 详细
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, LayoutController.class.getSimpleName());
	}

	/**
	 * 按照编排模版获取动态属性
	 * @param params
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getVariablesById", method = { RequestMethod.POST })
	public BsmResult getVariablesById(@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.ID, required = true) Long id, HttpServletRequest request) {
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		paramMap.put("id", id);
		String url = BASE_SERVICE + "/getVariablesById";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap, request);
		return service.invoke();
	}
	
	/**
	 * 获取被使用的应用信息
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/used", method = { RequestMethod.GET })
	public BsmResult listByAppId(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			HttpServletRequest request) {
		Map<String, Object> param = ListHelper.assembleParam(page, rows, params, sorter, simple);
        String url = BASE_SERVICE + "/used";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, param, request);
        return service.invoke();
	}
}
