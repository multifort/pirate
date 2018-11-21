package com.bocloud.paas.service.application.model;

import io.fabric8.kubernetes.api.model.Service;

/**
 * describe: 服务-告警策略属性，对象封装
 * @author Zaney
 * @data 2017年11月6日
 */
@SuppressWarnings("serial")
public class ServiceBean extends Service{
	private String alarmStatus;//服务实例数告警策略值
	
	public String getAlarmStatus() {
		return alarmStatus;
	}

	public void setAlarmStatus(String alarmStatus) {
		this.alarmStatus = alarmStatus;
	}

}
