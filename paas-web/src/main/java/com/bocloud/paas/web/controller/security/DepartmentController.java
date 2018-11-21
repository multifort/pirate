package com.bocloud.paas.web.controller.security;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.web.controller.BasicController;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.MapTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;
import com.bocloud.registry.utils.UrlTranslator;

@RestController
@RequestMapping("/department")
public class DepartmentController {

	private final String BASE_SERVICE = "/department";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;

	@Autowired
	private ServiceFactory serviceFactory;
	@Autowired
	private BasicController basicController;

	/**
	 * 获取权限列表
	 * 
	 * @param params
	 *            请求参数
	 * @param request
	 *            请求对象
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PARENTID, required = true) Long parentId,
			HttpServletRequest request) {
		Map<String, Object> param = new HashMap<>();
		if (null != parentId) {
			param.put(Common.PARENTID, parentId);
		}
		String url = BASE_SERVICE + "/list";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
		return service.invoke();
	}

	/**
	 * 权限创建
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return basicController.create(params, BASE_SERVICE, request, DepartmentController.class.getSimpleName());
	}

	/**
	 * 权限修改
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return basicController.modify(params, BASE_SERVICE, request, DepartmentController.class.getSimpleName());
	}

	/**
	 * 权限删除
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.remove(params, BASE_SERVICE, request, DepartmentController.class.getSimpleName());
	}

	/**
	 * 权限详细
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, DepartmentController.class.getSimpleName());
	}

	/**
	 * 配额
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/quota", method = { RequestMethod.POST })
	public BsmResult quota(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		if (null != obj) {
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, BASE_SERVICE + "/quota", BoCloudMethod.OTHERS,
					null, paramMap, request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 资源
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/res", method = { RequestMethod.POST })
	public BsmResult res(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		String url = UrlTranslator.translate(BASE_SERVICE + "/res", obj.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, null, request);
		return service.invoke();
	}

	/**
	 * 删除资源
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteRes", method = { RequestMethod.POST })
	public BsmResult deleteRes(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject && jsonObject.containsKey(Common.ID)) {
			String url = UrlTranslator.translate(BASE_SERVICE + "/deleteRes", jsonObject.getLong(Common.ID));
			Map<String, Object> paramMap = MapTools.simpleMap("resId",
					JSONObject.toJSONString(jsonObject.get("resId")));
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 分配资源
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/assign", method = { RequestMethod.POST })
	public BsmResult assign(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject && jsonObject.containsKey(Common.ID)) {
			String url = UrlTranslator.translate(BASE_SERVICE + "/assign", jsonObject.get(Common.ID));
			Map<String, Object> paramMap = MapTools.simpleMap("resources",
					JSONObject.toJSONString(jsonObject.get("resources")));
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}

}
