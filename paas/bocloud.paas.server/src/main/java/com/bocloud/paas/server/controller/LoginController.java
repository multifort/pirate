package com.bocloud.paas.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.paas.service.user.UserService;

/**
 * 登录控制器
 * 
 * @author dmw
 *
 */
@RestController
public class LoginController {
	@Autowired
	private UserService userService;

	/**
	 * 用户登录
	 * 
	 * @param username
	 *            用户账号
	 * @param password
	 *            用户密码
	 * @return 处理结果
	 */
	@RequestMapping(value = "/login", method = { RequestMethod.POST })
	public BsmResult login(@RequestParam(value = Common.USERNAME, required = true) String username,
			@RequestParam(value = Common.PASSWORD, required = true) String password,
			@RequestParam(value = Common.SESSIONID) String sessionId) {
		return userService.login(username, password, sessionId);
	}

	@RequestMapping(value = "/logout", method = { RequestMethod.POST })
	public BsmResult logout(@RequestParam(value = Common.ID, required = true) Long id) {
		return userService.logout(id);
	}
}
