package com.bocloud.paas.service.application.util;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BaseResult;
import com.bocloud.paas.entity.Monitor;
/**
 * @author Zaney
 * @data:2017年4月7日
 * @describe:容器平台监控任务类
 */
public class MonitorTask implements Callable<BaseResult<JSONObject>> {
	private static Logger logger = LoggerFactory.getLogger(MonitorTask.class);
	private Monitor monitor;
	private MonitorClient client;

	public MonitorTask(Monitor monitor) {
		super();
		this.monitor = monitor;
		client = new MonitorClient();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public BaseResult<JSONObject> call() throws Exception {
		switch (monitor.getResourceType()) {
		case "cpu":
			JSONObject cpuMonitor = client.cpuMonitor(monitor);
			if (null == cpuMonitor) {
				logger.error("get cpu monitor info error");
				return new BaseResult(false, "get cpu monitor info error", null);
			}
			return new BaseResult(true, "success", cpuMonitor);
		case "memory":
			JSONObject memoryMonitor = client.memoryMonitor(monitor);
			if (null == memoryMonitor) {
				logger.error("get memory monitor info error");
				return new BaseResult(false, "get memory monitor info error", null);
			}
			return new BaseResult(true, "success", memoryMonitor);
		case "networkRx":
			JSONObject networkRxMonitor = client.networkRxMonitor(monitor);
			if (null == networkRxMonitor) {
				logger.error("get networkRx monitor info error");
				return new BaseResult(false, "get networkRx monitor info error", null);
			}
			return new BaseResult(true, "success", networkRxMonitor);
		case "networkTx":
			JSONObject networkTxMonitor = client.networkTxMonitor(monitor);
			if (null == networkTxMonitor) {
				logger.error("get networkTx monitor info error");
				return new BaseResult(false, "get networkTx monitor info error", null);
			}
			return new BaseResult(true, "success", networkTxMonitor);
		case "fileUsage":
			JSONObject fileUsageMonitor = client.fileUsageMonitor(monitor);
			if (null == fileUsageMonitor) {
				logger.error("get fileUsage monitor info error");
				return new BaseResult(false, "get fileUsage monitor info error", null);
			}
			return new BaseResult(true, "success", fileUsageMonitor);
		case "fileLimit":
			JSONObject fileLimitMonitor = client.fileLimitMonitor(monitor);
			if (null == fileLimitMonitor) {
				logger.error("get fileLimit monitor info error");
				return new BaseResult(false, "get fileLimit monitor info error", null);
			}
			return new BaseResult(true, "success", fileLimitMonitor);
		default:
			logger.error("monitor type mismatching");
			return new BaseResult(false, "monitor type mismatching", null);
		}
	}

}
