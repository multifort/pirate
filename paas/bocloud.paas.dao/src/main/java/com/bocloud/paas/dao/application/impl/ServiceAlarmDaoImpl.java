package com.bocloud.paas.dao.application.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.BasicDao;
import com.bocloud.paas.dao.application.ServiceAlarmDao;
import com.bocloud.paas.entity.ServiceAlarm;

/**
 * describe: 服务告警Dao层接口实现类
 * @author Zaney
 * @data 2017年11月6日
 */
@Repository("serviceAlarmDao")
public class ServiceAlarmDaoImpl extends BasicDao implements ServiceAlarmDao {

	@Override
	public boolean insert(ServiceAlarm serviceAlarm) throws Exception {
		return this.baseSaveEntity(serviceAlarm);
	}

	@Override
	public ServiceAlarm detail(Long applicationId, String name) throws Exception {
		String sql = "select * from service_alarm where  application_id = :applicationId and name = :name ";
		Map<String, Object> paramMap = MapTools.simpleMap("applicationId", applicationId);
		paramMap.put("name", name);
		List<Object> list = this.queryForList(sql, paramMap, ServiceAlarm.class);
		if (!ListTool.isEmpty(list)) {
			return (ServiceAlarm)list.get(0);
		}
		return null;
	}

	@Override
	public boolean update(ServiceAlarm serviceAlarm) throws Exception {
		return this.updateEntity(serviceAlarm);
	}

	@Override
	public List<ServiceAlarm> select() throws Exception {
		String sql = "select * from service_alarm ";
		List<Object> list = this.queryForList(sql, null, ServiceAlarm.class);
		List<ServiceAlarm> saList = new ArrayList<>();
		if (!ListTool.isEmpty(list)) {
			for (Object object : list) {
				saList.add((ServiceAlarm)object);
			}
		}
		return saList;
	}

	@Override
	public boolean delete(ServiceAlarm serviceAlarm) throws Exception {
		return this.baseDelete(serviceAlarm);
	}

	@Override
	public List<ServiceAlarm> select(Long applicationId) throws Exception {
		String sql = "select * from service_alarm where application_id = :applicationId ";
		Map<String, Object> paramMap = MapTools.simpleMap("applicationId", applicationId);
		List<Object> list = this.queryForList(sql, paramMap, ServiceAlarm.class);
		List<ServiceAlarm> saList = new ArrayList<>();
		if (!ListTool.isEmpty(list)) {
			for (Object object : list) {
				saList.add((ServiceAlarm)object);
			}
		}
		return saList;
	}
	
}
