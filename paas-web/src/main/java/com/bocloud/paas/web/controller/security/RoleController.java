package com.bocloud.paas.web.controller.security;

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
import com.bocloud.common.utils.MapTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;
import com.bocloud.registry.utils.UrlTranslator;

@RestController
@RequestMapping("/role")
public class RoleController {

	private final String BASE_SERVICE = "/role";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;
	@Autowired
	private BasicController basicController;

	/**
	 * 角色创建
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return basicController.create(params, BASE_SERVICE, request, RoleController.class.getSimpleName());
	}

	/**
	 * 获取角色列表
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
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				RoleController.class.getSimpleName());
	}

	/**
	 * 修改角色
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return basicController.modify(params, BASE_SERVICE, request, RoleController.class.getSimpleName());
	}

	/**
	 * 删除角色
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.remove(params, BASE_SERVICE, request, RoleController.class.getSimpleName());
	}

	/**
	 * 角色详细
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.POST })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, RoleController.class.getSimpleName());
	}

	/**
	 * 授权
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/accredit", method = { RequestMethod.POST })
	public BsmResult accredit(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONObject.parseObject(params);
		Map<String, Object> paramMap = MapTools.simpleMap(Common.AUTHS, jsonObject.getString(Common.AUTHS));
		String url = UrlTranslator.translate(BASE_SERVICE + "/accredit", jsonObject.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 获取权限列表
	 * 
	 * @param params
	 * @return 数据结果集
	 */
	@RequestMapping(value = "/auths", method = { RequestMethod.POST })
	public BsmResult auths(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARENTID, obj.get(Common.PARENTID));
		String url = UrlTranslator.translate(BASE_SERVICE + "/auths", obj.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 获取命令列表
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/commands", method = { RequestMethod.POST })
	public BsmResult commands(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		Map<String, Object> paramMap = MapTools.simpleMap(Common.ID, obj.get(Common.ID));
		String url = UrlTranslator.translate(BASE_SERVICE + "/commands", obj.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 授权命令
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/accCommand", method = { RequestMethod.POST })
	public BsmResult accreditCommand(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONObject.parseObject(params);
		Map<String, Object> paramMap = MapTools.simpleMap(Common.COMMANDS, jsonObject.getString(Common.COMMANDS));
		String url = UrlTranslator.translate(BASE_SERVICE + "/accCommand", jsonObject.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap, request);
		return service.invoke();
	}
}
