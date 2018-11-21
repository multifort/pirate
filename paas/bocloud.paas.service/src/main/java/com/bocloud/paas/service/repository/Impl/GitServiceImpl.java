package com.bocloud.paas.service.repository.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.paas.common.util.ExtendHttpClient;
import com.bocloud.paas.service.repository.GitService;
import com.bocloud.paas.service.utils.GitUtil;

@Service("gitService")
public class GitServiceImpl implements GitService {

	private GitUtil GitUtil = new GitUtil(new ExtendHttpClient());

	@Override
	public BsmResult getBranches(String url, String username, String password) {
		// 截取项目名称
		String projectName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
		// 如果是github账户，从地址获取用户名
		if (url.contains("://github.com")) {
			username = url.substring(url.indexOf("/", 8) + 1);
			username = username.substring(0, username.indexOf("/"));
		} 
		// 重新获取url路径
		url = url.substring(url.indexOf("http"), url.indexOf("/", 8));
		BsmResult bsmResult = GitUtil.getBranches(url, username, password, projectName);
		if (null != bsmResult.getData()) {
			List<SimpleBean> beans = new ArrayList<SimpleBean>();
			@SuppressWarnings("unchecked")
			List<String> branches = (List<String>) bsmResult.getData();
			for (String branche : branches) {
				beans.add(new SimpleBean("", branche, branche));
			}
			GridBean gridBean = new GridBean(1, 1, Integer.MAX_VALUE, beans);
			bsmResult.setData(gridBean);
		}
		return bsmResult;
	}
}
