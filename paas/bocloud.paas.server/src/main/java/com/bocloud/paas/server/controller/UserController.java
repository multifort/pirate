package com.bocloud.paas.server.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.bocloud.paas.entity.User;
import com.bocloud.paas.model.UserBean;
import com.bocloud.paas.service.user.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

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
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return userService.list(page, rows, JSONObject.parseArray(params, Param.class), sorterMap, simple,
				user);
	}

	/**
	 * 添加用户
	 * 
	 * @param params
	 *            用户属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param user
	 *            .getId() 操作者ID
	 * @return 添加结果
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = false) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			User newUser = JSONObject.parseObject(object.toJSONString(), User.class);
			newUser.setCreaterId(user.getId());
			newUser.setTenantId(user.getTenantId());
			return userService.create(newUser);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/checkUsername", method = { RequestMethod.POST })
	public BsmResult checkUsername(@RequestParam(value = Common.USERNAME, required = true) String username) {
		return userService.checkUsername(username);
	}
	
	/**
	 * 校验用户工号唯一性
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/checkUserId", method = { RequestMethod.POST })
	public BsmResult checkUserId(@RequestParam(value = Common.USERID, required = true) String userId) {
		return userService.checkUseId(userId);
	}

	/**
	 * 修改用户
	 * 
	 * @param params
	 *            用户属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param id
	 *            需要修改的用户ID
	 * @param user
	 *            .getId() 操作者ID
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user,
			HttpServletRequest request) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			UserBean bean = JSONObject.parseObject(object.toJSONString(), UserBean.class);
			return userService.modify(bean, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 移除用户
	 * 
	 * @param id
	 *            用户ID
	 * @param user
	 *            .getId() 操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/remove", method = { RequestMethod.DELETE })
	public BsmResult remove(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return userService.remove(id, user.getId());
	}

	/**
	 * 查看用户详细信息
	 * 
	 * @param id
	 *            用户ID
	 * @param user
	 *            .getId() 操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return userService.detail(id);
	}

	/**
	 * 冻结用户基本信息
	 * 
	 * @param id
	 *            用户ID
	 * @param user
	 *            .getId() 操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/lock", method = { RequestMethod.POST })
	public BsmResult lock(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return userService.lock(id, user.getId());
	}

	/**
	 * 解冻用户基本信息
	 * 
	 * @param id
	 *            用户ID
	 * @param user
	 *            .getId() 操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/active", method = { RequestMethod.POST })
	public BsmResult active(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return userService.active(id, user.getId());
	}

	/**
	 * 用户密码重置
	 * 
	 * @param id
	 *            用户ID
	 * @param user
	 *            .getId() 操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/reset", method = { RequestMethod.POST })
	public BsmResult reset(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return userService.reset(id, user.getId());
	}

	/**
	 * 旧密码验证
	 * 
	 * @param id
	 *            用户ID
	 * @param password
	 *            验证密码
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/check", method = { RequestMethod.POST })
	public BsmResult check(@PathVariable(value = Common.ID) Long id,
			@RequestParam(value = Common.PASSWORD, required = true) String password) {
		return userService.check(id, password);
	}

	/**
	 * 修改密码
	 * 
	 * @param id
	 *            用户ID
	 * @param password
	 *            修改的密码
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/change", method = { RequestMethod.POST })
	public BsmResult change(@PathVariable(value = Common.ID) Long id,
			@RequestParam(value = Common.PASSWORD, required = true) String password,
			@RequestParam(value = Common.OLD_PASSWORD, required = true) String oldPassword) {
		BsmResult result = userService.check(id, oldPassword);
		if (result.isSuccess()) {
			return userService.change(id, password);
		}
		return result;
	}

	/**
	 * 用户授权
	 * 
	 * @param id
	 *            用户ID
	 * @param user
	 *            .getId() 操作者ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/accredit", method = { RequestMethod.POST })
	public BsmResult accredit(@PathVariable(value = Common.ID) Long id,
			@RequestParam(value = Common.ROLES, required = true) String roles,
			@Value(Common.REQ_USER) RequestUser user) {
		return userService.accredit(id, roles, user.getId());
	}

	/**
	 * 检查用户是否异地登录
	 * 
	 * @param id
	 * @param sessionId
	 * @return
	 */
	@RequestMapping(value = "/{id}/checkUser", method = { RequestMethod.POST })
	public BsmResult checkUser(@PathVariable(value = Common.ID) Long id,
			@RequestParam(value = Common.SESSIONID, required = true) String sessionId) {
		return userService.checkUser(id, sessionId);
	}

	/**
	 * 用户的角色列表
	 * 
	 * @param id
	 *            用户ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/roles", method = { RequestMethod.POST })
	public BsmResult roles(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return userService.listRoles(id, user.getTenantId());
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
			@Value(Common.REQ_USER) RequestUser user) {
		return userService.listByDid(parentId);
	}

	/**
	 * 获取key
	 * 
	 * @param apiKey
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/key", method = { RequestMethod.POST })
	public BsmResult secKey(@RequestParam(value = "apiKey", required = true) String apiKey,
			@Value(Common.REQ_USER) RequestUser user) {
		return userService.secKey(apiKey);
	}

}
