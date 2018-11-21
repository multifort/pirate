package com.bocloud.paas.web.controller.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.web.controller.BasicController;
import com.bocloud.paas.web.model.UserSecurity;
import com.bocloud.paas.web.utils.ExcelReader;
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
@RequestMapping("/user")
public class UserController {

	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	private final String BASE_SERVICE = "/user";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;
	@Autowired
	private BasicController basicController;

	/**
	 * 创建用户
	 * 
	 * @param params
	 *            用户对象
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return basicController.create(params, BASE_SERVICE, request, UserController.class.getSimpleName());
	}

	/**
	 * 批量导入
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/leadIn", method = { RequestMethod.POST })
	public BsmResult load(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		try {
			JSONObject jsonObject = JSONObject.parseObject(params);
			String path = jsonObject.getString("path");
			if (null == path) {
				return new BsmResult(false, "导入失败!");
			}
			InputStream inputStream = new FileInputStream(path);
			ExcelReader excelReader = new ExcelReader();
			List<String> list = excelReader.readExcelContent(inputStream, serviceFactory, SERVICE, request);
			for (String string : list) {
				BsmResult result = basicController.create(string, BASE_SERVICE, request,
						UserController.class.getSimpleName());
				jsonObject = JSONObject.parseObject(string);
				if (result.isSuccess()) {
					UserSecurity userSecurity = JSONObject.parseObject(result.getData().toString(), UserSecurity.class);
					Map<String, Object> paramMap = MapTools.simpleMap(Common.ROLES, jsonObject.get("roleId"));
					String url = UrlTranslator.translate(BASE_SERVICE + "/accredit", userSecurity.getUserId());
					RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap,
							request);
					service.invoke();
				}
			}
			return new BsmResult(true, "导入成功!");
		} catch (FileNotFoundException e) {
			logger.error("lead in excel error:", e);
			return new BsmResult(false, "导入失败!");
		}
	}

	/**
	 * 租户校验
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/checkUsername", method = { RequestMethod.POST })
	public BsmResult checkUsername(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject jsonObject = JSONObject.parseObject(params);
		Map<String, Object> paramMap = MapTools.simpleMap(Common.USERNAME, jsonObject.getString(Common.USERNAME));
		RemoteService service = serviceFactory.safeBuild(SERVICE, BASE_SERVICE + "/checkUsername", BoCloudMethod.OTHERS,
				null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 用户修改
	 * 
	 * @return
	 */
	@RequestMapping(value = "/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		return basicController.modify(params, BASE_SERVICE, request, UserController.class.getSimpleName());
	}

	/**
	 * /sec/user/remove
	 * 
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.remove(params, BASE_SERVICE, request, UserController.class.getSimpleName());
	}

	/**
	 * 获取用户列表
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
				UserController.class.getSimpleName());
	}

	/**
	 * 冻结用户
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/lock", method = { RequestMethod.POST })
	public BsmResult lock(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		String url = UrlTranslator.translate(BASE_SERVICE + "/lock", obj.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, null, request);
		return service.invoke();
	}

	/**
	 * 解冻用户
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/active", method = { RequestMethod.POST })
	public BsmResult active(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		String url = UrlTranslator.translate(BASE_SERVICE + "/active", obj.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, null, request);
		return service.invoke();
	}

	/**
	 * 密码重置
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/reset", method = { RequestMethod.POST })
	public BsmResult reset(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		String url = UrlTranslator.translate(BASE_SERVICE + "/reset", obj.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, null, request);
		return service.invoke();
	}

	/**
	 * 用户详细
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, UserController.class.getSimpleName());
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
		Map<String, Object> paramMap = MapTools.simpleMap(Common.ROLES, jsonObject.getString(Common.ROLES));
		String url = UrlTranslator.translate(BASE_SERVICE + "/accredit", jsonObject.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 用户检查
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/check", method = { RequestMethod.POST })
	public BsmResult check(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject param = JSONObject.parseObject(params);
		String userId = param.get(Common.ID).toString();
		String password = param.get(Common.PASSWORD).toString();
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PASSWORD, password);
		String url = UrlTranslator.translate(BASE_SERVICE + "/check", userId);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.MODIFY, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 密码修改
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/change", method = { RequestMethod.POST })
	public BsmResult change(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject param = JSONObject.parseObject(params);
		String userId = param.get(Common.ID).toString();
		String password = param.get(Common.PASSWORD).toString();
		String oldPassword = param.get(Common.OLD_PASSWORD).toString();
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PASSWORD, password);
		paramMap.put(Common.OLD_PASSWORD, oldPassword);
		String url = UrlTranslator.translate(BASE_SERVICE + "/change", userId);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.MODIFY, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 检查用户是否登录
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/checkUser", method = { RequestMethod.POST })
	public BsmResult checkUser(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject param = JSONObject.parseObject(params);
		String userId = param.get(Common.ID).toString();
		String sessionId = request.getSession().getId();
		Map<String, Object> paramMap = MapTools.simpleMap(Common.SESSIONID, sessionId);
		String url = UrlTranslator.translate(BASE_SERVICE + "/checkUser", userId);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.MODIFY, null, paramMap, request);
		BsmResult result = service.invoke();
		if (!result.isSuccess()) {
			HttpSession session = request.getSession();
			session.invalidate();
			return new BsmResult(false, "用户已在其他地点登录!");
		}
		return result;
	}

	/**
	 * 获取角色列表
	 * 
	 * @param params
	 * @return 数据结果集
	 */
	@RequestMapping(value = "/roles", method = { RequestMethod.POST })
	public BsmResult roles(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject obj = JSONObject.parseObject(params);
		String url = UrlTranslator.translate(BASE_SERVICE + "/roles", obj.getLong(Common.ID));
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, null, request);
		return service.invoke();
	}

	/**
	 * 资源
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/res", method = { RequestMethod.POST })
	public BsmResult res(@RequestParam(value = Common.PARAMS, required = false) String params,
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
			String url = UrlTranslator.translate(BASE_SERVICE + "/deleteRes", jsonObject.get(Common.ID));
			Map<String, Object> paramMap = MapTools.simpleMap("resId",
					JSONObject.toJSONString(jsonObject.getLong("resId")));
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
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

	/**
	 * 获取组织关联的用户列表
	 * 
	 * @param parentId
	 *            组织ID
	 * @return 数据结果集
	 */
	@RequestMapping(value = "/listByDid", method = { RequestMethod.POST })
	public BsmResult listByDid(@RequestParam(value = Common.PARENTID, required = true) Long parentId,
			HttpServletRequest request) {
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARENTID, parentId);
		RemoteService service = serviceFactory.safeBuild(SERVICE, BASE_SERVICE + "/listByDid", BoCloudMethod.OTHERS,
				null, paramMap, request);
		return service.invoke();
	}

}
