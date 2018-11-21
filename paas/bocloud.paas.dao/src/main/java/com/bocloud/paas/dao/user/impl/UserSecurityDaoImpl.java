package com.bocloud.paas.dao.user.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.user.UserSecurityDao;
import com.bocloud.paas.entity.UserSecurity;

/**
 * 用户安全DAO实现
 * 
 * @author dongkai
 *
 */
@Repository("userSecurityDao")
public class UserSecurityDaoImpl extends BasicDao implements UserSecurityDao {

	@Override
	public UserSecurity getByUid(Long userId) throws Exception {
		String sql = "select * from user_security where user_id = :userId";
		Map<String, Object> params = new HashMap<>();
		params.put("userId", userId);
		List<Object> list = this.queryForList(sql, params, UserSecurity.class);
		if (null == list || list.isEmpty()) {
			return null;
		}
		return (UserSecurity) list.get(0);
	}

	@Override
	public UserSecurity save(UserSecurity userSecurity) throws Exception {
		return (UserSecurity) this.saveEntity(userSecurity);
	}

	@Override
	public boolean update(UserSecurity userSecurity) throws Exception {
		return this.updateEntity(userSecurity);
	}

	@Override
	public boolean delete(UserSecurity userSecurity) throws Exception {
		return this.baseDelete(userSecurity);
	}

	@Override
	public UserSecurity getKey(String apiKey) throws Exception {
		String sql = "select * from user_security where api_key = :apiKey";
		Map<String, Object> params = new HashMap<>();
		params.put("apiKey", apiKey);
		List<Object> list = this.queryForList(sql, params, UserSecurity.class);
		if (null == list || list.isEmpty()) {
			return null;
		}
		return (UserSecurity) list.get(0);
	}

	@Override
	public boolean deleteByTid(Long tenantId) throws Exception {
		String sql = "delete from user_security where tenant_id = :tenantId";
		Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
		return this.update(sql, params) > 0;
	}

}
