package com.bocloud.paas.service.application.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.DateTools;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.common.util.RfcDateTimeParser;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.service.application.ContainerService;
import com.bocloud.paas.service.application.util.ApplicationClient;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Zaney
 * @data:2017年3月9日
 * @describe:页面数据统计业务逻辑层实现
 */
@Component("containerService")
public class ContainerServiceImpl implements ContainerService {

	private static Logger logger = LoggerFactory.getLogger(ContainerServiceImpl.class);

	@Autowired
	private EnvironmentDao environmentDao;
	@Autowired
	private ApplicationDao applicationDao;

	@Override
	public BsmResult total(Long envId) {
		// 统计各组件数量
		Map<String, Integer> totalMap = new HashMap<>();
		// 检测环境
		Environment environment = getEnv(envId);
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}

		// 获取资源连接
		ApplicationClient client = build(environment);

		// 统计资源数
		totalMap.put("Pod", ((PodList) client.list(ApplicationEnum.RESOURCE.POD)).getItems().size());
		totalMap.put("Service", ((ServiceList) client.list(ApplicationEnum.RESOURCE.SERVICE)).getItems().size());
		totalMap.put("ReplicationController",
				((ReplicationControllerList) client.list(ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER)).getItems()
						.size()
						+ ((DeploymentList) client.list(ApplicationEnum.RESOURCE.DEPLOYMENT)).getItems().size());
		totalMap.put("PersistentVolume",
				((PersistentVolumeList) client.list(ApplicationEnum.RESOURCE.PERSISTENTVOLUME)).getItems().size());
		totalMap.put("Node", ((NodeList) client.list(ApplicationEnum.RESOURCE.NODE)).getItems().size());
		totalMap.put("HorizontalPodAutoscaler",
				((HorizontalPodAutoscalerList) client.list(ApplicationEnum.RESOURCE.HORIZONTALPODAUTOSCALER)).getItems()
						.size());
		client.close();
		return new BsmResult(true, totalMap, "获取数据统计信息成功");
	}

	@Override
	public BsmResult detail(String resourceName, Long appId, String resourceType) {
		Application application = getApplication(appId);
		// 检测环境
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}

		// 获取资源连接
		ApplicationClient client = build(environment);
		
		Object object = null;
		switch (resourceType) {
		case "Pod":
			Pod pod = (Pod) client.detail(application.getNamespace(), resourceName, ApplicationEnum.RESOURCE.POD);
			object = getPodDetail(pod);
			break;
		case "Job":
			object = client.detail(application.getNamespace(), resourceName, ApplicationEnum.RESOURCE.JOB);
			break;
		default:
			logger.warn(resourceType + "：不支持的类型");
			break;
		}
		
		client.close();
		return new BsmResult(true, object, "获取【资源详情】信息成功");
		
	}

	@Override
	public BsmResult detail(String resourceType, String namespace, String resourceName, Long envId) {
		Environment environment = getEnv(envId);
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}

		// 获取资源连接
		ApplicationClient client = build(environment);

		// 判断 根据资源的名称获取资源信息
		switch (resourceType) {
		case "Pod":
			Pod pod = (Pod) client.detail(namespace, resourceName, ApplicationEnum.RESOURCE.POD);
			client.close();
			return new BsmResult(true, getPodDetail(pod), "获取【运行实例】信息成功");

		case "ReplicationController":
			ReplicationController replicationController = (ReplicationController) client.detail(namespace, resourceName,
					ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
			if (null == replicationController) {
				Deployment deployment = (Deployment) client.detail(namespace, resourceName,
						ApplicationEnum.RESOURCE.DEPLOYMENT);
				client.close();
				return new BsmResult(true, getDeploymentDetail(deployment), "获取【部署】信息成功");
			}
			client.close();
			return new BsmResult(true, getRcDetail(replicationController), "获取【部署】信息成功");

		case "Service":
			Service service = (Service) client.detail(namespace, resourceName, ApplicationEnum.RESOURCE.SERVICE);
			client.close();
			return new BsmResult(true, getServiceDetail(service), "获取【服务】信息成功");

		case "PersistentVolumes":
			PersistentVolume pv = (PersistentVolume) client.detail(namespace, resourceName,
					ApplicationEnum.RESOURCE.PERSISTENTVOLUME);
			client.close();
			return new BsmResult(true, getPersistentVolumeDetail(pv), "获取【存储卷】信息成功");

		case "HorizontalPodAutoscaler":
			HorizontalPodAutoscaler hpa = (HorizontalPodAutoscaler) client.detail(namespace, resourceName,
					ApplicationEnum.RESOURCE.HORIZONTALPODAUTOSCALER);
			client.close();
			return new BsmResult(true, getHpaDetail(hpa), "获取【弹性伸缩】信息成功");
			
		case "Job":
			Job job = (Job) client.detail(namespace, resourceName, ApplicationEnum.RESOURCE.JOB);
			client.close();
			return new BsmResult(true, job, "获取【批处理任务】信息成功");

		default: // node or master
			Node node = (Node) client.detail(namespace, resourceName, ApplicationEnum.RESOURCE.NODE);
			client.close();
			return new BsmResult(true, getNodeDetail(node), "获取【节点】信息成功");
		}
	}

	@Override
	public BsmResult list(String resourceType, String resourceName, Long envId, String namespace) {
		// 环境检测
		Environment environment = getEnv(envId);
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}

		JSONArray containerList = containerList(resourceType, resourceName, environment, namespace);
		return new BsmResult(true, containerList, "获取成功");
	}

	@Override
	public BsmResult list(Map<String, String> labels, Long appId) {
		Application application = getApplication(appId);
		// 检测环境
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}

		JSONArray applicationList = applicationList(application.getNamespace(), environment, labels);
		return new BsmResult(true, applicationList, "获取成功");
	}

	@Override
	public BsmResult getNodePod(Long envId, String nodeIp) {
		if (null == envId) {
			return new BsmResult(false, "环境ID为空，请确认主机是否存在环境中");
		}
		// 检测环境
		Environment environment = getEnv(envId);
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}
		
		// 获取资源连接
		ApplicationClient client = build(environment);

		List<Pod> pods = ((PodList) client.list(ApplicationEnum.RESOURCE.POD)).getItems();
		client.close();
		JSONArray resourceList = getNodePod(environment.getProxy(), environment.getPort(), pods, nodeIp);
		return new BsmResult(true, resourceList, "获取Pod信息成功");
	}

	@Override
	public BsmResult event(String resourceName, Long appId) {
		Application application = getApplication(appId);
		// 检测环境
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}
		
		// 获取资源连接
		ApplicationClient client = build(environment);

		// 获取资源事件
		List<Event> items = ((EventList) client.list(application.getNamespace(), ApplicationEnum.RESOURCE.EVENT))
				.getItems();
		client.close();

		List<Map<String, Object>> eventList = eventDetail(items, resourceName);
		return new BsmResult(true, eventList, "获取成功");
	}

	@Override
	public BsmResult event(String namespace, String resourceName, Long envId) {
		// 检测环境
		Environment environment = getEnv(envId);
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}
		
		// 获取资源连接
		ApplicationClient client = build(environment);

		// 获取资源事件
		List<Event> items = ((EventList) client.list(namespace, ApplicationEnum.RESOURCE.EVENT)).getItems();
		client.close();

		List<Map<String, Object>> eventList = eventDetail(items, resourceName);
		return new BsmResult(true, eventList, "获取成功");

	}

	/**
	 * 获取事件详情
	 * 
	 * @param items
	 * @param resourceName
	 * @return
	 */
	private List<Map<String, Object>> eventDetail(List<Event> items, String resourceName) {
		try {
			List<Map<String, Object>> eventList = new ArrayList<>();
			for (Event event : items) {
				Map<String, Object> eventMap = new HashMap<>();
				// if (event.getInvolvedObject().getKind().equals(resourceType))
				// {
				if (event.getInvolvedObject().getName().equals(resourceName)) {
					eventMap.put("name", resourceName);
					eventMap.put("count", event.getCount());
					eventMap.put("kind", event.getInvolvedObject().getKind());
					eventMap.put("type", event.getType());
					eventMap.put("reason", event.getReason());
					eventMap.put("message", event.getMessage());
					eventMap.put("source", event.getSource().getComponent() + "   " + event.getSource().getHost());
					eventMap.put("time",
							DateTools.formatTime(RfcDateTimeParser.parseDateString(event.getFirstTimestamp())));
					eventMap.put("firstTime", DateTools.DateTime2Timestamp(
							DateTools.formatTime(RfcDateTimeParser.parseDateString(event.getFirstTimestamp()))));
					eventList.add(eventMap);
				}
			}
			// }
			if (!ListTool.isEmpty(eventList)) {
				// 按时间从大到小排序
				eventList = getSort(eventList, "firstTime");
			}
			return eventList;
		} catch (Exception e) {
			logger.error("time transform exception :", e);
			return null;
		}
	}

	@Override
	public BsmResult getLog(String resourceName, String containerName, String status, Long appId, Integer line) {
		Application application = getApplication(appId);
		// 检测环境
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}
		
		// 获取资源连接
		ApplicationClient client = build(environment);

		if (!logStatus(status)) {
			return new BsmResult(true, null, "获取成功");
		}
		String log = client.log(application.getNamespace(), resourceName, containerName, line);
		client.close();
		return new BsmResult(true, log, "获取成功");
	}

	@Override
	public BsmResult getLog(String namespace, String resourceName, String containerName, String status, Long envId, Integer line) {
		// 检测环境
		Environment environment = getEnv(envId);
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "环境信息为空或环境不可用");
		}
		
		// 获取资源连接
		ApplicationClient client = build(environment);

		if (!logStatus(status)) {
			return new BsmResult(true, null, "获取成功");
		}
		String log = client.log(namespace, resourceName, containerName, line);
		client.close();
		return new BsmResult(true, log, "获取成功");
	}

	/**
	 * 以下状态的pod没有日志，若继续强行获取，会报错
	 * 
	 * @param status
	 * @return
	 */
	private boolean logStatus(String status) {
		String[] args = new String[] { "ImagePullBackOff", "ContainerCreating", "ErrImagePull", "InvalidImageName" };
		for (String arg : args) {
			if (arg.equals(status)) {
				return false;
			}
		}
		return true;
	}

	/* ====================== 获取各资源列表 ============================ */
	private JSONArray getNodeResourceList(List<Node> nodes, String resourceName) {
		JSONArray nodeList = new JSONArray();
		for (Node node : nodes) {
			JSONObject nodeResourceMap = new JSONObject();
			StringBuilder status = new StringBuilder();
			// 逻辑获取节点状态
			List<NodeCondition> conditions = node.getStatus().getConditions();
			for (NodeCondition nodeCondition : conditions) {
				if (nodeCondition.getType().equals("Ready".trim())) {
					if (status.length() > 0) {
						status.append(",");
					}
					status.append(nodeCondition.getType()).append("=").append(nodeCondition.getStatus());
				}
			}
			// 判断节点是否调度
			if (status.length() > 0) {
				status.append(",").append("SchedulingDisabled").append("=");
			}
			if (node.getSpec().getUnschedulable() != null && node.getSpec().getUnschedulable()) {
				status.append("True");
			} else {
				status.append("False");
			}
			
			// 获取节点的IP
			List<NodeAddress> addresses = node.getStatus().getAddresses();
			if(!ListTool.isEmpty(addresses)){
				for (NodeAddress address : addresses) {
					if ("InternalIP".equals(address.getType())) {
						nodeResourceMap.put("ip", address.getAddress());
					}
				}
			}
			//
			nodeResourceMap.put("status", status);
			nodeResourceMap.put("namespace", node.getMetadata().getNamespace());
			nodeResourceMap.put("name", node.getMetadata().getName());
			nodeResourceMap.put("createTime",
					DateTools.formatTime(RfcDateTimeParser.parseDateString(node.getMetadata().getCreationTimestamp())));
			if (!StringUtils.isEmpty(resourceName)) {// 判断是否是模糊查询
														// resourceName!=null
														// 进行模糊查询
				if (node.getMetadata().getName().contains(resourceName)) {
					nodeList.add(nodeResourceMap);
				}
			} else {
				nodeList.add(nodeResourceMap);
			}
		}
		return nodeList;
	}

	private JSONArray getPersistentVolumeResourceList(List<PersistentVolume> persistentVolumes, String resourceName) {
		JSONArray resourceList = new JSONArray();
		for (PersistentVolume pv : persistentVolumes) {
			JSONObject pvResourceMap = new JSONObject();
			List<String> accessModes = pv.getSpec().getAccessModes();
			StringBuilder accessModeString = new StringBuilder();
			for (String accessMode : accessModes) {
				if (accessModeString.length() > 0) {
					accessModeString.append(",");
				}
				if ("ReadWriteMany".equals(accessMode)) {
					accessModeString.append("读写多次");
				} else {
					accessModeString.append("读写一次");
				}
			}
			// 获取pvc
			ObjectReference claimRef = pv.getSpec().getClaimRef();
			if (null != claimRef) {
				pvResourceMap.put("pvc", claimRef.getName());
			} else {
				pvResourceMap.put("pvc", null);
			}
			// 状态
			if ("bound".equals(pv.getStatus().getPhase())) {
				pvResourceMap.put("status", "绑定");
			} else {
				pvResourceMap.put("status", "发布");
			}
			// 策略
			if ("retain".equals(pv.getSpec().getPersistentVolumeReclaimPolicy())) {
				pvResourceMap.put("reclaimPolicy", "保留");
			} else {
				pvResourceMap.put("reclaimPolicy", "回收");
			}
			pvResourceMap.put("capacity", pv.getSpec().getCapacity().get("storage").getAmount());
			pvResourceMap.put("namespace", pv.getMetadata().getNamespace());
			pvResourceMap.put("name", pv.getMetadata().getName());
			pvResourceMap.put("createTime",
					DateTools.formatTime(RfcDateTimeParser.parseDateString(pv.getMetadata().getCreationTimestamp())));
			pvResourceMap.put("accessMode", accessModeString);
			if (!StringUtils.isEmpty(resourceName)) {// 判断是否是模糊查询
														// resourceName!=null
														// 进行模糊查询
				if (pv.getMetadata().getName().contains(resourceName)) {
					resourceList.add(pvResourceMap);
				}
			} else {
				resourceList.add(pvResourceMap);
			}
		}
		return resourceList;
	}

	private JSONArray getHpaResourceList(List<HorizontalPodAutoscaler> hpas, String resourceName) {
		JSONArray resourceList = new JSONArray();
		for (HorizontalPodAutoscaler hpa : hpas) {
			JSONObject hpaResourceMap = new JSONObject();
			hpaResourceMap.put("namespace", hpa.getMetadata().getNamespace());
			hpaResourceMap.put("name", hpa.getMetadata().getName());
			hpaResourceMap.put("resourceName", hpa.getSpec().getScaleTargetRef().getName());
			hpaResourceMap.put("cpuTargetUtilization", hpa.getSpec().getTargetCPUUtilizationPercentage());
			hpaResourceMap.put("cpuCurrentUtilization", hpa.getStatus().getCurrentCPUUtilizationPercentage() == null
					? "waiting" : hpa.getStatus().getCurrentCPUUtilizationPercentage());
			hpaResourceMap.put("minReplicas", hpa.getSpec().getMinReplicas());
			hpaResourceMap.put("maxReplicas", hpa.getSpec().getMaxReplicas());
			hpaResourceMap.put("createTime",
					DateTools.formatTime(RfcDateTimeParser.parseDateString(hpa.getMetadata().getCreationTimestamp())));
			if (!StringUtils.isEmpty(resourceName)) {// 判断是否是模糊查询
														// resourceName!=null
														// 进行模糊查询
				if (hpa.getMetadata().getName().contains(resourceName)) {
					resourceList.add(hpaResourceMap);
				}
			} else {
				resourceList.add(hpaResourceMap);
			}
		}
		return resourceList;
	}

	private JSONArray getServiceResourceList(List<Service> services, String nodeIp, String resourceName) {
		JSONArray resourceList = new JSONArray();
		for (Service service : services) {
			JSONObject serviceResourceMap = new JSONObject();
			StringBuilder ports = new StringBuilder();
			StringBuilder nodePorts = new StringBuilder();
			for (ServicePort servicePort : service.getSpec().getPorts()) {
				if (ports.length() > 0) {
					ports.append(",");
				}
				ports.append(servicePort.getPort()).append("/").append(servicePort.getProtocol());
				if (nodePorts.length() > 0) {
					nodePorts.append(",");
				}
				if (servicePort.getNodePort() == null) {
					continue;
				}
				nodePorts.append(servicePort.getNodePort());
			}
			String namespace = service.getMetadata().getNamespace();
			String serviceName = service.getMetadata().getName();
			serviceResourceMap.put("nodePort", nodePorts.toString());
			serviceResourceMap.put("namespace", namespace);
			serviceResourceMap.put("name", serviceName);
			serviceResourceMap.put("clusterIp", service.getSpec().getClusterIP());
			serviceResourceMap.put("port", ports.toString());
			serviceResourceMap.put("createTime", DateTools
					.formatTime(RfcDateTimeParser.parseDateString(service.getMetadata().getCreationTimestamp())));
			serviceResourceMap.put("accessPath", "https//" + nodeIp + "/" + serviceName.split("-")[0]);

			if (!StringUtils.isEmpty(resourceName)) {// 判断是否是模糊查询
														// resourceName!=null
														// 进行模糊查询
				if (service.getMetadata().getName().contains(resourceName)) {
					resourceList.add(serviceResourceMap);
				}
			} else {
				resourceList.add(serviceResourceMap);
			}
		}
		return resourceList;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getDeploymentResourceList(ApplicationClient client, List<Deployment> deployments, List<Service> services, String resourceName) {
		JSONArray resourceList = new JSONArray();
		JSONObject serviceObject = new JSONObject();// 存放service selector的数据信息
		/**
		 * 获取所有服务的selector
		 */
		for (Service service : services) {
			serviceObject.put(service.getMetadata().getNamespace() + "/" + service.getMetadata().getName(), 
					service.getSpec().getSelector());
		}
		
		for (Deployment deployment : deployments) {
			JSONObject deploymentResourceMap = new JSONObject();
			
			/**
			 * deploy 的 label 与 service 的selector 对比
			 */
			String serviceName = null;
			Map<String, String> labelsMap = deployment.getSpec().getTemplate().getMetadata().getLabels();
			if (null != labelsMap) {
				
				for (Entry<String, Object> entry : serviceObject.entrySet()) {
					Map<String, String> selectorMap = (Map<String, String>) entry.getValue();
					
					if (null == selectorMap) {
						continue;
					}
					//selector与lables对比
					boolean result = compareMap(labelsMap, selectorMap);
					if (result) {
						serviceName = entry.getKey().split("/")[1];
					}
				}
			}
			deploymentResourceMap.put("serviceName", serviceName);
			
			deploymentResourceMap.put("namespace", deployment.getMetadata().getNamespace());
			deploymentResourceMap.put("name", deployment.getMetadata().getName());
			deploymentResourceMap.put("createTime", DateTools
					.formatTime(RfcDateTimeParser.parseDateString(deployment.getMetadata().getCreationTimestamp())));
			deploymentResourceMap.put("desired", deployment.getSpec().getReplicas());
			deploymentResourceMap.put("current", deployment.getStatus().getReplicas());
			deploymentResourceMap.put("upToDate", deployment.getStatus().getUpdatedReplicas());
			deploymentResourceMap.put("available", deployment.getStatus().getAvailableReplicas());
			deploymentResourceMap.put("type", "Deployment");
			if (!StringUtils.isEmpty(resourceName)) {// 判断是否是模糊查询
														// resourceName!=null
														// 进行模糊查询
				if (deployment.getMetadata().getName().contains(resourceName)) {
					resourceList.add(deploymentResourceMap);
				}
			} else {
				resourceList.add(deploymentResourceMap);
			}
		}
		return resourceList;
	}
	
	private static boolean compareMap(Map<String, String> map1, Map<String, String> map2) {
	    boolean contain = false;
	    for (String key : map1.keySet()) {
	        contain = map2.containsKey(key);
	        if (contain) {
	            contain = map1.get(key).equals(map2.get(key));
	        }
	        if (!contain) {
	            return false;
	        }
	    }
	    return true;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getRcResourceList(List<ReplicationController> rcs, List<Service> services, String resourceName) {
		JSONArray resourceList = new JSONArray();
		JSONObject serviceObject = new JSONObject();// 存放service selector的数据信息
		/**
		 * 获取所有服务的selector
		 */
		for (Service service : services) {
			serviceObject.put(service.getMetadata().getNamespace() + "/" + service.getMetadata().getName(), 
					service.getSpec().getSelector());
		}
		for (ReplicationController rc : rcs) {
			/**
			 * rc 的 label 与 service 的selector 对比
			 */
			JSONObject rcResourceMap = new JSONObject();
			String serviceName = null;
			Map<String, String> labelsMap = rc.getSpec().getTemplate().getMetadata().getLabels();
			if (labelsMap != null) {
				for (Entry<String, Object> entry : serviceObject.entrySet()) {
					Map<String, String> selectorMap = (Map<String, String>) entry.getValue();
					
					if (null == selectorMap) {
						continue;
					}
					//selector与lables对比
					boolean result = compareMap(labelsMap, selectorMap);
					if (result) {
						serviceName = entry.getKey().split("/")[1];
					}
				}
			}
			rcResourceMap.put("serviceName", serviceName);
			rcResourceMap.put("namespace", rc.getMetadata().getNamespace());
			rcResourceMap.put("name", rc.getMetadata().getName());
			rcResourceMap.put("createTime",
					DateTools.formatTime(RfcDateTimeParser.parseDateString(rc.getMetadata().getCreationTimestamp())));
			rcResourceMap.put("desired", rc.getSpec().getReplicas());
			rcResourceMap.put("current", rc.getStatus().getReplicas());
			rcResourceMap.put("available", rc.getStatus().getAvailableReplicas());
			rcResourceMap.put("type", "ReplicationController");
			if (!StringUtils.isEmpty(resourceName)) {// 判断是否是模糊查询
														// resourceName!=null
														// 进行模糊查询
				if (rc.getMetadata().getName().contains(resourceName)) {
					resourceList.add(rcResourceMap);
				}
			} else {
				resourceList.add(rcResourceMap);
			}
		}
		return resourceList;
	}

	private JSONArray getPodResourceList(String proxy, Integer port, List<Pod> pods, String resourceName) {
		JSONArray resourceList = new JSONArray();
		for (Pod pod : pods) {
			JSONArray containers = new JSONArray();
			JSONObject podResourceMap = new JSONObject();
			Integer restartCount = 0;
			String status = null;
			int read = 0;
			List<String> statuss = new ArrayList<String>();
			JSONObject containerInfo = new JSONObject();
			StringBuffer image = new StringBuffer();
			// 循环pod里所有容器的状态及其其它信息
			String reason = pod.getStatus().getReason();
			//pod.getStatus().getReason()有值，一般是环境原因导致，比如node节点丢失
			if (StringUtils.hasText(reason)) {
				statuss.add(reason);
			} else {
				List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
				
				//pod的容器无状态的时候
				if (ListTool.isEmpty(containerStatuses)) {
					List<Container> containersList = pod.getSpec().getContainers();
					for (Container container : containersList) {
						
						// 对镜像的处理，多个镜像用“ ，” 隔开
						if (image.length() > 0) {
							image.append(",");
						}
						if (container.getImage() != null) {
							String imageName = container.getImage()
									.substring(container.getImage().lastIndexOf("/") + 1);
							image.append(imageName);
							containerInfo.put("image", imageName);
						}
						containerInfo.put("name", container.getName());
						status = "Pending";
						containerInfo.put("status", status);
						statuss.add(status);
					}
					containers.add(containerInfo);
				} else {//pod的容器有状态的时候
					for (ContainerStatus containerStatus : containerStatuses) {
						if (containerStatus.getRestartCount() >= restartCount) {
							restartCount = containerStatus.getRestartCount();
							ContainerStateWaiting waiting = containerStatus.getState().getWaiting();
							if (waiting != null) {
								status = waiting.getReason();
							} else {
								if (containerStatus.getState().getRunning() != null) {
									status = "running";
								} else if (null != containerStatus.getState().getTerminated() && 
										StringUtils.hasText(containerStatus.getState().getTerminated().getReason())) {
									status = containerStatus.getState().getTerminated().getReason();
								}else {
									status = "Terminating";
								}
							}
							statuss.add(status);
						}
						// 准备状态
						if (containerStatus.getReady()) {
							read++;
						}
						
						// 对镜像的处理，多个镜像用“ ，” 隔开
						if (image.length() > 0) {
							image.append(",");
						}
						if (containerStatus.getImage() != null) {
							String imageName = containerStatus.getImage()
									.substring(containerStatus.getImage().lastIndexOf("/") + 1);
							image.append(imageName);
							containerInfo.put("image", imageName);
						}
						
						// 获取pod所有容器信息，添加进集合
						containerInfo.put("name", containerStatus.getName());
						String containerId = null;
						if (null != containerStatus.getContainerID()) {
							containerId = containerStatus.getContainerID().split("//")[1];
							containerInfo.put("containerId", containerId.substring(0, 12));
						} else {
							containerInfo.put("containerId", containerId);
						}
						containerInfo.put("node", pod.getStatus().getHostIP());
						containerInfo.put("status", status);
						containers.add(containerInfo);
					}
				}
			}
			
			// 若该pod中，存在多个容器，会存在不健康的容器，那么pod的状态也显示不健康
			for (String stu : statuss) {
				if (!"running".equals(stu)) {
					status = stu;
				}
			}
			
			// 若无containerStatus 则
			podResourceMap.put("containers", containers);
			podResourceMap.put("proxyUrl", proxy);
			podResourceMap.put("port", port);
			podResourceMap.put("images", image);
			podResourceMap.put("namespace", pod.getMetadata().getNamespace());
			podResourceMap.put("name", pod.getMetadata().getName());
			podResourceMap.put("containersReady", read + "/" + statuss.size());// 正常的容器占该pod中所有容器的比例
			podResourceMap.put("status", status);
			podResourceMap.put("restartCount", restartCount);
			podResourceMap.put("createTime",
					DateTools.formatTime(RfcDateTimeParser.parseDateString(pod.getMetadata().getCreationTimestamp())));
			if (!StringUtils.isEmpty(resourceName)) {// 判断是否是模糊查询
														// resourceName!=null
														// 进行模糊查询
				if (pod.getMetadata().getName().contains(resourceName)) {
					resourceList.add(podResourceMap);
				}
			} else {
				resourceList.add(podResourceMap);
			}
		}
		return resourceList;
	}

	/* ====================== 获取各资源详情 ============================ */
	private JSONObject getPodDetail(Pod pod) {
		JSONObject detailMap = new JSONObject();
		// Status
		JSONObject statusMap = new JSONObject();
		JSONArray statusList = new JSONArray();
		statusMap.put("iP", pod.getStatus().getPodIP());
		statusMap.put("node", pod.getSpec().getNodeName());
		statusMap.put("restartPolicy", pod.getSpec().getRestartPolicy());
		statusList.add(statusMap);
		detailMap.put("Status", statusList);
		// Container
		JSONArray containerList = new JSONArray();
		List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
		for (ContainerStatus containerStatus : containerStatuses) {
			JSONObject containerMap = new JSONObject();
			containerMap.put("containerName", containerStatus.getName());
			if (containerStatus.getState().getWaiting() != null) {
				containerMap.put("state", containerStatus.getState().getWaiting().getReason());
			} else {
				if (containerStatus.getState().getTerminated() != null) {
					containerMap.put("state", containerStatus.getState().getTerminated().getReason());
				} else {
					containerMap.put("state", "running");
				}
			}
			containerMap.put("ready", containerStatus.getReady());
			containerMap.put("restartCount", containerStatus.getRestartCount());
			containerList.add(containerMap);
		}
		detailMap.put("Container", containerList);
		// Template
		JSONArray templateList = new JSONArray();
		for (Container container : pod.getSpec().getContainers()) {
			JSONObject templateMap = new JSONObject();
			templateMap.put("name", container.getName());
			templateMap.put("image", container.getImage());
			StringBuilder ports = new StringBuilder();
			for (ContainerPort containerPort : container.getPorts()) {
				if (ports.length() > 0) {
					ports.append(",");
				}
				ports.append(containerPort.getContainerPort()).append("/").append(containerPort.getProtocol());
			}
			templateMap.put("ports", ports.toString());
			List<VolumeMount> volumeMounts = container.getVolumeMounts();
			StringBuilder mount = new StringBuilder();
			for (VolumeMount volumeMount : volumeMounts) {
				if (mount.length() > 0) {
					mount.append(",");
				}
				mount.append(volumeMount.getName()).append("->").append(volumeMount.getMountPath());
			}
			templateMap.put("mount", mount.toString());
			// 获取 活性探针 livenessProbe
			Probe livenessProbe = container.getLivenessProbe();
			if (null != livenessProbe) {
				ExecAction exec = livenessProbe.getExec();
				HTTPGetAction httpGet = livenessProbe.getHttpGet();
				if (null != exec) {
					templateMap.put("livenessProbe", exec.getCommand());
				}
				if (null != httpGet) {
					StringBuffer httpGetBuffer = new StringBuffer();
					httpGetBuffer.append("GET ").append(httpGet.getPath()).append(" on port ")
							.append(httpGet.getPort());
					templateMap.put("livenessProbe", httpGetBuffer.toString());
				}
			}
			// 获取 准备探针 readinessProbe
			Probe readinessProbe = container.getReadinessProbe();
			if (null != readinessProbe) {
				ExecAction execAction = readinessProbe.getExec();
				HTTPGetAction httpGetAction = readinessProbe.getHttpGet();
				if (null != execAction) {
					templateMap.put("readinessProbe", execAction.getCommand());
				}
				if (null != httpGetAction) {
					StringBuffer httpGetBuffer = new StringBuffer();
					httpGetBuffer.append("GET ").append(httpGetAction.getPath()).append(" on port ")
							.append(httpGetAction.getPort());
					templateMap.put("readinessProbe", httpGetBuffer.toString());
				}
			}

			// 获取limit
			Map<String, Quantity> limits = container.getResources().getLimits();
			String cpuLimits = null;
			String memoryLimits = null;
			StringBuffer limit = new StringBuffer();
			if (null != limits) {
				if (null != limits.get("cpu")) {
					cpuLimits = limits.get("cpu").getAmount();
					limit.append("cpu").append("=").append(cpuLimits);
				}
				if (null != limits.get("memory")) {
					memoryLimits = limits.get("memory").getAmount();
					limit.append(",").append("memory").append("=").append(memoryLimits);
				}
			}
			templateMap.put("limits", limit.toString());
			// 获取 request
			String cpuRequest = null;
			String memoryRequest = null;
			StringBuffer request = new StringBuffer();
			Map<String, Quantity> requests = container.getResources().getRequests();
			if (null != requests) {
				if (null != requests.get("cpu")) {
					cpuRequest = requests.get("cpu").getAmount();
					request.append("cpu").append("=").append(cpuRequest);
				}
				if (null != requests.get("memory")) {
					memoryRequest = requests.get("memory").getAmount();
					request.append(",").append("memory").append("=").append(memoryRequest);
				}
			}
			templateMap.put("requests", request.toString());

			templateList.add(templateMap);
		}
		detailMap.put("Template", templateList);
		// Volumes
		List<Volume> volumes = pod.getSpec().getVolumes();
		JSONArray volumesList = new JSONArray();
		for (Volume volume : volumes) {
			JSONObject volumesMap = new JSONObject();
			volumesMap.put("volumes", volume.getName());
			if (null != volume.getPersistentVolumeClaim()) {
				volumesMap.put("flag", "flag");
				volumesMap.put("type", "持续卷声明");
				volumesMap.put("claimName", volume.getPersistentVolumeClaim().getClaimName());
				if (null == volume.getPersistentVolumeClaim().getReadOnly()) {
					volumesMap.put("mode", "读写");
				} else {
					volumesMap.put("mode", "只读");
				}
			} else if (null != volume.getEmptyDir()) {
				volumesMap.put("flag", "empty");
				volumesMap.put("type", "empty dir");
				if (null == volume.getEmptyDir().getMedium()) {
					volumesMap.put("medium", "节点默认");
				} else {
					volumesMap.put("medium", volume.getEmptyDir().getMedium());
				}
			} else {
				if (!StringUtils.isEmpty(volume.getSecret())) {
					volumesMap.put("flag", "notFlag");
					volumesMap.put("type", "secret");
					volumesMap.put("secretName", volume.getSecret().getSecretName());
				}
			}
			volumesList.add(volumesMap);
		}
		detailMap.put("Volumes", volumesList);
		return detailMap;
	}

	private JSONObject getRcDetail(ReplicationController rc) {
		JSONObject detailMap = new JSONObject();
		// labels
		Map<String, String> labelMap = rc.getMetadata().getLabels();
		StringBuilder labels = new StringBuilder();
		if (labelMap != null) {
			for (Map.Entry<String, String> entry : labelMap.entrySet()) {
				labels.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		// selectors
		Map<String, String> selectorMap = rc.getSpec().getSelector();
		StringBuilder selectors = new StringBuilder();
		if (selectorMap != null) {
			for (Map.Entry<String, String> entry : selectorMap.entrySet()) {
				selectors.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		// image
		StringBuilder images = new StringBuilder();
		List<Container> containers = rc.getSpec().getTemplate().getSpec().getContainers();
		for (Container container : containers) {
			if (images.length() > 0) {
				images.append(",");
			}
			images.append(container.getImage());
		}
		detailMap.put("name", rc.getMetadata().getName());
		detailMap.put("namespace", rc.getMetadata().getNamespace());
		detailMap.put("images", images);
		detailMap.put("labels", labels.toString());
		detailMap.put("selectors", selectors.toString());
		detailMap.put("replicas",
				rc.getStatus().getReplicas() + " 当前运行实例数 / " + rc.getSpec().getReplicas() + " 期望运行实例数");
		return detailMap;
	}

	/**
	 * 获取Deployment的详细信息
	 * 
	 * @param deployment
	 * @return
	 */
	private JSONObject getDeploymentDetail(Deployment deployment) {
		JSONObject detailMap = new JSONObject();
		// labels
		Map<String, String> labelMap = deployment.getMetadata().getLabels();
		StringBuilder labels = new StringBuilder();
		if (labelMap != null) {
			for (Map.Entry<String, String> entry : labelMap.entrySet()) {
				labels.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		// selectors
		Map<String, String> selectorMap = deployment.getSpec().getSelector().getMatchLabels();
		StringBuilder selectors = new StringBuilder();
		if (selectorMap != null) {
			for (Map.Entry<String, String> entry : selectorMap.entrySet()) {
				selectors.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		// image
		StringBuilder images = new StringBuilder();
		List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
		for (Container container : containers) {
			if (images.length() > 0) {
				images.append(",");
			}
			images.append(container.getImage());
		}
		detailMap.put("name", deployment.getMetadata().getName());
		detailMap.put("namespace", deployment.getMetadata().getNamespace());
		detailMap.put("images", images);
		detailMap.put("labels", labels.toString());
		detailMap.put("selectors", selectors.toString());
		detailMap.put("replicas",
				deployment.getStatus().getReplicas() + " 当前运行实例数 / " + deployment.getSpec().getReplicas() + " 期望运行实例数");
		return detailMap;
	}

	/**
	 * 获取服务的详情
	 *
	 * @param service
	 * @return
	 */
	private JSONObject getServiceDetail(Service service) {
		JSONObject detailMap = new JSONObject();
		// Basic Details
		JSONObject basicDetailMap = new JSONObject();
		JSONArray basicDetailList = new JSONArray();
		Map<String, String> labelMap = service.getMetadata().getLabels();
		StringBuilder labels = new StringBuilder();
		if (labelMap != null) {
			for (Map.Entry<String, String> entry : labelMap.entrySet()) {
				labels.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		Map<String, String> selectorMap = service.getSpec().getSelector();
		StringBuilder selectors = new StringBuilder();
		if (selectorMap != null) {
			for (Map.Entry<String, String> entry : selectorMap.entrySet()) {
				selectors.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		basicDetailMap.put("labels", labels.toString());
		basicDetailMap.put("selectors", selectors.toString());
		basicDetailMap.put("type", service.getSpec().getType());
		basicDetailMap.put("sessionAffinity", service.getSpec().getSessionAffinity());
		basicDetailList.add(basicDetailMap);
		detailMap.put("basicDetails", basicDetailList);
		return detailMap;
	}

	/**
	 * 获取PV详情
	 *
	 * @param persistentVolume
	 * @return
	 */
	private JSONObject getPersistentVolumeDetail(PersistentVolume persistentVolume) {
		JSONObject detailMap = new JSONObject();
		// Basic Details
		JSONObject basicDetailMap = new JSONObject();
		JSONArray basicDetailList = new JSONArray();
		Map<String, String> labelMap = persistentVolume.getMetadata().getLabels();
		StringBuilder labels = new StringBuilder();
		if (labelMap != null) {
			for (Map.Entry<String, String> entry : labelMap.entrySet()) {
				labels.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		basicDetailMap.put("labels", labels.toString());
		basicDetailMap.put("reclaimPolicy", persistentVolume.getSpec().getPersistentVolumeReclaimPolicy());
		basicDetailMap.put("message", persistentVolume.getStatus().getMessage());
		basicDetailList.add(basicDetailMap);
		detailMap.put("basicDetails", basicDetailList);
		// Source
		JSONObject sourceMap = new JSONObject();
		JSONArray sourceList = new JSONArray();
		NFSVolumeSource nfs = persistentVolume.getSpec().getNfs();
		HostPathVolumeSource hostPath = persistentVolume.getSpec().getHostPath();
		if (nfs != null) {
			sourceMap.put("server", nfs.getServer());
			sourceMap.put("path", nfs.getPath());
			if (nfs.getReadOnly() != null && nfs.getReadOnly()) {
				sourceMap.put("readOnly", true);
			} else {
				sourceMap.put("readOnly", false);
			}
		} else {
			sourceMap.put("path", hostPath.getPath());
		}
		/*
		 * sourceMap.put("server",
		 * persistentVolume.getSpec().getNfs().getServer());
		 * sourceMap.put("path", persistentVolume.getSpec().getNfs().getPath());
		 * if (persistentVolume.getSpec().getNfs().getReadOnly() != null &&
		 * persistentVolume.getSpec().getNfs().getReadOnly()) {
		 * sourceMap.put("readOnly", true); } else { sourceMap.put("readOnly",
		 * false); }
		 */
		sourceList.add(sourceMap);
		detailMap.put("Source", sourceList);
		// pvc
		JSONObject pvcMap = new JSONObject();
		JSONArray pvcList = new JSONArray();
		ObjectReference claimRef = persistentVolume.getSpec().getClaimRef();
		if (null != claimRef) {
			pvcMap.put("pvcName", claimRef.getName());
		}
		pvcList.add(pvcMap);
		detailMap.put("Pvc", pvcList);
		return detailMap;
	}

	/**
	 * 获取hpa的详情
	 *
	 * @param service
	 * @return
	 */
	private JSONObject getHpaDetail(HorizontalPodAutoscaler hpa) {
		JSONObject detailMap = new JSONObject();
		// Basic Details
		JSONObject basicDetailMap = new JSONObject();
		JSONArray basicDetailList = new JSONArray();
		Map<String, String> labelMap = hpa.getMetadata().getLabels();
		StringBuilder labels = new StringBuilder();
		if (labelMap != null) {
			for (Map.Entry<String, String> entry : labelMap.entrySet()) {
				labels.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		basicDetailMap.put("labels", labels.toString());
		basicDetailList.add(basicDetailMap);
		detailMap.put("basicDetails", basicDetailList);
		return detailMap;
	}

	/**
	 * 获取Node详情
	 *
	 * @param node
	 * @return
	 */
	private JSONObject getNodeDetail(Node node) {
		JSONObject detailMap = new JSONObject();
		// Basic Details
		JSONObject basicDetailMap = new JSONObject();
		JSONArray basicDetailList = new JSONArray();
		Map<String, String> labelsMap = node.getMetadata().getLabels();
		StringBuilder labels = new StringBuilder();
		if (labelsMap != null) {
			for (Map.Entry<String, String> entry : labelsMap.entrySet()) {
				labels.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
			}
		}
		basicDetailMap.put("labels", labels.toString());
		basicDetailMap.put("containerRuntimeVersion", node.getStatus().getNodeInfo().getContainerRuntimeVersion());
		basicDetailMap.put("externalID", node.getSpec().getExternalID());
		basicDetailList.add(basicDetailMap);
		detailMap.put("basicDetails", basicDetailList);
		// Capacity
		JSONObject capacityMap = new JSONObject();
		JSONArray capacityList = new JSONArray();
		capacityMap.put("cpu", node.getStatus().getCapacity().get("cpu").getAmount());
		BigDecimal memory = new BigDecimal(node.getStatus().getCapacity().get("memory").getAmount().split("Ki")[0]);
		memory = memory.divide(new BigDecimal(1e6), 2, RoundingMode.HALF_UP);
		capacityMap.put("memory", memory.toPlainString() + "Gi");
		capacityMap.put("pods", node.getStatus().getCapacity().get("pods").getAmount());
		capacityList.add(capacityMap);
		detailMap.put("capacity", capacityList);
		// Allocatable
		JSONObject allocatablelMap = new JSONObject();
		JSONArray allocatableList = new JSONArray();
		allocatablelMap.put("cpu", node.getStatus().getAllocatable().get("cpu").getAmount());
		BigDecimal mem = new BigDecimal(node.getStatus().getAllocatable().get("memory").getAmount().split("Ki")[0]);
		mem = mem.divide(new BigDecimal(1e6), 2, RoundingMode.HALF_UP);
		allocatablelMap.put("memory", mem.toPlainString() + "Gi");
		allocatablelMap.put("pods", node.getStatus().getAllocatable().get("pods").getAmount());
		allocatableList.add(allocatablelMap);
		detailMap.put("allocatable", allocatableList);
		return detailMap;
	}

	/* =====================获取各资源平台所有的列表========================= */
	/**
	 * 获取kubernetes平台资源列表
	 * 
	 * @param resourceType
	 * @param resourceName
	 * @param appId
	 * @param proxyId
	 * @return
	 */
	public JSONArray containerList(String resourceType, String resourceName, Environment environment,
			String namespace) {
		// 获取资源连接
		ApplicationClient client = build(environment);

		// 3、判断获取各资源列表
		JSONArray resourceList;
		switch (resourceType) {
		case "Pod": // pod
			List<Pod> pods = new ArrayList<>();
			if (null != namespace) {
				pods = ((PodList) client.list(namespace, ApplicationEnum.RESOURCE.POD)).getItems();
			} else {
				pods = ((PodList) client.list(ApplicationEnum.RESOURCE.POD)).getItems();
			}
			client.close();
			return getPodResourceList(environment.getProxy(), environment.getPort(), pods, resourceName);
		case "ReplicationController": // 此处resourceType为ReplicationController，但是取的是RC和Deployment，而不是只取RC
			List<ReplicationController> rcs = new ArrayList<>();
			List<Deployment> deployments = new ArrayList<>();
			if (null != namespace) {
				deployments = ((DeploymentList) client.list(namespace, ApplicationEnum.RESOURCE.DEPLOYMENT)).getItems();
				rcs = ((ReplicationControllerList) client.list(namespace,
						ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER)).getItems();
			} else {
				deployments = ((DeploymentList) client.list(ApplicationEnum.RESOURCE.DEPLOYMENT)).getItems();
				rcs = ((ReplicationControllerList) client.list(ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER))
						.getItems();
			}

			List<Service> serviceList = ((ServiceList) client.list(namespace, ApplicationEnum.RESOURCE.SERVICE))
					.getItems();
			JSONArray deploymentList = getDeploymentResourceList(client, deployments, serviceList, resourceName);
			resourceList = getRcResourceList(rcs, serviceList, resourceName);
			for (Object object : deploymentList) {
				resourceList.add(JSONObject.toJSON(object));
			}
			client.close();
			return resourceList;
		case "Service": // service
			List<Service> services = new ArrayList<>();
			if (null != namespace) {
				services = ((ServiceList) client.list(namespace, ApplicationEnum.RESOURCE.SERVICE)).getItems();
			} else {
				services = ((ServiceList) client.list(ApplicationEnum.RESOURCE.SERVICE)).getItems();
			}
			client.close();
			// List<Ingress> ingresses =
			// client.extensions().ingresses().inAnyNamespace().list().getItems();
			return getServiceResourceList(services, environment.getProxy(), resourceName);
		case "PersistentVolumes": // persistentVolumes
			List<PersistentVolume> persistentVolumes = ((PersistentVolumeList) client
					.list(ApplicationEnum.RESOURCE.PERSISTENTVOLUME)).getItems();
			client.close();
			return getPersistentVolumeResourceList(persistentVolumes, resourceName);
		case "HorizontalPodAutoscaler": // HorizontalPodAutoscaler
			List<HorizontalPodAutoscaler> hpa = ((HorizontalPodAutoscalerList) client
					.list(ApplicationEnum.RESOURCE.HORIZONTALPODAUTOSCALER)).getItems();
			client.close();
			return getHpaResourceList(hpa, resourceName);
		case "Job":
			return getJobList(client, resourceName, namespace);
		default: // node or master
			List<Node> nodes = ((NodeList) client.list(ApplicationEnum.RESOURCE.NODE)).getItems();
			client.close();
			return getNodeResourceList(nodes, resourceName);
		}
	}

	/**
	 * 获取应用平台资源列表
	 * 
	 * @param clusters
	 * @param resourceType
	 * @param resourceName
	 * @param appId
	 * @param proxyId
	 * @return
	 */
	public JSONArray applicationList(String namespace, Environment environment, Map<String, String> labels) {

		// 获取资源连接
		ApplicationClient client = build(environment);

		PodList podList = (PodList) client.list(namespace, labels, ApplicationEnum.RESOURCE.POD);
		List<Pod> pods = podList.getItems();
		client.close();
		return getPodResourceList(environment.getProxy(), environment.getPort(), pods, null);
	}

	/**
	 * 获取节点下的pod
	 * 
	 * @param proxyUrl
	 * @param pods
	 * @param nodeName
	 * @return
	 */
	private JSONArray getNodePod(String proxyUrl, Integer port, List<Pod> pods, String nodeIp) {
		JSONArray resourceList = new JSONArray();
		for (Pod pod : pods) {
			// 与节点名相同的pod 就返回页面
			String podNodeIp = pod.getStatus().getHostIP();
			if (StringUtils.isEmpty(podNodeIp)) {
				continue;
			}
			if (podNodeIp.equals(nodeIp)) {
				JSONArray containers = new JSONArray();
				JSONObject podResourceMap = new JSONObject();
				Integer restartCount = 0;
				String status = null;
				int read = 0;
				StringBuffer image = new StringBuffer();
				List<String> statuss = new ArrayList<String>();
				// 循环pod里所有容器的状态及其其它信息
				List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
				for (ContainerStatus containerStatus : containerStatuses) {
					JSONObject containerInfo = new JSONObject();
					if (containerStatus.getRestartCount() >= restartCount) {
						restartCount = containerStatus.getRestartCount();
						ContainerStateWaiting waiting = containerStatus.getState().getWaiting();
						if (waiting != null) {
							status = waiting.getReason();
						} else {
							if (containerStatus.getState().getTerminated() != null) {
								status = containerStatus.getState().getTerminated().getReason();
							} else {
								status = "running";
							}
						}
						statuss.add(status);
					}
					// 准备状态
					if (containerStatus.getReady()) {
						read++;
					}
					// 对镜像的处理，多个镜像用“ ，” 隔开
					if (image.length() > 0) {
						image.append(",");
					}
					if (containerStatus.getImage() != null) {
						String imageName = containerStatus.getImage()
								.substring(containerStatus.getImage().lastIndexOf("/") + 1);
						image.append(imageName);
						containerInfo.put("image", imageName);
					}
					// 获取pod所有容器信息，添加进集合
					containerInfo.put("name", containerStatus.getName());
					String containerId = null;
					if (null != containerStatus.getContainerID()) {
						containerId = containerStatus.getContainerID().split("//")[1];
						containerInfo.put("containerId", containerId.substring(0, 12));
					} else {
						containerInfo.put("containerId", containerId);
					}
					containerInfo.put("node", pod.getStatus().getHostIP());
					containerInfo.put("status", status);
					containers.add(containerInfo);
				}
				// 若该pod中，存在多个容器，会存在不健康的容器，那么pod的状态也显示不健康
				for (String stu : statuss) {
					if (!"running".equals(stu)) {
						status = stu;
					}
				}
				// 若无containerStatus 则
				podResourceMap.put("containers", containers);
				podResourceMap.put("proxyUrl", proxyUrl);
				podResourceMap.put("port", port);
				podResourceMap.put("images", image);
				podResourceMap.put("namespace", pod.getMetadata().getNamespace());
				podResourceMap.put("name", pod.getMetadata().getName());
				podResourceMap.put("containersReady", read + "/" + statuss.size());// 正常的容器占该pod中所有容器的比例
				podResourceMap.put("status", status);
				podResourceMap.put("restartCount", restartCount);
				podResourceMap.put("createTime", DateTools
						.formatTime(RfcDateTimeParser.parseDateString(pod.getMetadata().getCreationTimestamp())));
				resourceList.add(podResourceMap);
			}
		}
		return resourceList;
	}

	/**
	 * 时间排序 从大到小
	 * 
	 * @param events
	 * @param sortColunm
	 * @return
	 */
	public List<Map<String, Object>> getSort(List<Map<String, Object>> events, String sortColunm) {
		// 时间排序
		for (int i = 0; i < events.size() - 1; i++) {
			for (int j = 0; j < events.size() - 1 - i; j++) {
				if ((Long) events.get(j).get("firstTime") < (Long) events.get(j + 1).get("firstTime")) {
					Map<String, Object> event = events.get(j);
					events.set(j, events.get(j + 1));
					events.set(j + 1, event);
				}
			}
		}
		return events;
	}

	@Override
	public BsmResult template(Long applicationId, String name) {
		BsmResult bsmResult = new BsmResult(false, "");
		// 获取资源连接
		Application application = getApplication(applicationId);
		if (null == application) {
			bsmResult.setMessage("未获取到应用信息");
			return bsmResult;
		}
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			bsmResult.setMessage("环境信息为空或环境不可用");
			return bsmResult;
		}
		
		ApplicationClient client = build(environment);
		if (null == client) {
			return new BsmResult(false, "未获取到连接");
		}
		Object object = client.detail(application.getNamespace(), name, ApplicationEnum.RESOURCE.POD);
		return new BsmResult(true, JSONObject.toJSON(object), "获取模板信息成功");
	}

	@Override
	public BsmResult hostList(Long applicationId, Map<String, String> labels) {
		BsmResult bsmResult = new BsmResult(false, "");
		Set<String> set = new HashSet<String>();
		List<Node> nodes = new ArrayList<Node>();
		// 获取资源连接
		Application application = getApplication(applicationId);
		if (null == application) {
			bsmResult.setMessage("未获取到应用信息");
			return bsmResult;
		}
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			bsmResult.setMessage("环境信息为空或环境不可用");
			return bsmResult;
		}
		ApplicationClient client = build(environment);
		if (null == client) {
			return new BsmResult(false, "未获取到连接");
		}
		// 获取nodeName
		PodList podList = (PodList) client.list(application.getNamespace(), labels, ApplicationEnum.RESOURCE.POD);
		 List<Pod> pods = podList.getItems();
		for (Pod pod : pods) {
			set.add(pod.getSpec().getNodeName());
		}
		// 遍历set
		for (String name : set) {
			if (StringUtils.isEmpty(name)) {
				continue;
			}
			Node node = (Node) client.detail(application.getNamespace(), name, ApplicationEnum.RESOURCE.NODE);
			nodes.add(node);
		}
		return new BsmResult(true, getNodeResourceList(nodes, null), "获取成功");
	}

	/**
	 * 获取应用信息
	 * 
	 * @param applicationId
	 * @return
	 */
	private Application getApplication(Long applicationId) {
		Application application;
		try {
			application = applicationDao.get(Application.class, applicationId);
			return application;
		} catch (Exception e) {
			logger.error("获取应用名称异常.", e);
			return null;
		}
	}

	/**
	 * 获取环境对象
	 * 
	 * @param envId
	 * @return
	 */
	private Environment getEnv(Long envId) {
		try {
			return environmentDao.get(Environment.class, envId);
		} catch (Exception e) {
			logger.error("get Environment info exception.", e);
			return null;
		}
	}

	/**
	 * 构建资源链接的client
	 * 
	 * @param applicationId
	 * @return
	 */
	private ApplicationClient build(Environment environment) {
		String url = environment.getProxy(), port = String.valueOf(environment.getPort());

		// 获取详细的资源信息，包括service和deployment
		ApplicationClient client = new ApplicationClient(url, port);
		return client;
	}
	
	//获取job列表
	private JSONArray getJobList(ApplicationClient client, String resourceName, String namespace){
		JSONArray array = new JSONArray();
		List<Job> jobs = null;
		if(StringUtils.hasText(namespace)){
			jobs = ((JobList) client.list(namespace, ApplicationEnum.RESOURCE.JOB)).getItems();
			
		} else {
			jobs = ((JobList) client.list(ApplicationEnum.RESOURCE.JOB)).getItems();
		}
		
		for (Job job : jobs) {
			if (!StringUtils.isEmpty(resourceName)) {// 判断是否是模糊查询
				// resourceName!=null
				// 进行模糊查询
				if (job.getMetadata().getName().contains(resourceName)) {
					array.add(job);
				}
			} else {
				array.add(job);
			}
		}
		client.close();
		return array;
	}

}
