package com.bocloud.paas.dao.application;

import java.util.List;
import com.bocloud.paas.entity.ServiceAlarm;

/**
 * describe: 服务告警Dao接口
 * @author Zaney
 * @data 2017年11月6日
 */
public interface ServiceAlarmDao {
	
	public boolean insert(ServiceAlarm serviceAlarm) throws Exception;
	
	public boolean update(ServiceAlarm serviceAlarm) throws Exception;
	
	public ServiceAlarm detail(Long applicationId, String name) throws Exception;
	
	public List<ServiceAlarm> select() throws Exception;
	
	public List<ServiceAlarm> select(Long applicationId) throws Exception;
	
	public boolean delete(ServiceAlarm serviceAlarm) throws Exception;
	
}
