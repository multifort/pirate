package com.bocloud.paas.dao.environment.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.entity.Environment;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import org.springframework.util.StringUtils;

@Service("environmentDao")
public class EnvironmentDaoImpl extends JdbcGenericDao<Environment, Long> implements EnvironmentDao {

	@Override
	public Environment query(Long id) throws Exception {
		String sql = "select * from environment a where a.id = :id and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Environment> environments = this.list(Environment.class, sql, params);
		if(ListTool.isEmpty(environments)){
			return null;
		}
		return environments.get(0);
	}

	@Override
	public List<Environment> queryByName(String name) throws Exception {
		String sql = "select * from environment a where a.name = :name and is_deleted = 0";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<Environment> environments = this.list(Environment.class, sql, params);
		return environments;
	}

	@Override
	public Integer remove(Long id, Long userId) throws Exception {
		String sql = "update environment set is_deleted = true , gmt_modify = :gmtModify where is_deleted = 0 and id = :id ";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("gmtModify", new Date());
		params.put("menderId", userId);
		return this.execute(sql, params);
	}

	@Override
	public Integer count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from environment a where is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptIds))";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name from environment a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptIds)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
		List<Environment> environments = this.list(Environment.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<SimpleBean>();
		for (Environment environment : environments) {
			beans.add(new SimpleBean(environment.getId(), environment.getName()));
		}
		return beans;
	}

	@Override
	public List<Environment> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptId) throws Exception {
		String sql = "select * from environment a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId",  Arrays.asList(deptId.split(",")));
		return this.list(Environment.class, sql, paramMap);
	}

	@Override
	public List<Environment> queryNormalEnv(String deptIds) throws Exception {
		String sql = "SELECT * from (SELECT * from environment where (status='2' or status='4') and (dept_id is null or dept_id in (:deptIds))) as tempenvtable where  tempenvtable.is_deleted = 0 and tempenvtable.source='create'";
		Map<String, Object> params = MapTools.simpleMap("deptIds", deptIds);
		List<Environment> environments = this.list(Environment.class, sql, params);
		return environments;
	}

	@Override
	public List<Environment> queryAll(String deptIds) throws Exception {
		StringBuilder sqlBuilder = new StringBuilder("select * from environment a where a.is_deleted = 0 ");
		Map<String, Object> params = null;
		if (!StringUtils.isEmpty(deptIds)) {
			sqlBuilder.append("and (dept_id is null or dept_id in (:deptIds))");
			params = MapTools.simpleMap("deptIds", Arrays.asList(deptIds.split(",")));
		}
		
		return this.list(Environment.class, sqlBuilder.toString(), params);
	}

	@Override
	public List<Environment> queryActiveEnv(String deptIds) throws Exception {
		String sql = "select * from environment a where a.is_deleted = '0' and status = '2' ";
		Map<String, Object> params = null;
		if (!StringUtils.isEmpty(deptIds)) {
			sql += "and (dept_id is null or dept_id in (:deptIds)) ";
			params = MapTools.simpleMap("deptIds", Arrays.asList(deptIds.split(",")));
		}
		return this.list(Environment.class, sql, params);
	}

	@Override
	public Environment queryByHostId(Long hostId) throws Exception {
		String sql = "select environment.* from environment "
				+ "LEFT JOIN host on host.env_id = environment.id "
				+ "where host.id = :hostId ";
		Map<String, Object> params = MapTools.simpleMap("hostId", hostId);
		List<Environment> environments = this.list(Environment.class, sql, params);
		if(ListTool.isEmpty(environments)){
			return null;
		}
		return environments.get(0);
	}

}
