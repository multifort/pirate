package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.entity.Application;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 */
@Repository("applicationDao")
public class ApplicationDaoImpl extends JdbcGenericDao<Application, Long> implements ApplicationDao {

	@Override
	public Application query(Long id) throws Exception {
		String sql = "select cu.name as creator_name, mu.name as mender_name, " + "a.* " + "from application a "
				+ "LEFT JOIN `user` cu on cu.id = a.creater_id " + "LEFT JOIN `user` mu on mu.id = a.mender_id "
				+ "where a.is_deleted = 0 and a.id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Application> list = this.list(Application.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}
	
	@Override
	public Application detail(Long envId, String namespace) throws Exception {
		String sql = "select * from application "
				+ "where is_deleted = 0 and env_id = :envId and namespace = :namespace ";
		Map<String, Object> params = MapTools.simpleMap("envId", envId);
		params.put("namespace", namespace);
		List<Application> list = this.list(Application.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public Application query(String name) throws Exception {
		String sql = "select cu.name as creator_name, mu.name as mender_name, " + "a.* " + "from application a "
				+ "LEFT JOIN `user` cu on cu.id = a.creater_id " + "LEFT JOIN `user` mu on mu.id = a.mender_id "
				+ "where a.is_deleted = 0 and a.name = :name";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<Application> list = this.list(Application.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}
	
	@Override
	public Application query(String name, Long envId) throws Exception {
		String sql = "select * from application "
				+ "where is_deleted = 0 and name = :name and env_id = :envId ";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		params.put("envId", envId);
		List<Application> list = this.list(Application.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<Application> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds)
			throws Exception {
		String sql = "select cu.name as creator_name, mu.name as mender_name, " + "a.* " + "from application a "
				+ "LEFT JOIN `user` cu on cu.id = a.creater_id " + "LEFT JOIN `user` mu on mu.id = a.mender_id "
				+ "where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptIds)) ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
		return this.list(Application.class, sql, paramMap);
	}

	@Override
	public List<Application> listByLayout(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select cu.name as creator_name, mu.name as mender_name, " + "a.* " + "from application a "
				+ "LEFT JOIN `user` cu on cu.id = a.creater_id " + "LEFT JOIN `user` mu on mu.id = a.mender_id "
				+ "LEFT JOIN `application_layout_info` ali on ali.application_id = a.id " + "where a.is_deleted = 0 ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.list(Application.class, sql, paramMap);
	}

	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select a.id,a.name from application a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptIds)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "a");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
		List<Application> applications = this.list(Application.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<>();
		for (Application application : applications) {
			beans.add(new SimpleBean(application.getId(), application.getName()));
		}
		return beans;
	}
	
	@Override
	public List<Application> select(String deptIds) throws Exception {
		String sql = "select a.* from application a where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptIds)) ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, null, null, "a");
		Map<String, Object> paramMap = MapTools.simpleMap("deptIds", Arrays.asList(deptIds.split(",")));
		return this.list(Application.class, sql, paramMap);
	}

	@Override
	public int count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from application a " + "LEFT JOIN `user` cu on cu.id = a.creater_id "
				+ "LEFT JOIN `user` mu on mu.id = a.mender_id " + "where a.is_deleted = 0 and (a.dept_id is null or a.dept_id in (:deptIds)) ";
		sql = SQLHelper.buildRawSql(sql, params, null, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptIds", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public int countByLayout(List<Param> params) throws Exception {
		String sql = "select count(1) from application a " + "LEFT JOIN `user` cu on cu.id = a.creater_id "
				+ "LEFT JOIN `user` mu on mu.id = a.mender_id "
				+ "LEFT JOIN `application_layout_info` ali on ali.application_id = a.id " + "where a.is_deleted = 0 ";
		sql = SQLHelper.buildRawSql(sql, params, null, "");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}

	@Override
	public boolean delete(Long id, Long userId) throws Exception {
		String sql = "update application set is_deleted = true , gmt_modify = :gmtModify ,mender_id = :menderId where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("menderId", userId);
		params.put("gmtModify", new Date());
		return this.execute(sql, params) > 0;
	}

	@Override
	public Map<String, Object> countApp(String deptId) throws Exception {
		String sql = "SELECT SUM(case when status='NOT_DEPLOY'  then 1 else 0 end) NOT_DEPLOY ,SUM(case when status='DEPLOY' then 1 else 0 end) DEPLOY "
				+ "FROM application where is_deleted = 0 ";
		Map<String, Object> params = null;
		if (null != deptId) {
			sql += "and (dept_id is null or dept_id in (:deptId)) ";
			params = MapTools.simpleMap("deptId", Arrays.asList(deptId.split(",")));
		}
		List<Map<String, Object>> list = this.list(sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public boolean deleteAppLayoutInfo(Long appId) throws Exception {
		String sql = "delete from application_layout_info where application_id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", appId);
		return this.execute(sql, params) >= 0;
	}

	@Override
	public boolean deleteAppImageInfo(Long appId) throws Exception {
		String sql = "delete from application_image_info where application_id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", appId);
		return this.execute(sql, params) >= 0;
	}

	@Override
	public boolean delateAppClusterInfo(Long appId) throws Exception {
		String sql = "delete from application_openshift_cluster_info where application_id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", appId);
		return this.execute(sql, params) >= 0;
	}

	@Override
	public List<Application> queryAll(String deptIds) throws Exception {
		String sql = "select * from application where is_deleted = 0 and (dept_id is null or dept_id in (:deptId)) ";
		Map<String,Object> params = MapTools.simpleMap("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(Application.class, sql, params);
	}

	@Override
	public List<Application> queryByEnvId(Long envId) throws Exception {
		String sql = "select * from application where is_deleted = 0 and env_Id = :envId";
		Map<String,Object> params = MapTools.simpleMap("envId", envId);
		return this.list(Application.class, sql, params);
	}

}
