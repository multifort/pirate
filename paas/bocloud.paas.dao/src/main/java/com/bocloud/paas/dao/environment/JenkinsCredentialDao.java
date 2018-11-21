package com.bocloud.paas.dao.environment;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.JenkinsCredential;

public interface JenkinsCredentialDao extends GenericDao<JenkinsCredential, Long> {

	/**
	 * 根据ID查询jenkins凭证
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JenkinsCredential query(Long id) throws Exception;

	/**
	 * 根据jenkins凭证id查询jenkins凭证
	 * 
	 * @param credentialId
	 * @return
	 * @throws Exception
	 */
	public JenkinsCredential query(String credentialId) throws Exception;
	
	/**
	 * 根据jenkins凭证用户名和密码查询jenkins凭证
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public List<JenkinsCredential> queryByUsernameAndPassword(String username, String password) throws Exception;

	/**
	 * 根据id删除jenkins凭证
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Integer remove(Long id, Long userId) throws Exception;

	/**
	 * 统计数量
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Integer count(List<Param> params) throws Exception;

	/**
	 * 简易列表
	 * 
	 * @param params
	 *            the params
	 * @param sorter
	 *            the sorter
	 * @return the bean
	 * @throws Exception
	 */
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter) throws Exception;

	/**
	 * 列表
	 * 
	 * @param page
	 *            the page
	 * @param rows
	 *            the rows
	 * @param params
	 *            the params
	 * @param sorter
	 *            the sorter
	 * @return the pool list
	 * @throws Exception
	 */
	public List<JenkinsCredential> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception;

}
