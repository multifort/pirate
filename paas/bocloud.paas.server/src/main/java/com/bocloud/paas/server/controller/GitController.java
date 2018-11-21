package com.bocloud.paas.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.repository.GitService;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 */
@RestController
@RequestMapping("/git")
public class GitController {
	@Autowired
	private GitService gitService;

	/**
	 * 获取git项目所有分支信息
	 * 
	 * @param repositoryUrl
	 *            gitlab的地址
	 * @param username
	 *            git的用户名
	 * @param password
	 *            gitlab的密码
	 * @param projectName
	 *            git的项目名
	 * @param accessToken
	 *            开源中国的授权码
	 * @return
	 */
	@RequestMapping(value = "/branches", method = { RequestMethod.GET })
	@Log(name = "获取git分支")
	public BsmResult getBranches(@RequestParam(required = false) String repositoryUrl,
			@RequestParam(required = true) String username, @RequestParam(required = false) String password,
			@Value(Common.REQ_USER) RequestUser user) {
		return gitService.getBranches(repositoryUrl, username, password);
	}

}
