package com.bocloud.paas.service.repository;

import com.bocloud.common.model.BsmResult;

public interface GitService {

	/**
	 * 获取git项目所有分支信息
	 * 
	 * @param url gitlab的地址
	 * @param username git的用户名
	 * @param password gitlab的密码
	 * @return
	 */
	public BsmResult getBranches(String url, String username, String password);
}
