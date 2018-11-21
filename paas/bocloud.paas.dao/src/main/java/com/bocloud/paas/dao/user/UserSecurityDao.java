package com.bocloud.paas.dao.user;

import com.bocloud.paas.entity.UserSecurity;

/**
 * 用户安全DAO接口
 * 
 * @author dongkai
 *
 */
public interface UserSecurityDao {

	/**
	 * 根据用户ID查询
	 * 
	 * @param Uid
	 * @return
	 * @throws Exception
	 */
	public UserSecurity getByUid(Long userId) throws Exception;

	/**
	 * 根据apiKey获取对象
	 * 
	 * @param apiKey
	 * @return
	 * @throws Exception
	 */
	public UserSecurity getKey(String apiKey) throws Exception;

	/**
	 * 保存对象
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public UserSecurity save(UserSecurity userSecurity) throws Exception;

	/**
	 * 更新
	 * 
	 * @param userSecurity
	 * @return
	 * @throws Exception
	 */
	public boolean update(UserSecurity userSecurity) throws Exception;

	/**
	 * 删除
	 * 
	 * @param userSecurity
	 * @return
	 * @throws Exception
	 */
	public boolean delete(UserSecurity userSecurity) throws Exception;

	/**
	 * 根据租户Id删除
	 * 
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByTid(Long tenantId) throws Exception;
}
