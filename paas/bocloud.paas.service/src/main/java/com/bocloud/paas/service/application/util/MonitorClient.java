package com.bocloud.paas.service.application.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BaseResult;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.entity.Monitor;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
/**
 * @author Zaney
 * @data:2017年4月7日
 * @describe:容器平台监控client类
 */
@Repository("monitorClient")
public class MonitorClient {
	private static Logger logger = LoggerFactory.getLogger(MonitorClient.class);


	//*==========================cpu/memory/network 监控数据信息格式处理====================================*//
	
	public JSONObject memoryMonitor(Monitor monitor){
		JSONObject data = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		JSONArray values = new JSONArray();
		// 1、获取memory监控
		JSONObject memoryObject = new JSONObject();
		JSONObject memoryMonitor = getMemoryMonitor(monitor);
		memoryObject.put("name", "Memory");
		memoryObject.put("data", memoryMonitor.get("memorys"));
		values.add(memoryObject);
		jsonObject.put("keys", memoryMonitor.get("dates"));// 时间轴
		jsonObject.put("values", values);
		data.put(String.valueOf(Monitor.Type.MEMORY), jsonObject);
		data.put("type", Monitor.Type.MEMORY);
		return data;
	}
	public JSONObject cpuMonitor(Monitor monitor){
		JSONObject data = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		JSONArray values = new JSONArray();
		// 1、获取cpu监控
		JSONObject cpuObject = new JSONObject();
		JSONArray cpuMonitor = getCpuMonitor(monitor);
		cpuObject.put("name", "CPU");
		cpuObject.put("data", cpuMonitor);
		values.add(cpuObject);
		jsonObject.put("values", values);
		data.put(String.valueOf(Monitor.Type.CPU), jsonObject);
		data.put("type", Monitor.Type.CPU);
		return data;
	}
	public JSONObject networkRxMonitor(Monitor monitor){
		JSONObject data = new JSONObject();
		// 1、获取networkRx监控，Received
		JSONObject networkRx = new JSONObject();
		JSONArray networkMonitorRx = getNetworkRxMonitor(monitor);
		networkRx.put("name", "Received");
		networkRx.put("data", networkMonitorRx);
		data.put(String.valueOf(Monitor.Type.NETWORKRX), networkRx);
		data.put("type", Monitor.Type.NETWORKRX);
		return data;
	}
	public JSONObject networkTxMonitor(Monitor monitor){
		JSONObject data = new JSONObject();
		// 1、获取networkTx监控，Send
		JSONObject networkTx = new JSONObject();
		JSONArray networkMonitorTx = getNetworkTxMonitor(monitor);
		networkTx.put("name", "Send");
		networkTx.put("data", networkMonitorTx);
		data.put(String.valueOf(Monitor.Type.NETWORKTX), networkTx);
		data.put("type", Monitor.Type.NETWORKTX);
		return data;
	}
	
	public JSONObject fileUsageMonitor(Monitor monitor){
		JSONObject data = new JSONObject();
		// 1、获取fileUsage监控
		JSONObject usageObject = new JSONObject();
		JSONArray usageArray = getFileUsageMonitor(monitor);
		usageObject.put("name", "Usage");
		usageObject.put("data", usageArray);
		data.put(String.valueOf(Monitor.Type.FILEUSAGE), usageObject);
		data.put("type", Monitor.Type.FILEUSAGE);
		return data;
	}
	
	public JSONObject fileLimitMonitor(Monitor monitor){
		JSONObject data = new JSONObject();
		// 1、获取fileUsage监控
		JSONObject limitObject = new JSONObject();
		JSONArray limitArray = getFileLimitMonitor(monitor);
		limitObject.put("name", "Limit");
		limitObject.put("data", limitArray);
		data.put(String.valueOf(Monitor.Type.FILELIMIT), limitObject);
		data.put("type", Monitor.Type.FILELIMIT);
		return data;
	}
	
	// *=============================pod的cpu/memory/network监控信息数据处理============================*//
	
