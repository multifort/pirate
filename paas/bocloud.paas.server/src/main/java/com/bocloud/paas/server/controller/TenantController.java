package com.bocloud.paas.server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.Tenant;
import com.bocloud.paas.model.TenantBean;
import com.bocloud.paas.service.user.TenantService;

@RestController
@RequestMapping("/tenant")
public class TenantController {

	@Autowired
	private TenantService tenantService;

	/**
	 * 获取用户列表
	 * 
	 * @param page
	 *            当前页码
	 * @param rows
	 *            页面数据大小
	 * @param params
	 *            查询参数，例如：[{"param":{"name":"aaa","password":"1245"},"sign":
	 *            "EQ|UEQ"},{"param":{"name":"aaa","password":"1245"},"sign":
	 *            "EQ|UEQ"}]
	 * @param sorter
	 *            排序参数，例如：{"name":0|1,"password":0|1},0表示增序，1表示降序
	 * @param simple
	 *            简单查询标记，只有true和false,为false时返回用户的详细信息，为true时只返回id和name值。
	 * @return 数据结果集
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple) {
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return tenantService.list(page, rows, JSONObject.parseArray(params, Param.class), sorterMap, simple);
	}

	/**
	 * 添加用户
	 * 
	 * @param params
	 *            用户属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param user.getId()
	 *            操作者ID
	 * @return 添加结果
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = false) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			String auths = object.getString(Common.AUTHS);
			object.remove(Common.AUTHS);
			Tenant tenant = JSONObject.parseObject(object.toJSONString(), Tenant.class);
			tenant.setCreaterId(user.getId());
			return tenantService.create(tenant, auths);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/checkTenant", method = { RequestMethod.POST })
	public BsmResult checkTenant(@RequestParam(value = Common.EMAIL, required = true) String email) {
		return tenantService.checkTenant(email);
	}

	/**
	 * 修改用户
	 * 
	 * @param params
	 *            用户属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param id
	 *            需要修改的用户ID
	 * @param user.getId()
	 *            操作者ID
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			TenantBean tenant = JSONObject.parseObject(object.toJSONString(), TenantBean.class);
			return tenantService.modify(tenant, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 移除用户
	 * 
	 * @param id
	 *            用户ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/remove", method = { RequestMethod.DELETE })
	public BsmResult remove(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return tenantService.remove(id, user.getId());
	}

	/**
	 * 查看用户详细信息
	 * 
	 * @param id
	 *            用户ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return tenantService.detail(id);
	}

	/**
	 * 冻结用户基本信息
	 * 
	 * @param id
	 *            用户ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/lock", method = { RequestMethod.POST })
	public BsmResult lock(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return tenantService.lock(id, user.getId());
	}

	/**
	 * 解冻用户基本信息
	 * 
	 * @param id
	 *            用户ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/active", method = { RequestMethod.POST })
	public BsmResult active(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return tenantService.active(id, user.getId());
	}

	/**
	 * 重置密码
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/reset", method = { RequestMethod.POST })
	public BsmResult reset(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return tenantService.reset(id, user.getId());
	}

	/**
	 * 用户授权
	 * 
	 * @param id
	 *            用户ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/accredit", method = { RequestMethod.POST })
	public BsmResult accredit(@PathVariable(value = Common.ID) Long id,
			@RequestParam(value = Common.AUTHS, required = true) String auths,
			@Value(Common.REQ_USER) RequestUser user) {
		return tenantService.accredit(id, auths, user.getId());
	}

	/**
	 * 租户的用户列表
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 */
	@RequestMapping(value = "/{id}/users", method = { RequestMethod.GET })
	public BsmResult users(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter) {
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return tenantService.listUsers(page, rows, JSONObject.parseArray(params, Param.class), sorterMap);
	}

	/**
	 * 租户的权限列表
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 */
	@RequestMapping(value = "/{id}/roles", method = { RequestMethod.GET })
	public BsmResult roles(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter) {
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return tenantService.listRoles(page, rows, JSONObject.parseArray(params, Param.class), sorterMap);
	}

	/**
	 * 租户的权限列表
	 * 
	 * @param id
	 *            租户ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/auths", method = { RequestMethod.POST })
	public BsmResult auths(@PathVariable(value = Common.ID) Long id,
			@RequestParam(value = Common.PARENTID, required = true) Long parantId) {
		return tenantService.listAuths(id, parantId);
	}

}
