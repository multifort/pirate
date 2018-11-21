package com.bocloud.paas.dao.application;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.LayoutTemplateVersion;

import java.util.List;
import java.util.Map;

public interface LayoutTemplateVersionDao extends GenericDao<LayoutTemplateVersion, Long> {

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
	public List<LayoutTemplateVersion> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception;

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
	public LayoutTemplateVersion query(Long id) throws Exception;

	/**
	 * 版本校验
	 * 
	 * @param layoutTemplateId 应用模板Id
	 * @param version 版本号
	 * @return <br/>
     *          存在：LayoutTemplateVersion 对象
     *          否则：null
	 * @throws Exception
	 */
	public LayoutTemplateVersion getTemplateVersion(String version, Long layoutTemplateId) throws Exception;
}
