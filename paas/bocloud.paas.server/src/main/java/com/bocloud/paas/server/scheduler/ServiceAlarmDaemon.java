package com.bocloud.paas.server.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.bocloud.paas.service.application.Service;

/**
 * describe: 服务告警健康检查
 * @author Zaney
 * @data 2017年11月6日
 */
@Component
public class ServiceAlarmDaemon {
	@Autowired
	private Service service;
	
	@Scheduled(cron = "${service_alarm.monitor.schedule}")
	public void monitor() {
		service.serviceAlarmMonitor();
	}
}
