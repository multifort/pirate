package com.bocloud.paas.dao.user.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.user.UserRoleDao;
import com.bocloud.paas.entity.UserRole;

/**
 * 用户-角色DAO接口实现
 * 
 * @author dongkai
 *
 */
@Repository("userRoleDao")
public class UserRoleDaoImpl extends BasicDao implements UserRoleDao {

	@Override
	public UserRole save(UserRole userRole) throws Exception {
		return (UserRole) this.saveEntity(userRole);
	}

	@Override
	public boolean update(UserRole userRole) throws Exception {
		return this.updateEntity(userRole);
	}

	@Override
	public boolean delete(UserRole userRole) throws Exception {
		return this.baseDelete(userRole);
	}

	@Override
	public List<UserRole> list(Long userId) throws Exception {
		String sql = "select * from user_role where user_id = :userId";
		Map<String, Object> params = MapTools.simpleMap("userId", userId);
		List<Object> list = this.queryForList(sql, params, UserRole.class);
		List<UserRole> result = new ArrayList<>();
		for (Object object : list) {
			result.add((UserRole) object);
		}
		return result;
	}

	@Override
	public boolean deleteByUid(Long userId) throws Exception {
		String sql = "delete from user_role where user_id = :userId";
		Map<String, Object> params = MapTools.simpleMap("userId", userId);
		return this.update(sql, params) > 0;
	}

	@Override
	public boolean deleteByRid(Long roleId) throws Exception {
		String sql = "delete from user_role where role_id = :roleId";
		Map<String, Object> params = MapTools.simpleMap("roleId", roleId);
		return this.update(sql, params) > 0;
	}

	@Override
	public List<UserRole> queryByRid(Long roleId) throws Exception {
		String sql = "select * from user_role where role_id = :roleId";
		Map<String, Object> params = MapTools.simpleMap("roleId", roleId);
		List<Object> list = this.queryForList(sql, params, UserRole.class);
		List<UserRole> result = new ArrayList<>();
		for (Object object : list) {
			result.add((UserRole) object);
		}
		return result;
	}
	
}
