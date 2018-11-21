package com.bocloud.paas.service.application.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BaseResult;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.Monitor;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.service.environment.EnvironmentService;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.extensions.IngressList;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;

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
		
		// 获取namespace信息
		String namespace = application.getNamespace();

		// 获取环境信息
		BsmResult result = environmentService.detail(application.getEnvId());
		if (result.isFailed()) {
			return application;
		}
		Environment environment = (Environment) result.getData();
		// 获取ApplicationClient
		application.setEnvironmentName(environment.getName());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			logger.warn(environment.getProxy()+": 环境信息为空或环境不可用");
			return application;
		}
		
		String url = environment.getProxy(), port = String.valueOf(environment.getPort());
		ApplicationClient client = new ApplicationClient(url, port);
		
		int serviceNum = ((ServiceList)client.list(namespace, ApplicationEnum.RESOURCE.SERVICE)).getItems().size();
		application.setServiceNum(serviceNum);
		List<Pod> pods = ((PodList)client.list(namespace, ApplicationEnum.RESOURCE.POD)).getItems();
		int instanceNum = pods.size();
		application.setInstanceNum(instanceNum);
		//增加请求资源使用情况
		JSONObject requestMap = getRequestCpuAndMemory(pods);
		BigDecimal requestCpu = new BigDecimal(requestMap.getString("requestCpu"));
		BigDecimal requestMemory = new BigDecimal(requestMap.getString("requestMemory"));
		JSONObject cpuObject = cpuUtilTool(requestCpu);//cpu数据转换
		JSONObject mpuObject = memoryUtilTool(requestMemory);//mem数据转换
		application.setRequestCpu(cpuObject.getString("realValue"));
		application.setRequestMemory(mpuObject.getString("realValue"));
		
		//TODO当前使用情况
		application.setCurrentCpu(cpuUsage(application)+"m");
		application.setCurrentMemory(memoryUsage(application)+"Mi");
		
		//增加限制资源使用情况
		JSONObject limitMap = getLimitCpuAndMemory(pods);
		BigDecimal limitCpu = new BigDecimal(limitMap.getString("limitCpu"));
		BigDecimal limitMemory = new BigDecimal(limitMap.getString("limitMemory"));
		JSONObject cpuLimit = cpuUtilTool(limitCpu);//cpu数据转换
		JSONObject memLimit = memoryUtilTool(limitMemory);//mem数据转换
		application.setLimitCpu(cpuLimit.getString("realValue"));
		application.setLimitMemory(memLimit.getString("realValue"));
		
		//应用服务访问路径
		String servicePath = getIngress(client, namespace, url);
		if (null != servicePath) {
			application.setServicePath(servicePath);
		}
		return application;
	
	}
	
	/**
	 * 获取pod Limit cpu/memory
	 * @param client
	 * @return
	 */
	private JSONObject getLimitCpuAndMemory(List<Pod> pods){
		JSONObject jsonObject = new JSONObject();
		//全部主机上的cpu/memory
		Double totalPodCpu = 0.00;
		Double totalPodmemory = 0.00;
		for (Pod pod : pods) {
			List<Container> containers = pod.getSpec().getContainers();
			for (Container container : containers) {
				Map<String, Quantity> limit = container.getResources().getLimits();
				if(limit != null){
					if(limit.toString().contains("cpu")){
						String cpu = limit.get("cpu").getAmount();
						totalPodCpu += cpuTool(cpu);
					}
					if(limit.toString().contains("memory")){
						String memory = limit.get("memory").getAmount();
						totalPodmemory  += memoryTool(memory);
					}
				}
			}
		}
		jsonObject.put("limitCpu", totalPodCpu);
		jsonObject.put("limitMemory", totalPodmemory);
		return jsonObject;
	}
	
	/**
	 * 获取pod Requests cpu/memory
	 * @param client
	 * @return
	 */
	private JSONObject getRequestCpuAndMemory(List<Pod> pods){
		JSONObject jsonObject = new JSONObject();
		//全部主机上的cpu/memory
		Double totalPodCpu = 0.00;
		Double totalPodmemory = 0.00;
		for (Pod pod : pods) {
			List<Container> containers = pod.getSpec().getContainers();
			for (Container container : containers) {
				Map<String, Quantity> requests = container.getResources().getRequests();
				if(requests != null){
					if(requests.toString().contains("cpu")){
						String cpu = requests.get("cpu").getAmount();
						totalPodCpu += cpuTool(cpu);
					}
					if(requests.toString().contains("memory")){
						String memory = requests.get("memory").getAmount();
						totalPodmemory  += memoryTool(memory);
					}
				}
			}
		}
		jsonObject.put("requestCpu", totalPodCpu);
		jsonObject.put("requestMemory", totalPodmemory);
		return jsonObject;
	}
	
	//*===========================根据不同单位获取数值===============================*//
	
	//cpu计算基准单位m
	private int cpuTool(String object){
		int value = 0;
		if(object.contains("m")){
			value = Integer.parseInt(object.split("m")[0]);
		}else {
			value = Integer.parseInt(object)*1000;
		}
		return value;
	}
	//memory计算基本单位 Ki
	private int memoryTool(String object){
		int value = 0;
		if (object.contains("Ki")) {
			value = Integer.parseInt(object.split("Ki")[0]);
		} else if (object.contains("K")) {
			value = Integer.parseInt(object.split("K")[0]);
		}else if(object.contains("Mi")){
			value = Integer.parseInt(object.split("Mi")[0]);
			value = unitConversion(value, 1);
		}else if (object.contains("M")) {
			value = Integer.parseInt(object.split("M")[0]);
			value = unitConversion(value, 1);
		}else if (object.contains("Gi")) {
			value = Integer.parseInt(object.split("Gi")[0]);
			value = unitConversion(value, 2);
		}else if (object.contains("G")) {
			value = Integer.parseInt(object.split("G")[0]);
			value = unitConversion(value, 2);
		}else if (object.contains("Ti")) {
			value = Integer.parseInt(object.split("Ti")[0]);
			value = unitConversion(value, 3);
		}else if (object.contains("T")) {
			value = Integer.parseInt(object.split("T")[0]);
			value = unitConversion(value, 3);
		}else if (object.contains("Pi")) {
			value = Integer.parseInt(object.split("Pi")[0]);
			value = unitConversion(value, 4);
		}else if (object.contains("P")) {
			value = Integer.parseInt(object.split("P")[0]);
			value = unitConversion(value, 4);
		}else if (object.contains("Ei")) {
			value = Integer.parseInt(object.split("Ei")[0]);
			value = unitConversion(value, 5);
		}else if (object.contains("E")) {
			value = Integer.parseInt(object.split("E")[0]);
			value = unitConversion(value, 5);
		}else{
			value = Integer.parseInt(object);
			value = value / (int) Math.pow(1024, 1);
		}
		return value;
	}
	
	//缩减长度
		private JSONObject cpuUtilTool(BigDecimal bigDecimal){
			JSONObject object = new JSONObject();
			int intValue = bigDecimal.intValue();
			object.put("value", intValue);
			if (intValue < Math.pow(1000, 1)) {
				object.put("realValue", bigDecimal.intValue()+" m");
				return object;
			} else {
				bigDecimal = bigDecimal.divide(new BigDecimal(1000), 0, RoundingMode.HALF_UP);
				object.put("realValue", bigDecimal.intValue()+" c");
				return object;
			}
		}

		// 缩减长度
		private JSONObject memoryUtilTool(BigDecimal bigDecimal){
			JSONObject object = new JSONObject();
			int intValue = bigDecimal.intValue();
			object.put("value", intValue);
			if ( intValue < Math.pow(1024, 1)) {//Ki
				object.put("realValue", bigDecimal.intValue()+" Ki");
				return object;
			} else if (intValue < Math.pow(1024, 2)) {//Mi
				bigDecimal = bigDecimal.divide(BigDecimal.valueOf(Math.pow(1024, 1)), 0, RoundingMode.HALF_UP);
				object.put("realValue", bigDecimal.intValue()+" Mi");
				return object;
			} else if (intValue < Math.pow(1024, 3)) {//Gi
				bigDecimal = bigDecimal.divide(BigDecimal.valueOf(Math.pow(1024, 2)), 0, RoundingMode.HALF_UP);
				object.put("realValue", bigDecimal.intValue()+" Gi");
				return object;
			} else if (intValue < Math.pow(1024, 4)) {//Ti
				bigDecimal = bigDecimal.divide(BigDecimal.valueOf(Math.pow(1024, 3)), 0, RoundingMode.HALF_UP);
				object.put("realValue", bigDecimal.intValue()+" Ti");
				return object;
			} else if (intValue < Math.pow(1024, 5)) {//Pi
				bigDecimal = bigDecimal.divide(BigDecimal.valueOf(Math.pow(1024, 4)), 0, RoundingMode.HALF_UP);
				object.put("realValue", bigDecimal.intValue()+" Pi");
				return object;
			} else {//Ei
				bigDecimal = bigDecimal.divide(BigDecimal.valueOf(Math.pow(1024, 5)), 0, RoundingMode.HALF_UP);
				object.put("realValue", bigDecimal.intValue()+" Ei");
				return object;
			}
		}
	
	//单位换算
	private int unitConversion(int value, int n){
		int metric = 1024;
		for (int i = 0; i < n; i++) {
			value = value * metric ;
		}
		return value;
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
	
	/**
	 * 获取ingress已暴露服务的名单
	 * @param client
	 * @param namespace
	 * @param envProxy
	 */
	private String getIngress(ApplicationClient client, String namespace, String envProxy){
		StringBuffer buffer = new StringBuffer();
		IngressList ingressList = (IngressList) client.list(namespace, ApplicationEnum.RESOURCE.INGRESS);
		if (!ListTool.isEmpty(ingressList.getItems())) {
			List<IngressRule> rules = ingressList.getItems().get(0).getSpec().getRules();
			for (IngressRule ingressRule : rules) {
				String path = ingressRule.getHttp().getPaths().get(0).getPath();
				List<LoadBalancerIngress> list = ingressList.getItems().get(0).getStatus().getLoadBalancer().getIngress();
				if (!ListTool.isEmpty(list)) {
					if (buffer.length() > 0) {
						buffer.append(",");
					}
					buffer.append("https://"+list.get(0).getIp()+path);
				}
			}
		}
		return buffer.toString();
	}
}
