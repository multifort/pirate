package com.bocloud.paas.dao.user.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.user.RoleDao;
import com.bocloud.paas.entity.Role;

/**
 * 角色DAO接口实现
 * 
 * @author dongkai
 *
 */
@Repository("roleDao")
public class RoleDaoImpl extends JdbcGenericDao<Role, Long> implements RoleDao {

	@Override
	public boolean delete(Long id, Long userId) throws Exception {
		String sql = "update role set is_deleted = 1, gmt_modify = :gmtModify, mender_id = :menderId where id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params) > 0;
	}

	@Override
	public List<Role> listByUid(Long userId) throws Exception {
		String sql = "select r.* from role r,user_role u where u.user_id= :userId and r.id = u.role_id and r.is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("userId", userId);
		return this.list(Role.class, sql, params);
	}

	@Override
	public List<Role> listByTid(Long tenantId) throws Exception {
		String sql = "select * from role a where a.tenant_id= :tenantId and a.is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
		return this.list(Role.class, sql, params);
	}

	@Override
	public List<Role> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select * from role where is_deleted = 0 ";
		if (!StringUtils.isEmpty(deptIds)) {
			sql += "and (dept_id is null or dept_id in (:deptIds)) ";
		}
		sql = SQLHelper.buildSql(sql, page, rows, params, sorter, "");
		Map<String, Object> param = SQLHelper.getParam(params);
		param.put("deptIds", Arrays.asList(deptIds.split(",")));
		return this.list(Role.class, sql, param);
	}

	@Override
	public int count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from role r where r.is_deleted = 0 ";
		if (!StringUtils.isEmpty(deptIds)) {
			sql += "and (r.dept_id is null or r.dept_id in (:deptIds)) ";
		}
		sql = SQLHelper.buildSql(sql, params, null, "r");
		Map<String, Object> param = SQLHelper.getParam(params);
		param.put("deptIds", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, param).intValue();
	}

	@Override
	public List<Role> list(Long tenantId) throws Exception {
		String sql = "";
		if (null != tenantId && !tenantId.equals(0L)) {
			sql = "select * from role where is_deleted = 0 and tenant_id = :tenantId";
			return this.list(Role.class, sql);
		} else {
			sql = "select * from role where is_deleted = 0";
			Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
			return this.list(Role.class, sql, params);
		}
	}

	@Override
	public boolean deleteByTid(Long tenantId, Long userId) throws Exception {
		String sql = "update role set is_deleted = 1, gmt_modify = :gmtModify, mender_id = :menderId where tenant_id = :tenantId";
		Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params) > 0;
	}

	@Override
	public Role query(Long id) throws Exception {
		String sql = "select * from role where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Role> list = this.list(Role.class, sql, params);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name from role a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptIds)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
		List<Role> roles = this.list(Role.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<>();
		for (Role role : roles) {
			beans.add(new SimpleBean(role.getId(), role.getName()));
		}
		return beans;
	}

}
