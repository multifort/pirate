package com.bocloud.paas.dao.user;

import java.util.List;

import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Department;

/**
 * 组织机构抽象DAO接口
 * 
 * @author dongkai
 *
 */
public interface DepartmentDao extends GenericDao<Department, Long> {

	/**
	 * 根据租户ID和父ID查询
	 * 
	 * @param parentId
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public List<Department> list(Long parentId, Long tenantId) throws Exception;

	/**
	 * 根据父亲ID查询
	 * 
	 * @param parentId
	 * @return
	 * @throws Exception
	 */
	public List<Department> list(Long parentId) throws Exception;

	/**
	 * 根据租户ID查询
	 * 
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public List<Department> listByTid(Long tenantId) throws Exception;

	/**
	 * 删除组织机构
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public int delete(Long id, Long menderId) throws Exception;

	/**
	 * 根据ID查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Department query(Long id) throws Exception;

	/**
	 * 根据名称查找
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Department exists(String name, Long parentId) throws Exception;
}