	private JSONObject getMemoryMonitor(Monitor memoryMonitor) {
		JSONObject object = new JSONObject();
		JSONArray memorys = new JSONArray();
		JSONArray dates = new JSONArray();
		BaseResult<JSONArray> monitorMessage = null;
		try {
			monitorMessage = getMonitorMessage(memoryMonitor);
			if (monitorMessage.isSuccess()) {
				// 获取存储信息
				JSONArray messages = monitorMessage.getData();
				for (Object message : messages) {
					BigDecimal avg = new BigDecimal(0);
					JSONArray obj = JSONObject.parseArray(message.toString());
					// 统计时间
					dates.add(obj.get(0));
					// 计算内存信息
					if (null != obj.get(1)) {
						avg = new BigDecimal(obj.get(1).toString());
					}
					avg = avg.divide(new BigDecimal(1000).multiply(new BigDecimal(1000)), 2, RoundingMode.HALF_UP);
					memorys.add(avg);
				}
			}
			if (monitorMessage.isSuccess() && ListTool.isEmpty(memorys)) {
				for (int j = 0; j < 30; j++) {
					memorys.add(0.00);
				}
			}
			logger.warn(monitorMessage.getMessage());
			object.put("dates", dates);
			object.put("memorys", memorys);
		} catch (Exception e) {
			logger.error("初步获取memory监控信息失败 ", e);
		}
		return object;
	}

	private JSONArray getCpuMonitor(Monitor cpuMonitor) {
		JSONArray cpus = new JSONArray();
		BaseResult<JSONArray> monitorMessage = null;
		try {
			monitorMessage = getMonitorMessage(cpuMonitor);
			if (monitorMessage.isSuccess()) {
				JSONArray messages = monitorMessage.getData();
				for (int i = 0; i < messages.size(); i++) {
					BigDecimal avg = new BigDecimal(0);
					JSONArray cpu = JSONObject.parseArray(messages.get(i).toString());
					if (null != cpu.get(1)) {
						avg = new BigDecimal(cpu.get(1).toString());
					}
					cpus.add(avg);
				}
			}
			if (monitorMessage.isSuccess() && ListTool.isEmpty(cpus)) {
				for (int j = 0; j < 30; j++) {
					cpus.add(0.00);
				}
			}
			logger.warn(monitorMessage.getMessage());
		} catch (Exception e) {
			logger.error("初步获取cpu监控信息失败 ", e);
		}
		return cpus;
	}

	private JSONArray getNetworkTxMonitor(Monitor netTxMonitor) {
		JSONArray networks = new JSONArray();
		BaseResult<JSONArray> networkTx = null;
		try {
			networkTx = getMonitorMessage(netTxMonitor);
			// tx
			if (networkTx.isSuccess()) {
				JSONArray objTx = networkTx.getData();
				for (int i = 0; i < objTx.size(); i++) {
					BigDecimal firstAvg = new BigDecimal(0);
					JSONArray firstObj = JSONObject.parseArray(objTx.get(i).toString());
					if (null != firstObj.get(1)) {
						firstAvg = new BigDecimal(firstObj.get(1).toString()).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
					}
					networks.add(firstAvg);
				}
			}
			if (networkTx.isSuccess() && ListTool.isEmpty(networks)) {
				for (int j = 0; j < 30; j++) {
					networks.add(0.00);
				}
			}
			logger.warn(networkTx.getMessage());
		} catch (Exception e) {
			logger.error("初步获取network监控信息失败 ", e);
		}
		return networks;
	}

	private JSONArray getNetworkRxMonitor(Monitor netRxMonitor) {
		JSONArray networks = new JSONArray();
		BaseResult<JSONArray> networkRx = null;
		try {
			networkRx = getMonitorMessage(netRxMonitor);
			// rx
			if (networkRx.isSuccess()) {
				JSONArray objRx = networkRx.getData();
				for (int i = 0; i < objRx.size(); i++) {
					JSONArray firstObj = JSONObject.parseArray(objRx.get(i).toString());
					BigDecimal firstAvg = new BigDecimal(0);
					if (null != firstObj.get(1)) {
						firstAvg = new BigDecimal(firstObj.get(1).toString()).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
					}
					networks.add(firstAvg);
				}
			}
			if (networkRx.isSuccess() && ListTool.isEmpty(networks)) {
				for (int j = 0; j < 30; j++) {
					networks.add(0.00);
				}
			}
			logger.warn(networkRx.getMessage());
		} catch (Exception e) {
			logger.error("初步获取network监控信息失败 ", e);
		}
		return networks;
	}
	
