package com.bocloud.paas.dao.environment;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.LoadBalance;

public interface LoadBalanceDao extends GenericDao<LoadBalance, Long> {

	/**
	 * 根据负载变量ID查询负载变量
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public List<LoadBalance> query(Long id) throws Exception;

	/**
	 * 根据名称查询负载变量的名称
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public List<LoadBalance> queryByName(String name) throws Exception;

	/**
	 * 根据id删除负载变量
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
	public List<LoadBalance> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception;
	
	/**
	 * 按照负载id查询数量
	 * 
	 * @param loadbalanceId
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int countApps(Long loadbalanceId) throws Exception;
	
	/**
	 * 按照负载id查询数量
	 * 
	 * @author zjm
	 * @date 2017年4月14日
	 *
	 * @param layoutId
	 * @return
	 * @throws Exception
	 */
	public int countApps(List<Param> params) throws Exception;
	
	/**
	 * 根据负载获取应用列表
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param deptId
	 * @return
	 * @throws Exception
	 */
	public List<Application> listApps(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception;

	
}
