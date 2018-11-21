package com.bocloud.paas.dao.user.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.bocloud.common.enums.BaseStatus;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.User;

/**
 * 用户DAO接口实现
 * 
 * @author dongkai
 *
 */
@Repository("userDao")
public class UserDaoImpl extends JdbcGenericDao<User, Long> implements UserDao {

	@Override
	public boolean delete(Long id, Long userId) throws Exception {
		String sql = "update user set is_deleted = 1, gmt_modify = :gmtModify, mender_id = :menderId where id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params) > 0;
	}

	@Override
	public boolean deleteByTid(Long tenantId, Long userId) throws Exception {
		String sql = "update user set is_deleted = 1, gmt_modify = :gmtModify, mender_id = :menderId where tenant_id = :tenantId";
		Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params) > 0;
	}

	@Override
	public boolean lock(Long id, Long userId) throws Exception {
		String sql = "update user set status = :status, gmt_modify = :gmtModify, mender_id = :menderId where id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		params.put("status", BaseStatus.ABNORMAL.name());
		return this.execute(sql, params) > 0;
	}

	@Override
	public User login(String username, String password) throws Exception {
		String sql = "select * from user where username = :username and password = :password and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("username", username);
		params.put("password", password);
		List<User> list = this.list(User.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public boolean active(Long id, Long userId) throws Exception {
		String sql = "update user set status = :status, gmt_modify = :gmtModify, mender_id = :menderId  where id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		params.put("status", BaseStatus.NORMAL.name());
		return this.execute(sql, params) > 0;
	}

	@Override
	public User getByName(String username) throws Exception {
		String sql = "select * from user where username = :username and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("username", username);
		List<User> list = this.list(User.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<User> list(int page, int rows, List<Param> params, Map<String, String> sorter, Long tenantId, String deptIds) throws Exception {
		String sql = "";
		if (null != tenantId && tenantId != 0) {
			sql = "select a.*,b.name tenant_name from user a left join tenant b on a.tenant_id = b.id where a.is_deleted = 0 and a.id != 1 ";
			if (!StringUtils.isEmpty(deptIds)) {
				sql += "and (a.depart_id is null or a.depart_id in (:deptIds)) ";
			}
			sql = SQLHelper.buildSql(sql, page, rows, params, sorter, "a");
			Map<String, Object> paramMap = SQLHelper.getParam(params);
			paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
			return this.list(User.class, sql, paramMap);
		}else{
			sql = "select a.* from user a where a.is_deleted = 0 and a.id != 1 ";
			if (!StringUtils.isEmpty(deptIds)) {
				sql += "and (a.depart_id is null or a.depart_id in (:deptIds)) ";
			}
			sql = SQLHelper.buildSql(sql, page, rows, params, sorter, "a");
			Map<String, Object> paramMap = SQLHelper.getParam(params);
			paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
			return this.list(User.class, sql,paramMap);
		}
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name from user a where a.is_deleted = 0 and a.id != 1 "
				+ "and (a.depart_id is null or a.depart_id in (:deptIds)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
		List<User> users = this.list(User.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<>();
		for (User user : users) {
			beans.add(new SimpleBean(user.getId(), user.getName()));
		}
		return beans;
	}

	@Override
	public int count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from user u where u.is_deleted = 0 and u.id !=1 ";
		if (!StringUtils.isEmpty(deptIds)) {
			sql += "and (u.depart_id is null or u.depart_id in (:deptIds)) ";
		}
		sql = SQLHelper.buildSql(sql, params, null, "u");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public User query(Long id) throws Exception { 
		String sql = "select a.* from user a where a.id = :id";
		Map<String, Object> paramMap = MapTools.simpleMap("id", id);
		List<User> list = this.list(User.class, sql, paramMap);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public int tenantUserCount(Long tenantId) throws Exception {
		String sql = "select count(1) from user u where u.tenant_id = :tenant_id and u.is_deleted = 0";
		Map<String, Object> paramMap = MapTools.simpleMap("tenant_id", tenantId);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public User getByEmail(String email) throws Exception {
		String sql = "select * from user where username = :email and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap(Common.EMAIL, email);
		List<User> list = this.list(User.class, sql, params);
		if (null != list && !list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<User> list(Long tenantId) throws Exception {
		String sql = "select a.* from user a where a.is_deleted = 0 and a.tenant_id=:tenantId";
		Map<String, Object> paramMap = MapTools.simpleMap("tenantId", tenantId);
		return this.list(User.class, sql, paramMap);
	}

	@Override
	public List<User> listByDid(Long departId) throws Exception {
		String sql = "select a.* from user a where a.is_deleted = 0 and a.depart_id=:departId";
		Map<String, Object> paramMap = MapTools.simpleMap("departId", departId);
		return this.list(User.class, sql, paramMap);
	}

	@Override
	public List<User> listByTid(Long tenantId) throws Exception {
		String sql = "select a.* from user a where a.is_deleted = 0 and a.tenant_id=:tenantId";
		Map<String, Object> paramMap = MapTools.simpleMap("tenantId", tenantId);
		return this.list(User.class, sql, paramMap);
	}

	@Override
	public List<User> listByRid(Long roleId) throws Exception {
		String sql = "select a.* from user a, user_role b where a.is_deleted = 0 and a.id = b.user_id and b.role_id = :roleId";
		Map<String, Object> paramMap = MapTools.simpleMap("roleId", roleId);
		return this.list(User.class, sql, paramMap);
	}

	@Override
	public User getByUserId(String userId) throws Exception {
		String sql = "select * from user where user_id = :user_id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("user_id", userId);
		List<User> list = this.list(User.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

}
