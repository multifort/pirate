package com.bocloud.paas.dao.user.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.paas.dao.user.AuthDao;
import com.bocloud.paas.entity.Authority;

/**
 * 权限DAO接口实现
 * 
 * @author dongkai
 *
 */
@Repository("authDao")
public class AuthDaoImpl extends JdbcGenericDao<Authority, Long> implements AuthDao {

	@Override
	public int delete(Long id, Long menderId) throws Exception {
		String sql = "update authority set is_deleted = 1, gmt_modify = :gmtModify, mender_id = :menderId where id = :id";
		Map<String, Object> params = new HashMap<>();
		params.put("gmtModify", new Date());
		params.put("menderId", menderId);
		params.put("id", id);
		return this.execute(sql, params);
	}

	@Override
	public List<Authority> listByRid(Long roleId) throws Exception {
		String sql = "select a.* from authority a , role_authority b where b.role_id= :roleId and a.id = b.auth_id and a.is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("roleId", roleId);
		return this.list(Authority.class, sql, params);
	}

	@Override
	public List<Authority> list() throws Exception {
		String sql = "select * from authority where is_deleted = 0";
		return this.list(Authority.class, sql);
	}

	@Override
	public List<Authority> list(Long parentId) throws Exception {
		if (null == parentId || parentId == 0) {
			String sql = "select * from authority where (parent_id = 0 or parent_id is null) and is_deleted = 0";
			return this.list(Authority.class, sql);
		} else {
			String sql = "select * from authority where parent_id = :parentId and is_deleted = 0";
			Map<String, Object> params = new HashMap<>();
			params.put("parentId", parentId);
			return this.list(Authority.class, sql, params);
		}
	}

	@Override
	public List<Authority> list(Long parentId, Long tenantId) throws Exception {
		
		if (null != parentId) {
			String sql = "select a.* from authority a where a.parent_id = "
					+ ":parentId and a.is_deleted = 0";
			Map<String, Object> params = new HashMap<>();
			params.put("parentId", parentId);
			return this.list(Authority.class, sql, params);
		} else {
			return null;
		}
		// 和租户相关暂时注释掉 by luogan
		/*if (null != tenantId && tenantId != 0) {
			String sql = "select a.* from authority a, tenant b, tenant_authority c where c.auth_id = a.id and c.tenant_id = b.id and a.parent_id = "
					+ ":parentId and b.id = :tenantId and a.is_deleted = 0";
			Map<String, Object> params = new HashMap<>();
			params.put("parentId", parentId);
			params.put("tenantId", tenantId);
			return this.list(Authority.class, sql, params);
		} else {
			return null;
		}
*/
	}

	@Override
	public List<Authority> listByTid(Long tenantId) throws Exception {
		String sql = "select a.* from authority a left join tenant_authority b on a.id = b.auth_id where b.tenant_id= :tenantId and a.is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
		return this.list(Authority.class, sql, params);
	}

	@Override
	public List<Authority> listParents() throws Exception {
		String sql = "select * from authority where parent_id = 0 and is_deleted = 0";
		return this.list(Authority.class, sql);
	}

	@Override
	public Authority query(Long id) throws Exception {
		String sql = "select * from authority where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Authority> list = this.list(Authority.class, sql, params);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

}
