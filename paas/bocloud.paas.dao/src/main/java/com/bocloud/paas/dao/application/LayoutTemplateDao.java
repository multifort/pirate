package com.bocloud.paas.dao.application;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.LayoutTemplate;

import java.util.List;
import java.util.Map;

public interface LayoutTemplateDao extends GenericDao<LayoutTemplate, Long> {

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
	public List<LayoutTemplate> list(int page, int rows, List<Param> params,
                                     Map<String, String> sorter) throws Exception;

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
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter)
			throws Exception;

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
	public int count(List<Param> params) throws Exception;

	/**
	 * 根据ID查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public LayoutTemplate query(Long id) throws Exception;

	/**
	 * 名称校验
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public LayoutTemplate checkName(String name) throws Exception;
	
	/**
	 * 无分页查询列表
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public List<LayoutTemplate> getList() throws Exception;
	
}
