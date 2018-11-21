package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.application.ApplicationStoreDao;
import com.bocloud.paas.entity.ApplicationStore;

@Repository("applicationStoreDao")
public class ApplicationStoreDaoImpl extends BasicDao implements ApplicationStoreDao {
	
	@Override
	public boolean save(ApplicationStore applicationStore) throws Exception {
		return this.baseSaveEntity(applicationStore);
	}

	@Override
	public List<ApplicationStore> select(String name) throws Exception {
		String sql = "select * from application_store where is_deleted = 0 ";
		if (StringUtils.hasText(name)) {
			sql = sql + "and name like :name";
		}
		Map<String, Object> params = MapTools.simpleMap("name", "%"+name+"%");
		List<Object> list = this.queryForList(sql, params, ApplicationStore.class);
		List<ApplicationStore> result = new ArrayList<>();
		for (Object object : list) {
			result.add((ApplicationStore) object);
		}
		return result;
	}

	@Override
	public ApplicationStore query(String template) throws Exception {
		String sql = "select * from application_store where is_deleted = 0 and template = :template ";
		Map<String, Object> params = MapTools.simpleMap("template", template);
		List<Object> list = this.queryForList(sql, params, ApplicationStore.class);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return (ApplicationStore)list.get(0);
	}
	
	@Override
	public ApplicationStore query(Long id) throws Exception {
		String sql = "select * from application_store where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		List<Object> list = this.queryForList(sql, params, ApplicationStore.class);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return (ApplicationStore)list.get(0);
	}

	@Override
	public boolean update(Long id, Long deployNumber) throws Exception {
		String sql = "update application_store set deploy_number = :deployNumber where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("deployNumber", deployNumber);
		return this.update(sql, params) > 0;
	}
	
	@Override
	public boolean update(Long id, String version) throws Exception {
		String sql = "update application_store set version = :version where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		params.put("version", version);
		return this.update(sql, params) > 0;
	}

	@Override
	public ApplicationStore detail(String name) throws Exception {
		String sql = "select * from application_store where is_deleted = 0 and name = :name ";
		Map<String, Object> params = MapTools.simpleMap("name", name);
		List<Object> list = this.queryForList(sql, params, ApplicationStore.class);
		if (ListTool.isEmpty(list)) {
			return null;
		}
		return (ApplicationStore)list.get(0);
	}

	@Override
	public boolean delete(Long id) throws Exception {
		String sql = "update application_store set is_deleted = true where is_deleted = 0 and id = :id";
		Map<String, Object> params = MapTools.simpleMap("id", id);
		return this.update(sql, params) > 0;
	}

	@Override
	public List<ApplicationStore> selectDeployType(String deployType) throws Exception {
		String sql = "select * from application_store where is_deleted = 0 and deploy_type = :deployType";
		Map<String, Object> params = MapTools.simpleMap("deployType", deployType);
		List<Object> list = this.queryForList(sql, params, ApplicationStore.class);
		List<ApplicationStore> result = new ArrayList<>();
		for (Object object : list) {
			result.add((ApplicationStore) object);
		}
		return result;
	}

}
