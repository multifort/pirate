package com.bocloud.paas.dao.environment;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Host;

public interface HostDao extends GenericDao<Host, Long> {

	/**
	 * 根据id查询主机
	 * 
	 * @param id
	 * @return
	 */
	public Host queryById(Long id) throws Exception;

	/**
	 * 根据ip查询主机
	 * 
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public List<Host> queryByIp(String ip) throws Exception;

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
	public List<Host> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;

	/**
	 * 根据envId查询主机
	 * 
	 * @param envId
	 * @return
	 */
	public List<Host> queryByEnvId(Long envId) throws Exception;

	/**
	 * 查询不在环境中的主机
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Host> queryHostNotInEnv() throws Exception;

	/**
	 * 根据名称查询主机
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Host> queryByName(String hostname) throws Exception;

	/**
	 * 根据id删除主机
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Integer remove(Long id, Long userId) throws Exception;

	/**
	 * 查询状态正常的独立主机（不存在于环境中）
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Host> queryNormalHost(Long userId) throws Exception;

	/**
	 * 查询所有的主机
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Host> queryAll() throws Exception;

}
