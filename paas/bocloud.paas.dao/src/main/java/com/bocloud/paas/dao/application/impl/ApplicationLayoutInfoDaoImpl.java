package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.bocloud.paas.dao.application.ApplicationLayoutInfoDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.ApplicationLayoutInfo;
import com.bocloud.common.model.Param;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.database.utils.SQLHelper;

/**
 * 
 * @author zjm
 * @date 2017年3月18日
 */
@Repository
public class ApplicationLayoutInfoDaoImpl extends BasicDao implements ApplicationLayoutInfoDao{

	@Override
	public boolean insert(ApplicationLayoutInfo applicationLayoutInfo) throws Exception {
		return this.baseSaveEntity(applicationLayoutInfo);
	}

	@Override
	public boolean delete(ApplicationLayoutInfo applicationLayoutInfo) throws Exception {
		return this.baseDelete(applicationLayoutInfo);
	}

	@Override
	public ApplicationLayoutInfo query(Long appId, Long layoutId) throws Exception {
		String sql = "select * from application_layout_info where "
				+ "application_id = :appId and layout_id = :layoutId";
		Map<String, Object> params = MapTools.simpleMap("appId", appId);
		params.put("layoutId", layoutId);
		List<Object> list = this.queryForList(sql, params, ApplicationLayoutInfo.class);
		List<ApplicationLayoutInfo> result = new ArrayList<>();
		for (Object object : list) {
			result.add((ApplicationLayoutInfo) object);
		}
		if (list.isEmpty()) {
			return null;
		}
		return result.get(0);
	}
	@Override
	public ApplicationLayoutInfo query(Long appId) throws Exception {
		String sql = "select * from application_layout_info where "
				+ "application_id = :appId";
		Map<String, Object> params = MapTools.simpleMap("appId", appId);
		List<Object> list = this.queryForList(sql, params, ApplicationLayoutInfo.class);
		List<ApplicationLayoutInfo> result = new ArrayList<>();
		for (Object object : list) {
			result.add((ApplicationLayoutInfo) object);
		}
		if (list.isEmpty()) {
			return null;
		}
		return result.get(0);
	}
	@Override
	public int count(List<Param> params) throws Exception {
		String sql = "select count(distinct 1) from layout "
				+ "LEFT JOIN application_layout_info on application_layout_info.layout_id = layout.id "
				+ "LEFT JOIN application on application_layout_info.application_id = application.id "
				+ "where application.is_deleted = 0 and layout.is_deleted = 0 ";
		sql = SQLHelper.buildRawSql(sql, params, null, null);
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		return this.countQuery(sql, paramMap).intValue();
	}
	@Override
	public List<Application> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception {
		String sql = "select distinct application.* from layout "
				+ "LEFT JOIN application_layout_info on application_layout_info.layout_id = layout.id "
				+ "LEFT JOIN application on application_layout_info.application_id = application.id "
				+ "where application.is_deleted = 0 and layout.is_deleted = 0 ";
		sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, null);
		Map<String, Object> paramMap = SQLHelper.getParam(params);
		List<Object> list = this.queryForList(sql, paramMap, Application.class);
		List<Application> result = new ArrayList<>();
		for (Object object : list) {
			result.add((Application) object);
		}
		return result;
	}
}