	private JSONArray getFileUsageMonitor(Monitor fileUsageMonitor) {
		JSONArray usageArray = new JSONArray();
		BaseResult<JSONArray> fileUsage = null;
		try {
			fileUsage = getMonitorMessage(fileUsageMonitor);
			// fileUsage
			if (fileUsage.isSuccess()) {
				JSONArray objUsage = fileUsage.getData();
				for (int i = 0; i < objUsage.size(); i++) {
					JSONArray firstObj = JSONObject.parseArray(objUsage.get(i).toString());
					BigDecimal firstAvg = new BigDecimal(0);
					if (null != firstObj.get(1)) {
						firstAvg = new BigDecimal(firstObj.get(1).toString()).divide(new BigDecimal((int)Math.pow(1000,3)), 2, RoundingMode.HALF_UP);
					}
					usageArray.add(firstAvg);
				}
			}
			if (fileUsage.isSuccess() && ListTool.isEmpty(usageArray)) {
				for (int j = 0; j < 30; j++) {
					usageArray.add(0.00);
				}
			}
			logger.warn(fileUsage.getMessage());
		} catch (Exception e) {
			logger.error("初步获取节点使用磁盘空间监控信息失败 ", e);
		}
		return usageArray;
	}
	
	private JSONArray getFileLimitMonitor(Monitor fileLimitMonitor) {
		JSONArray limitArray = new JSONArray();
		BaseResult<JSONArray> fileLimit = null;
		try {
			fileLimit = getMonitorMessage(fileLimitMonitor);
			// rx
			if (fileLimit.isSuccess()) {
				JSONArray objLimit = fileLimit.getData();
				for (int i = 0; i < objLimit.size(); i++) {
					JSONArray firstObj = JSONObject.parseArray(objLimit.get(i).toString());
					BigDecimal firstAvg = new BigDecimal(0);
					if (null != firstObj.get(1)) {
						firstAvg = new BigDecimal(firstObj.get(1).toString()).divide(new BigDecimal((int)Math.pow(1000,3)), 2, RoundingMode.HALF_UP);
					}
					limitArray.add(firstAvg);
				}
			}
			if (fileLimit.isSuccess() && ListTool.isEmpty(limitArray)) {
				for (int j = 0; j < 30; j++) {
					limitArray.add(0.00);
				}
			}
			logger.warn(fileLimit.getMessage());
		} catch (Exception e) {
			logger.error("初步获取节点拥有的磁盘空间监控信息失败 ", e);
		}
		return limitArray;
	}

