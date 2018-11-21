package com.bocloud.paas.service.environment;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.paas.entity.JenkinsCredential;

public interface JenkinsCredentialService {

	/**
	 * 创建jenkins凭证
	 * 
	 * @param params
	 * @param userId
	 * @return
	 */
	public BsmResult create(JenkinsCredential jenkinsCredential, Long userId);

	/**
	 * 删除jenkins凭证
	 * 
	 * @param params
	 * @param userId
	 * @return
	 */
	public BsmResult remove(List<Long> ids, Long userId);

	/**
	 * 查询jenkins凭证
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple);

	/**
	 * 更改jenkins凭证
	 * 
	 * @param params
	 * @param userId
	 * @return
	 */
	public BsmResult modify(JenkinsCredential jenkinsCredential, Long userId);

	/**
	 * 查询jenkins凭证详情
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public BsmResult detail(Long id);
	
	/**
	 * 查询jenkins凭证详情
	 * 
	 * @param credentialId
	 * @return
	 * @throws Exception
	 */
	public BsmResult queryByCredentialId(String credentialId);
	
	/**
	 * 根据凭证用户名和密码查询jenkins凭证id
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public BsmResult queryCredentialId(String username, String password, Long userId);

}
