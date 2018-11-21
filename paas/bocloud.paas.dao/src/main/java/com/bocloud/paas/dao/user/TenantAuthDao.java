package com.bocloud.paas.dao.user;

import java.util.List;

import com.bocloud.paas.entity.TenantAuthority;

/**
 * 租户-权限DAO接口
 * 
 * @author dongkai
 *
 */
public interface TenantAuthDao {

	/**
	 * 根据角色ID查询
	 * 
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public List<TenantAuthority> list(Long roleId) throws Exception;

	/**
	 * 保存
	 * 
	 * @param tenantAuthority
	 * @return
	 * @throws Exception
	 */
	public TenantAuthority save(TenantAuthority tenantAuthority) throws Exception;

	/**
	 * 更新
	 * 
	 * @param tenantAuthority
	 * @return
	 * @throws Exception
	 */
	public boolean update(TenantAuthority tenantAuthority) throws Exception;

	/**
	 * 删除
	 * 
	 * @param tenantAuthority
	 * @return
	 * @throws Exception
	 */
	public boolean delete(TenantAuthority tenantAuthority) throws Exception;

	/**
	 * 根据租户ID删除租户权限
	 * 
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByTid(Long tenantId) throws Exception;

	/**
	 * 根据权限ID删除租户权限
	 * 
	 * @param authId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByAuthId(Long authId) throws Exception;
}