	/**
	 * url
	 * @param monitor
	 * @return
	 */
	public BaseResult<JSONArray> getMonitorMessage(Monitor monitor) {
		JSONArray values = new JSONArray();
		
		//获取sql
		String sql = sql(monitor);
		HttpResponse<JsonNode> response = httpClient(monitor, sql);
		if (null == response) {
			logger.error("Get connection fail with monitor server");
			return new BaseResult<JSONArray>(false, "与监控服务器端的连接失败");
		}
		
		if (200 == response.getStatus()) {
			JSONObject message = JSONObject.parseObject(response.getBody().toString());
			if (null == message || message.isEmpty()) {
				logger.info("未获取到监控信息, 请求状态码："+response.getStatus());
				return new BaseResult<JSONArray>(false, "未获取到监控信息");
			}
			
			if (message.containsKey("results")) {
				JSONArray results = JSONObject.parseArray(message.get("results").toString());
				if (!ListTool.isEmpty(results)) {
					JSONObject object = JSONObject.parseObject(results.get(0).toString());
					if (null == object || object.isEmpty()) {
						logger.info("未获取到监控信息, 请求结果没有数据");
						return new BaseResult<JSONArray>(false, "未获取到监控信息");
					}
					
					if (object.containsKey("series")) {
						JSONArray seriess = JSONObject.parseArray(object.get("series").toString());
						if (!ListTool.isEmpty(seriess)) {
							JSONObject series = JSONObject.parseObject(seriess.get(0).toString());
							if (null == series || series.isEmpty()) {
								logger.info("未获取到监控信息");
								return new BaseResult<JSONArray>(false, "未获取到监控信息");
							}
							
							if (series.containsKey("values")) {
								values = JSONObject.parseArray(series.get("values").toString());
							}
						}
					}
				}
			}
			return new BaseResult<JSONArray>(true, "获取服务端信息成功", values);
		} else {
			return new BaseResult<JSONArray>(false, "获取服务端监控信息失败");
		}
	}
	/**
	 * 获取sql
	 * @param monitor
	 * @return
	 */
	private String sql(Monitor monitor){
		String sql = null;
		switch (monitor.getType()) {
		case "pod":
			if (monitor.getDescripteName().contains("network")) {
				sql ="SELECT%20sum(%22value%22)%20FROM%20%22"
						+monitor.getDescripteName()+"%22%20WHERE%20%22type%22%20%3D%20%27pod%27%20AND%20%22namespace_name%22%20%3D~%20%2F"
						+monitor.getNamespace()+"%24%2F%20AND%20%22pod_name%22%20%3D~%20%2F"
						+monitor.getResourceName()+"%24%2F%20AND%20time%20%3E%20now()%20-%20"
						+monitor.getStartTime()+"%20GROUP%20BY%20time("
						+monitor.getBucketDuration()+"s)%20fill(null)";
			}else{
				sql = "SELECT%20sum(%22value%22)%20FROM%20%22"
						+monitor.getDescripteName()+"%22%20WHERE%20%22type%22%20%3D%20%27pod_container%27%20AND%20%22namespace_name%22%20%3D~%20%2F"
						+monitor.getNamespace()+"%24%2F%20AND%20%22pod_name%22%20%3D~%20%2F"
						+monitor.getResourceName()+"%24%2F%20AND%20time%20%3E%20now()%20-%20"
						+monitor.getStartTime()+"%20GROUP%20BY%20time("
						+monitor.getBucketDuration()+"s)%2C%20%22container_name%22%20fill(null)";
			}
			break;
        case "node":
        	sql = "SELECT%20sum(%22value%22)%20FROM%20%22"
					+monitor.getDescripteName()+"%22%20WHERE%20%22type%22%20%3D%20%27node%27%20AND%20%22nodename%22%20%3D~%20%2F"
					+monitor.getResourceName()+"%24%2F%20AND%20time%20%3E%20now()%20-%20"
					+monitor.getStartTime()+"%20GROUP%20BY%20time("
					+monitor.getBucketDuration()+"s)%2C%20%22nodename%22%20fill(null)";
			break;
		default://application
			sql = "SELECT%20sum(%22value%22)%20FROM%20%22"
					+monitor.getDescripteName()+"%22%20WHERE%20%22type%22%20%3D%20%27pod%27%20AND%20%22namespace_name%22%20%3D~%20%2F"
					+monitor.getNamespace()+"%24%2F%20AND%20time%20%3E%20now()%20-%20"
					+monitor.getStartTime()+"%20GROUP%20BY%20time(60s)%2C%20%22container_name%22%20fill(null)";
			break;
		}
		return sql;
	}
	/**
	 * 请求监控数据
	 * @param monitor
	 * @param sql
	 * @return
	 */
	private HttpResponse<JsonNode>  httpClient(Monitor monitor, String sql){
		StringBuffer bufferUrl = new StringBuffer();
		bufferUrl.append(monitor.getUrl()).append("/api/datasources/proxy/1/query?")
				.append("db=").append(monitor.getDataSource())
				.append("&q=").append(sql)
				.append("&epoch=ms");
		logger.info(monitor.getResourceType()+"访问的url: "+bufferUrl.toString());
		try {
			return Unirest.get(bufferUrl.toString()).asJson();
		} catch (UnirestException e) {
			logger.error("url request exception");
		}
		return null;
	}
	
	/**
	 * 线程池任务
	 * @param monitors
	 * @return
	 * @throws Exception
	 */
	public List<JSONObject> batch(List<Monitor> monitors) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
		CompletionService<BaseResult<JSONObject>> completionService = new ExecutorCompletionService<>(executor);
		if (ListTool.isEmpty(monitors)) {
			return null;
		}
		for (Monitor monitor : monitors) {
			MonitorTask task = new MonitorTask(monitor);
			completionService.submit(task);
		}
		List<JSONObject> objectList = new ArrayList<JSONObject>();
		int completeTask = 0;
		int timeout = 1000 * 60 * 10;
		long begingTime = System.currentTimeMillis();
		while (completeTask < monitors.size()) {
			try {
				Future<BaseResult<JSONObject>> take = completionService.take();
				if (null != take) {
					BaseResult<JSONObject> result = take.get();
					if (result.isSuccess()) {
						objectList.add((JSONObject) result.getData());
					}
					completeTask++;
					if (System.currentTimeMillis() - begingTime > timeout) {
						break;
					}
				}
			} catch (Exception e) {
				logger.error("线程池处理任务结果异常");
				continue;
			}
			
		}
		executor.shutdown();
		return objectList;
	}
	
	
}
