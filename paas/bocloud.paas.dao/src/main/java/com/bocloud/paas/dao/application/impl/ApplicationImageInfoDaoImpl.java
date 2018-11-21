package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.application.ApplicationImageInfoDao;
import com.bocloud.paas.entity.ApplicationImageInfo;

/**
 * 
 * @author zjm
 * @date 2017年3月18日
 */
@Repository("applicationImageInfoDao")
public class ApplicationImageInfoDaoImpl extends BasicDao implements ApplicationImageInfoDao{

	@Override
	public boolean insert(ApplicationImageInfo applicationImageInfo) throws Exception {
		return this.baseSaveEntity(applicationImageInfo);
	}

	@Override
	public boolean delete(ApplicationImageInfo applicationImageInfo) throws Exception {
		return this.baseDelete(applicationImageInfo);
	}

	@Override
	public ApplicationImageInfo detail(Long appId, Long imageId) throws Exception {
		String sql = "select * from application_image_info where "
				+ "application_id = :appId and image_id = :imageId ";
		Map<String, Object> params = MapTools.simpleMap("appId", appId);
		params.put("imageId", imageId);
		List<Object> list = this.queryForList(sql,params, ApplicationImageInfo.class);
		List<ApplicationImageInfo> result = new ArrayList<>();
		for (Object object : list) {
			result.add((ApplicationImageInfo) object);
		}
		if (list.isEmpty()) {
			return null;
		}
		return result.get(0);
	}

	@Override
	public boolean update(ApplicationImageInfo applicationImageInfo) throws Exception {
		return updateEntity(applicationImageInfo);
	}

	@Override
	public List<ApplicationImageInfo> select(Long appId) throws Exception {
		String sql = "select * from application_image_info where application_id = :appId";
		Map<String, Object> params = MapTools.simpleMap("appId", appId);
		List<Object> list = this.queryForList(sql,params, ApplicationImageInfo.class);
		List<ApplicationImageInfo> result = new ArrayList<>();
		for (Object object : list) {
			result.add((ApplicationImageInfo) object);
		}
		return result;
	}
}
