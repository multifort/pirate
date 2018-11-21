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
import com.bocloud.paas.entity.Role;
import com.bocloud.paas.model.RoleBean;
import com.bocloud.paas.service.user.RoleService;

@RestController
@RequestMapping("/role")
public class RoleController {

	@Autowired
	private RoleService roleService;

	/**
	 * 获取角色列表
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
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return roleService.list(page, rows, JSONObject.parseArray(params, Param.class), sorterMap, simple,
				user);
	}

	/**
	 * 添加角色
	 * 
	 * @param params
	 *            角色属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param user.getId()
	 *            操作者ID
	 * @return 添加结果
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Role role = JSONObject.parseObject(object.toJSONString(), Role.class);
			role.setCreaterId(user.getId());
			role.setTenantId(user.getTenantId());
			return roleService.create(role);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 修改角色
	 * 
	 * @param params
	 *            角色属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param id
	 *            需要修改的角色ID
	 * @param user.getId()
	 *            操作者ID
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			RoleBean bean = JSONObject.parseObject(object.toJSONString(), RoleBean.class);
			return roleService.modify(bean, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 移除角色
	 * 
	 * @param id
	 *            角色ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/remove", method = { RequestMethod.DELETE })
	public BsmResult remove(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return roleService.remove(id, user.getId());
	}

	/**
	 * 查看角色详细信息
	 * 
	 * @param id
	 *            角色ID
	 * @param user.getId()
	 *            操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return roleService.detail(id);
	}

	/**
	 * 角色授权
	 * 
	 * @param id
	 *            角色ID
	 * @param params
	 *            权限信息
	 * @param user
	 *            操作者ID
	 * @return
	 */
	@RequestMapping(value = "/{id}/accredit", method = { RequestMethod.POST })
	public BsmResult accredit(@PathVariable(value = Common.ID) Long id,
			@RequestParam(value = Common.AUTHS, required = true) String authoritys,
			@Value(Common.REQ_USER) RequestUser user) {
		return roleService.accredit(id, authoritys, user.getId());
	}

	/**
	 * 角色的权限列表
	 * 
	 * @param id
	 *            租户ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/auths", method = { RequestMethod.POST })
	public BsmResult auths(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user,
			@RequestParam(value = Common.PARENTID, required = true) Long parentId) {
		return roleService.listAuths(id, user.getTenantId(), parentId);
	}

}
