package com.bocloud.paas.server.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bocloud.paas.service.resource.HostService;

@Component
public class HostDaemon {

	@Autowired
	private HostService hostService;
	@Scheduled(cron = "${host.monitor.schedule:0 0/1 * * * ?}")
	public void hostDaemon(){
		hostService.monitor();
	}
}
