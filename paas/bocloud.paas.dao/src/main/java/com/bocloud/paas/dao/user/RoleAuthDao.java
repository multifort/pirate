package com.bocloud.paas.dao.user;

import java.util.List;

import com.bocloud.paas.entity.RoleAuthority;

/**
 * 角色-权限DAO接口
 * 
 * @author dongkai
 *
 */
public interface RoleAuthDao {

	/**
	 * 根据角色ID查询
	 * 
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public List<RoleAuthority> list(Long roleId) throws Exception;

	/**
	 * 保存
	 * 
	 * @param roleAuthority
	 * @return
	 * @throws Exception
	 */
	public RoleAuthority save(RoleAuthority roleAuthority) throws Exception;

	/**
	 * 更新
	 * 
	 * @param roleAuthority
	 * @return
	 * @throws Exception
	 */
	public boolean update(RoleAuthority roleAuthority) throws Exception;

	/**
	 * 删除
	 * 
	 * @param roleAuthority
	 * @return
	 * @throws Exception
	 */
	public boolean delete(RoleAuthority roleAuthority) throws Exception;

	/**
	 * 根据角色ID删除角色权限
	 * 
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByRid(Long roleId) throws Exception;

	/**
	 * 根据权限ID删除角色权限
	 * 
	 * @param authId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByAuthId(Long authId) throws Exception;
}
