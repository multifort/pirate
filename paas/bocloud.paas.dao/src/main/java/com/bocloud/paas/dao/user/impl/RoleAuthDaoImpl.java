package com.bocloud.paas.dao.user.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.user.RoleAuthDao;
import com.bocloud.paas.entity.RoleAuthority;

/**
 * 角色-权限DAO接口实现
 * 
 * @author dongkai
 *
 */
@Repository("roleAuthDao")
public class RoleAuthDaoImpl extends BasicDao implements RoleAuthDao {

	@Override
	public RoleAuthority save(RoleAuthority roleAuthority) throws Exception {
		return (RoleAuthority) this.saveEntity(roleAuthority);
	}

	@Override
	public boolean update(RoleAuthority roleAuthority) throws Exception {
		return this.updateEntity(roleAuthority);
	}

	@Override
	public boolean delete(RoleAuthority roleAuthority) throws Exception {
		return this.baseDelete(roleAuthority);
	}

	@Override
	public List<RoleAuthority> list(Long roleId) throws Exception {
		String sql = "select * from role_authority where role_id = :roleId";
		Map<String, Object> params = MapTools.simpleMap("roleId", roleId);
		List<Object> list = this.queryForList(sql, params, RoleAuthority.class);
		List<RoleAuthority> result = new ArrayList<>();
		for (Object object : list) {
			result.add((RoleAuthority) object);
		}
		return result;
	}

	@Override
	public boolean deleteByRid(Long roleId) throws Exception {
		String sql = "delete from role_authority where role_id = :roleId";
		Map<String, Object> params = MapTools.simpleMap("roleId", roleId);
		return this.update(sql, params) > 0;
	}

	@Override
	public boolean deleteByAuthId(Long authId) throws Exception {
		String sql = "delete from role_authority where auth_id = :authId";
		Map<String, Object> params = MapTools.simpleMap("authId", authId);
		return this.update(sql, params) > 0;
	}
}
