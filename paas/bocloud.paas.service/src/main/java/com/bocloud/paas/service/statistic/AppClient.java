package com.bocloud.paas.service.statistic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BaseResult;
import com.bocloud.common.model.BsmResult;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Monitor;
import com.bocloud.paas.service.application.util.MonitorClient;
import com.bocloud.paas.service.environment.EnvironmentService;

/**
 * describe: 应用列表线程池Client类
 * @author Zaney
 * @data 2017年8月17日
 */
public class AppClient {
	private static Logger logger = LoggerFactory.getLogger(AppClient.class);
	
	private MonitorClient monitorClient;
	
	private EnvironmentService environmentService;
	
	
	public Application getApplication(Application application, MonitorClient monitorClient, EnvironmentService environmentService){
		//为成员变量赋值
		this.environmentService = environmentService;
		this.monitorClient = monitorClient;
		
		//TODO当前使用情况
		application.setCurrentCpu(cpuUsage(application)+"m");
		application.setCurrentMemory(memoryUsage(application)+"Mi");
		
		return application;
	
	}
	
	//==================================监控==========================================//
	private Integer cpuUsage(Application application) {
		//获取IP和port
		Monitor cpuMonitor = getAddress(application);
		if (null == cpuMonitor) {
			return 0;
		}
		cpuMonitor.setDescripteName("cpu/usage_rate");
		cpuMonitor.setResourceType("cpu");
		cpuMonitor.setType("application");
		cpuMonitor.setStartTime("30m");
		
		return getCpuValue(cpuMonitor).intValue();
	}
	private Integer memoryUsage(Application application) {
		//获取IP和port
		Monitor memoryMonitor = getAddress(application);
		if (null == memoryMonitor) {
			return 0;
		}
		memoryMonitor.setDescripteName("memory/usage");
		memoryMonitor.setResourceType("memory");
		memoryMonitor.setType("application");
		memoryMonitor.setStartTime("30m");
		
		return getMemoryValue(memoryMonitor).intValue();
	}
	/**
	 * 数据memory处理获取列表最后一个值（也是最新的数据值）
	 * @param result
	 * @return
	 */
	private BigDecimal getMemoryValue(Monitor monitor){
		BaseResult<JSONArray> result = monitorClient.getMonitorMessage(monitor);
		if (result.isFailed()) {
			return new BigDecimal(0);
		}
		JSONArray data = result.getData();
		JSONArray array = JSONObject.parseArray(data.get(data.size()-1).toString());
		if (null == array.get(1)) {
			return new BigDecimal(0);
		} else {
			return new BigDecimal(array.get(1).toString())
					.divide(new BigDecimal(1000).multiply(new BigDecimal(1000)), 2, RoundingMode.HALF_UP);
		}
	}
	/**
	 * 数据cpu处理获取列表最后一个值（也是最新的数据值）
	 * @param monitor
	 * @return
	 */
	private BigDecimal getCpuValue(Monitor monitor){
		BaseResult<JSONArray> result = monitorClient.getMonitorMessage(monitor);
		if (result.isFailed()) {
			return new BigDecimal(0);
		}
		JSONArray data = result.getData();
		JSONArray array = JSONObject.parseArray(data.get(data.size()-1).toString());
		if (null == array.get(1)) {
			return new BigDecimal(0);
		} else {
			return new BigDecimal(array.get(1).toString());
		}
		
	}
	/**
	 * 获取监控服务地址
	 * @param monitor
	 * @return
	 */
	private Monitor getAddress(Application application){
		Monitor monitor = new Monitor();
		//获取namespace
		monitor.setNamespace(application.getNamespace());
		Long envId = application.getEnvId();
		
		BsmResult result = environmentService.monitorUrl(envId, null);
		if (result.isSuccess()) {
			JSONObject object = JSONObject.parseObject(JSONObject.toJSONString(result.getData()));
			monitor.setUrl(object.getString("url"));
			monitor.setDataSource(object.getString("dataSource"));
			return monitor;
		} else {
			logger.warn("monitor address is null");
			return null;
		}
	}
	
}
