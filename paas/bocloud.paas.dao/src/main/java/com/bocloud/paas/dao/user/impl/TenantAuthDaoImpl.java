package com.bocloud.paas.dao.user.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.user.TenantAuthDao;
import com.bocloud.paas.entity.TenantAuthority;

/**
 * 租户-权限DAO实现类
 * 
 * @author dongkai
 *
 */
@Repository("tenantAuthDao")
public class TenantAuthDaoImpl extends BasicDao implements TenantAuthDao {

	@Override
	public TenantAuthority save(TenantAuthority tenantAuthority) throws Exception {
		return (TenantAuthority) this.saveEntity(tenantAuthority);
	}

	@Override
	public boolean update(TenantAuthority tenantAuthority) throws Exception {
		return this.updateEntity(tenantAuthority);
	}

	@Override
	public boolean delete(TenantAuthority tenantAuthority) throws Exception {
		return this.baseDelete(tenantAuthority);
	}

	@Override
	public List<TenantAuthority> list(Long tenantId) throws Exception {
		String sql = "select * from tenant_authority where tenant_id = :tenantId";
		Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
		List<Object> list = this.queryForList(sql, params, TenantAuthority.class);
		List<TenantAuthority> result = new ArrayList<>();
		for (Object object : list) {
			result.add((TenantAuthority) object);
		}
		return result;
	}

	@Override
	public boolean deleteByTid(Long tenantId) throws Exception {
		String sql = "delete from tenant_authority where tenant_id = :tenantId";
		Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
		return this.update(sql, params) > 0;
	}

	@Override
	public boolean deleteByAuthId(Long authId) throws Exception {
		String sql = "delete from tenant_authority where auth_id = :authId";
		Map<String, Object> params = MapTools.simpleMap("authId", authId);
		return this.update(sql, params) > 0;
	}
}
