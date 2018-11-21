package com.bocloud.paas.service.statistic;

import java.util.concurrent.Callable;
import com.bocloud.common.model.BaseResult;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.service.application.util.MonitorClient;
import com.bocloud.paas.service.environment.EnvironmentService;

/**
 * 应用列表线程池任务类
 * describe:
 * @author Zaney
 * @data 2017年8月17日
 */
public class AppTask implements Callable<BaseResult<Application>> {
	private Application application;
	
	private MonitorClient monitorClient;
	
	private EnvironmentService environmentService;
	
	private AppClient appClient;

	public AppTask(Application application, MonitorClient monitorClient, EnvironmentService environmentService) {
		super();
		this.application = application;
		this.monitorClient = monitorClient;
		this.environmentService = environmentService;
		this.appClient = new AppClient();
	}

	@Override
	public BaseResult<Application> call() throws Exception {
		Application app = appClient.getApplication(application, monitorClient, environmentService);
		return new BaseResult<Application>(true, "获取成功", app);
	}
	
}
