package com.bocloud.paas.dao.repository.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.repository.RepositoryDao;
/**
 * @author Zaney
 * @data:2017年3月9日
 * @describe:仓库Dao接口实现类
 */
@Repository("repositoryDao")
public class RepositoryDaoImpl extends JdbcGenericDao<com.bocloud.paas.entity.Repository, Long> implements RepositoryDao {
	
	@Override
	public com.bocloud.paas.entity.Repository query(Long id) throws Exception {
		String sql = "select * from repository where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<com.bocloud.paas.entity.Repository> list = this.list(com.bocloud.paas.entity.Repository.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return this.get(com.bocloud.paas.entity.Repository.class, id);
	}

	@Override
	public List<com.bocloud.paas.entity.Repository> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select * from repository a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(com.bocloud.paas.entity.Repository.class, sql, paramMap);
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name from repository a where a.is_deleted = 0 and a.status = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		List<com.bocloud.paas.entity.Repository> repositorys = this.list(com.bocloud.paas.entity.Repository.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<>();
		for (com.bocloud.paas.entity.Repository repository : repositorys) {
			beans.add(new SimpleBean(repository.getId(), repository.getName(), repository.getAddress()));
		}
		return beans;
	}
	
	@Override
	public List<com.bocloud.paas.entity.Repository> select(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select r.* from repository r where r.is_deleted = 0 and r.status = 0 and (r.dept_id is null or r.dept_id in (:deptId)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "r");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(com.bocloud.paas.entity.Repository.class, sql, paramMap);
	}

	@Override
	public int count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from repository a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptId))";
		sql = SQLHelper.buildRawSql(sql, params, null, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public boolean deleteById(Long id, Long userId) throws Exception {
		String sql = "update repository set is_deleted = true , gmt_modify = :gmtModify ,mender_id = :menderId where id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("menderId", userId);
		params.put("gmtModify", new Date());
		return this.execute(sql, params) > 0;
	}
	@Override
	public List<Map<String, Object>> countInfo(Long userId) throws Exception {
		String sql = "SELECT SUM(case when property=0  then 1 else 0 end) public ,SUM(case when property=1 then 1 else 0 end) private "
				+ "FROM repository where is_deleted =0";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("menderId", userId);
		List<Map<String, Object>> list = this.list(sql, params);
		return list;
	}
	
	public com.bocloud.paas.entity.Repository selectRepository(String address, Integer port, String username, String password) throws Exception {
		String sql = "select * from repository where is_deleted = 0 and address = :address and port = :port and username = :username and password = :password";
		Map<String, Object> params = MapTools.simpleMap("address", address);
		params.put("port", port);
		params.put("username", username);
		params.put("password", password);
		List<com.bocloud.paas.entity.Repository> repositories = this.list(com.bocloud.paas.entity.Repository.class, sql,params);
		if (ListTool.isEmpty(repositories)) {
			return null;
		}
		return repositories.get(0);
	}
	
	@Override
	public List<com.bocloud.paas.entity.Repository> selectRepository() throws Exception {
		String sql = "select * from repository where is_deleted = 0 ";
		return this.list(com.bocloud.paas.entity.Repository.class, sql);
	}

	@Override
	public Boolean checkName(String name) throws Exception {
		String sql = "select * from repository where is_deleted = 0 and name = :name ";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<com.bocloud.paas.entity.Repository> list = this.list(com.bocloud.paas.entity.Repository.class, sql, params);
		if (list.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public com.bocloud.paas.entity.Repository query(String name) throws Exception {
		String sql = "select * from repository where is_deleted = 0 and name = :name";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<com.bocloud.paas.entity.Repository> list = this.list(com.bocloud.paas.entity.Repository.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public com.bocloud.paas.entity.Repository query(String address, Integer port, String username, String password) throws Exception {
		String sql = "select * from repository a where a.is_deleted = 0 and address = :address and port = :port ";
		Map<String, Object> params = MapTools.simpleMap("address", address);
		params.put("port", port);
		if (!StringUtils.isEmpty(username)) {
			sql += " and username = :username";
			params.put("username", username);
		}
		if (!StringUtils.isEmpty(password)) {
			sql += " and password = :password";
			params.put("password", password);
		}
		List<com.bocloud.paas.entity.Repository> list = this.list(com.bocloud.paas.entity.Repository.class, sql, params);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

}
