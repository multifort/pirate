package com.bocloud.paas.dao.application;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Layout;

/**
 * 編排文件数据操作接口
 * 
 * @author weiwei
 * @version 1.0
 * @since 2016.12.28
 *
 */
public interface LayoutDao extends GenericDao<Layout, Long> {

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
	public List<Layout> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;

	/**
	 * 按照应用查询编排列表
	 * 
	 * @author zjm
	 * @date 2017年3月23日
	 *
	 * @param appId
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Layout> listByAppId(Long appId, int page, int rows, List<Param> params, Map<String, String> sorter)
			throws Exception;

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
	 * 按照应用查询编排简易列表
	 * 
	 * @author zjm
	 * @date 2017年3月23日
	 *
	 * @param appId
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<SimpleBean> listByAppId(Long appId, List<Param> params, Map<String, String> sorter) throws Exception;

	/**
	 * 删除
	 * 
	 * @param id
	 *            the id
	 * @param userId
	 *            the user id
	 * @return
	 * @throws Exception
	 */
	public boolean remove(Long id, Long userId) throws Exception;

	/**
	 * 数量
	 * 
	 * @param params
	 *            the params
	 * @return count
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptIds) throws Exception;

	/**
	 * 按照应用统计编排数量
	 * 
	 * @author zjm
	 * @date 2017年3月23日
	 *
	 * @param appId
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int countByAppId(Long appId, List<Param> params) throws Exception;

	/**
	 * 根据ID查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Layout query(Long id) throws Exception;

	/**
	 * 删除编排文件的应用信息
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public boolean deleteLayoutAppInfo(Long id) throws Exception;

	/**
	 * 名称校验
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Layout checkName(String name) throws Exception;
}
