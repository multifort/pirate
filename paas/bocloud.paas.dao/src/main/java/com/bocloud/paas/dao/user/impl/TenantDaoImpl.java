package com.bocloud.paas.dao.user.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.user.TenantDao;
import com.bocloud.paas.entity.Tenant;

/**
 * 租户DAO接口实现
 * 
 * @author dongkai
 *
 */
@Repository("tenantDao")
public class TenantDaoImpl extends JdbcGenericDao<Tenant, Long> implements TenantDao {

	@Override
	public boolean delete(Long id, Long userId) throws Exception {
		String sql = "update tenant set is_deleted = 1, gmt_modify = :gmtModify, mender_id = :menderId where id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params) > 0;
	}

	@Override
	public boolean lock(Long id, Long userId) throws Exception {
		String sql = "update tenant set status = :status, gmt_modify = :gmtModify, mender_id = :menderId where id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		params.put("status", BaseStatus.ABNORMAL.name());
		return this.execute(sql, params) > 0;
	}

	@Override
	public boolean active(Long id, Long userId) throws Exception {
		String sql = "update tenant set status = :status, gmt_modify = :gmtModify, mender_id = :menderId  where id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		params.put("status", BaseStatus.NORMAL.name());
		return this.execute(sql, params) > 0;
	}

	@Override
	public List<Tenant> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select * from tenant where is_deleted = 0";
		sql = SQLHelper.buildSql(sql, page, rows, params, sorter, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.list(Tenant.class, sql, paramMap);
	}

	@Override
	public int count(List<Param> params) throws Exception {
		String sql = "select count(1) from tenant t where t.is_deleted = 0";
		sql = SQLHelper.buildSql(sql, params, null, "t");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public Tenant getByEmail(String email) throws Exception {
		String sql = "select * from tenant where contact_email = :email and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap(Common.EMAIL, email);
		List<Tenant> list = this.list(Tenant.class, sql, params);
		if (null != list && !list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<Tenant> listAll() throws Exception {
		String sql = "select * from tenant where is_deleted = 0";
		return this.list(Tenant.class, sql);
	}

	@Override
	public Tenant query(Long id) throws Exception {
		String sql = "select * from tenant where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap(Common.ID, id);
		List<Tenant> list = this.list(Tenant.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

}
