package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.application.ServiceRelyInfoDao;
import com.bocloud.paas.entity.ServiceRelyInfo;
/**
 * describe:服务依赖dao层实现
 * @author Zaney
 * @data 2017年8月1日
 */
@Repository("serviceRelyInfoDao")
public class ServiceRelyInfoDaoImpl extends BasicDao implements ServiceRelyInfoDao {

	@Override
	public boolean insert(ServiceRelyInfo serviceRelyInfo) throws Exception {
		return this.baseSaveEntity(serviceRelyInfo);
	}

	@Override
	public boolean delete(ServiceRelyInfo serviceRelyInfo) throws Exception {
		return this.baseDelete(serviceRelyInfo);
	}

	@Override
	public List<ServiceRelyInfo> select(String currentName, String currentNamespace) throws Exception {
		String sql = "select * from service_rely_info where current_namespace = :currentNamespace";
		Map<String, Object> paramMap = MapTools.simpleMap("currentNamespace", currentNamespace);
		if (null != currentName) {
			sql = sql +" and current_name = :currentName";
			paramMap.put("currentName", currentName);
		}
		List<Object> list = this.queryForList(sql, paramMap, ServiceRelyInfo.class);
		List<ServiceRelyInfo> result = new ArrayList<>();
		for (Object object : list) {
			result.add((ServiceRelyInfo) object);
		}
		return result;
	}
	
	@Override
	public List<ServiceRelyInfo> list(String relyName, String relyNamespace) throws Exception {
		String sql = "select * from service_rely_info where rely_namespace = :relyNamespace and rely_name = :relyName";
		Map<String, Object> paramMap = MapTools.simpleMap("relyNamespace", relyNamespace);
		paramMap.put("relyName", relyName);
		
		List<Object> list = this.queryForList(sql, paramMap, ServiceRelyInfo.class);
		List<ServiceRelyInfo> result = new ArrayList<>();
		for (Object object : list) {
			result.add((ServiceRelyInfo) object);
		}
		return result;
	}
	
}
