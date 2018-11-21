package com.bocloud.paas.dao.user;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.database.core.intf.GenericDao;
import com.bocloud.paas.entity.Role;

/**
 * 角色抽象DAO接口
 * 
 * @author dongkai
 *
 */
public interface RoleDao extends GenericDao<Role, Long> {

	/**
	 * 分页查询
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Role> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;

	/**
	 * 简易查询
	 * 
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception;

	/**
	 * 查询所有角色
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Role> list(Long tenantId) throws Exception;

	/**
	 * 获取角色数量
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params, String deptIds) throws Exception;

	/**
	 * 根据用户ID查询角色
	 * 
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	public List<Role> listByUid(Long userId) throws Exception;

	/**
	 * 根据租户ID查询角色
	 * 
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	public List<Role> listByTid(Long tenantId) throws Exception;

	/**
	 * 根据ID删除角色
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public boolean delete(Long id, Long userId) throws Exception;

	/**
	 * 根据租户ID删除角色
	 * 
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByTid(Long tenantId, Long userId) throws Exception;

	/**
	 * 根据ID查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Role query(Long id) throws Exception;

}
