package com.bocloud.paas.dao.user;

import java.util.List;

import com.bocloud.paas.entity.UserRole;

/**
 * 用户-角色DAO接口
 * 
 * @author dongkai
 *
 */
public interface UserRoleDao {

	/**
	 * 根据用户ID查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public List<UserRole> list(Long userId) throws Exception;

	/**
	 * 保存
	 * 
	 * @param userRole
	 * @return
	 * @throws Exception
	 */
	public UserRole save(UserRole userRole) throws Exception;

	/**
	 * 修改
	 * 
	 * @param userRole
	 * @return
	 * @throws Exception
	 */
	public boolean update(UserRole userRole) throws Exception;

	/**
	 * 删除
	 * 
	 * @param userRole
	 * @return
	 * @throws Exception
	 */
	public boolean delete(UserRole userRole) throws Exception;

	/**
	 * 根据用户ID删除
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByUid(Long userId) throws Exception;

	/**
	 * 根据角色ID删除
	 * 
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByRid(Long roleId) throws Exception;
	
	public List<UserRole> queryByRid(Long roleId) throws Exception;

}
