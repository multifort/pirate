package com.bocloud.paas.dao.environment;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Volume;

public interface PersistentVolumeDao extends GenericDao<Volume, Long> {

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
	public List<Volume> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;

	/**
	 * 查询在某一环境（即集群下是否已存在该存储卷）
	 * 
	 * @param name
	 * @param EnvId
	 * @return
	 */
	public List<Volume> queryByName(String name, Long EnvId) throws Exception;

	/**
	 * 根据存储id查询存储
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Volume queryById(Long id) throws Exception;

	/**
	 * 根据id删除存储
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Integer remove(Long id, Long userId) throws Exception;

	/**
	 * 根据环境ID获取存储卷
	 * 
	 * @param envId
	 * @return
	 * @throws Exception
	 */
	public List<Volume> queryByEnvId(Long envId) throws Exception;
}
