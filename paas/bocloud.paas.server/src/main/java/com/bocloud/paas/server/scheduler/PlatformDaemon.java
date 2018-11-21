package com.bocloud.paas.server.scheduler;

import com.bocloud.paas.service.repository.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 
 * @author Zaney
 * @data:2017年3月24日
 * @describe: 平台守护类
 */
@Repository("platformDaemon")
public class PlatformDaemon {
	private static Logger logger = LoggerFactory.getLogger(PlatformDaemon.class);
	private RegistryDaemon registryDaemon = null;
	@Autowired
	private RepositoryService registryService;

	/**
	 * 启动时打开守护线程
	 */
	@PostConstruct
	public void onStart() {
		start();
	}
	
	/**
	 * 销毁时停掉守护线程
	 */
	@PreDestroy
	public void destory() {
		stop();
	}
	
	private void start() {
		if (null == registryDaemon) {
			registryDaemon = new RegistryDaemon(registryService);
		}
		registryDaemon.start();
		logger.info("registry daemon start");
	}

	private void stop() {
		if (null != registryDaemon) {
			registryDaemon.stopDeamon();
			logger.info("registry daemon stop");
		}
	}
		
}
