package com.bocloud.paas.server.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bocloud.paas.service.resource.PersistentVolumeService;

@Component
public class PersistentVolumeDaemon {

	@Autowired
	private PersistentVolumeService persistentVolumeService;

	@Scheduled(cron = "${persistent.monitor.schedule:0 0/1 * * * ?}")
	public void monitor() {
		persistentVolumeService.pvMonitor();
	}
}
