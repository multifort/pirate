package com.bocloud.paas.server.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bocloud.paas.service.environment.EnvironmentService;

@Component
public class EnvironmentDaemon {

	@Autowired
	private EnvironmentService environmentService;

	@Scheduled(cron = "${environment.monitor.schedule:0 0/1 * * * ?}")
	public void monitor() {
		environmentService.envMonitor();
	}

}
