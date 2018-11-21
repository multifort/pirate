package com.bocloud.paas.dao.environment;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Environment;

public interface EnvironmentDao extends GenericDao<Environment, Long> {

	/**
	 * 根据环境变量ID查询环境变量
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Environment query(Long id) throws Exception;

	/**
	 * 根据名称查询环境变量的名称
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public List<Environment> queryByName(String name) throws Exception;

	/**
	 * 根据id删除环境变量
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
	public Integer count(List<Param> params, String deptIds) throws Exception;

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
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;

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
	public List<Environment> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;

	/**
	 * 查询状态正常的环境
	 * 
	 * @param deptIds
	 * @return
	 * @throws Exception
	 */
	public List<Environment> queryNormalEnv(String deptIds) throws Exception;

	/**
	 * 查询激活状态的环境
	 * 
	 * @param deptIds
	 * @return
	 * @throws Exception
	 */
	public List<Environment> queryActiveEnv(String deptIds) throws Exception;

	/**
	 * 查询所有的环境，对环境状态进行监控
	 * 
	 * @return
	 */
	public List<Environment> queryAll(String deptIds) throws Exception;
	/**
	 * 根据主机ID获取环境信息
	 * @param hostId
	 * @return
	 * @throws Exception
	 */
	public Environment queryByHostId(Long hostId) throws Exception;

}
