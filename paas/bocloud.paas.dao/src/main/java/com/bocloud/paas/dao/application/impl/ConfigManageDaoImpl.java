package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.application.ConfigManageDao;
import com.bocloud.paas.entity.ConfigManage;

/**
 * describe: 配置管理与数据库交互层实现层
 * @author Zaney
 * @data 2017年10月17日
 */
@Repository("configManageDao")
public class ConfigManageDaoImpl extends JdbcGenericDao<ConfigManage, Long> implements ConfigManageDao {
	
	@Override
	public ConfigManage existed(Long appId, String name) throws Exception {
		String sql = "select * from config_manage where is_deleted = 0 and app_id =:appId and name =:name ";
		Map<String, Object> params = MapTools.simpleMap("appId", appId);
		params.put("name", name);
		List<ConfigManage> list = this.list(ConfigManage.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}
	
	@Override
	public ConfigManage detail(Long id) throws Exception {
		String sql = "select application.name as app_name, environment.name as env_name, user.name as creator_name, mu.name as mender_name, "
				+ "config_manage.* from config_manage "
				+ "LEFT JOIN user on user.id = config_manage.creater_id "
				+ "LEFT JOIN user mu on  mu.id = config_manage.mender_id "
				+ "LEFT JOIN application on application.id = config_manage.app_id "
				+ "LEFT JOIN environment on environment.id = application.env_id "
				+ "where config_manage.is_deleted = 0 and config_manage.id =:id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<ConfigManage> list = this.list(ConfigManage.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}
	
	@Override
	public List<ConfigManage> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select application.name as app_name, environment.name as env_name, user.name as creator_name, mu.name as mender_name, "
				+ "config_manage.* from config_manage "
				+ "LEFT JOIN user on user.id = config_manage.creater_id "
				+ "LEFT JOIN user mu on  mu.id = config_manage.mender_id "
				+ "LEFT JOIN application on application.id = config_manage.app_id "
				+ "LEFT JOIN environment on environment.id = application.env_id "
				+ "where config_manage.is_deleted = 0 and (config_manage.dept_id is null or config_manage.dept_id in (:deptId))  ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "config_manage");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.list(ConfigManage.class, sql, paramMap);
	}
	
	@Override
	public List<SimpleBean> list(List<Param> params, Map<String, String> sorter, String deptIds) throws Exception {
		String sql = "select config_manage.id, config_manage.name from config_manage "
				+ "LEFT JOIN application on application.id = config_manage.app_id "
				+ "LEFT JOIN environment on environment.id = application.env_id "
				+ "where config_manage.is_deleted = 0 and (config_manage.dept_id is null or config_manage.dept_id in (:deptId))  ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, params, sorter, "config_manage");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		List<ConfigManage> configManages = this.list(ConfigManage.class, sql, paramMap);
		List<SimpleBean> beans = new ArrayList<>();
		for (ConfigManage configManage : configManages) {
			beans.add(new SimpleBean(configManage.getId(), configManage.getName()));
		}
		return beans;
	}
	
	@Override
	public int count(List<Param> params, String deptIds) throws Exception {
		String sql = "select count(1) from config_manage "
				+ "LEFT JOIN user on user.id = config_manage.creater_id "
				+ "LEFT JOIN user mu on  mu.id = config_manage.mender_id "
				+ "LEFT JOIN application on application.id = config_manage.app_id "
				+ "LEFT JOIN environment on environment.id = application.env_id "
				+ "where config_manage.is_deleted = 0 and (config_manage.dept_id is null or config_manage.dept_id in (:deptId)) ";
		sql = SQLHelper.buildRawSql(sql, params, null, "config_manage");
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		paramMap.put("deptId", Arrays.asList(deptIds.split(",")));
		return this.countQuery(sql, paramMap).intValue();
	}
	
	@Override
	public ConfigManage detail(String name, String appName, String envName) throws Exception {
		String sql = "select application.name as app_name, environment.name as env_name, user.name as creator_name, mu.name as mender_name, "
				+ "config_manage.* from config_manage "
				+ "LEFT JOIN user on user.id = config_manage.creater_id "
				+ "LEFT JOIN user mu on  mu.id = config_manage.mender_id "
				+ "LEFT JOIN application on application.id = config_manage.app_id "
				+ "LEFT JOIN environment on environment.id = application.env_id "
				+ "where config_manage.is_deleted = 0 and config_manage.name =:name "
				+ "and application.name =:appName and environment.name =:envName ";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		params.put("appName", appName);
		params.put("envName", envName);
		List<ConfigManage> list = this.list(ConfigManage.class, sql, params);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<ConfigManage> list(Long applicationId) throws Exception {
		String sql = "select cm.* from config_manage cm "
				+ "where cm.is_deleted = 0 and cm.app_id =:applicationId ";
		sql = SQLHelper.buildSql(sql, 1, Integer.MAX_VALUE, null, null, "cm");
		Map<String, Object> params = MapTools.simpleMap("applicationId", applicationId);
		return this.list(ConfigManage.class, sql, params);
	}
	
}
