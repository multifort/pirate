package com.bocloud.paas.dao.user.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.paas.dao.user.DepartmentDao;
import com.bocloud.paas.entity.Department;

/**
 * 组织机构DAO接口实现
 * 
 * @author dongkai
 *
 */
@Repository("departmentDao")
public class DepartmentDaoImpl extends JdbcGenericDao<Department, Long> implements DepartmentDao {

	@Override
	public List<Department> list(Long parentId, Long tenantId) throws Exception {
		if (null == parentId || parentId == 0) {
			StringBuffer sqlStr = new StringBuffer();
			sqlStr.append("select * from department where (parent_id = 0 or parent_id is null) and is_deleted = 0");
			Map<String, Object> params = new HashMap<>();
			if (null != tenantId && tenantId != 0) {
				sqlStr.append(" and tenant_id = :tenantId");
				params.put("tenantId", tenantId);
			}
			return this.list(Department.class, sqlStr.toString(), params);
		} else {
			String sql = "select * from department where parent_id = :parentId and is_deleted = 0 ";
			Map<String, Object> params = new HashMap<>();
			params.put("parentId", parentId);
			return this.list(Department.class, sql, params);
		}
	}

	@Override
	public int delete(Long id, Long menderId) throws Exception {
		String sql = "update department set is_deleted = 1, gmt_modify = :gmtModify, mender_id = :menderId where id = :id";
		Map<String, Object> params = new HashMap<>();
		params.put("gmtModify", new Date());
		params.put("menderId", menderId);
		params.put("id", id);
		return this.execute(sql, params);
	}

	@Override
	public List<Department> list(Long parentId) throws Exception {
		if (null == parentId || parentId == 0) {
			String sql = "select * from department where (parent_id = 0 or parent_id is null) and is_deleted = 0";
			return this.list(Department.class, sql);
		} else {
			String sql = "select * from department where parent_id = :parentId and is_deleted = 0";
			Map<String, Object> params = new HashMap<>();
			params.put("parentId", parentId);
			return this.list(Department.class, sql, params);
		}
	}

	@Override
	public List<Department> listByTid(Long tenantId) throws Exception {
		String sql = "select * from department where is_deleted = 0 and tenant_id = :tenantId";
		Map<String, Object> params = MapTools.simpleMap("tenantId", tenantId);
		return this.list(Department.class, sql, params);
	}

	@Override
	public Department query(Long id) throws Exception {
		String sql = "select * from department where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Department> list = this.list(Department.class, sql, params);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public Department exists(String name, Long parentId) throws Exception {
		
		List<Department> list = null;
		
		if(null != name && null != parentId ){
			String sql = "select * from department where is_deleted = 0 and name = :name and parent_id = :parentId";
			Map<String, Object> params = MapTools.simpleMap("name", name);
			params.put("parentId", parentId);
			list = this.list(Department.class, sql, params);
		}else if(null != name){
			String sql = "select * from department where is_deleted = 0 and name = :name";
			Map<String, Object> params = MapTools.simpleMap("name", name);
			list = this.list(Department.class, sql, params);
		}
		// 判断是否重复
		if (isEmpty(list)) {
			return null;
		} else {
			return list.get(0);
		}
	}
	
	private boolean isEmpty(List<Department> departments){
		return departments.isEmpty();
	}

}
