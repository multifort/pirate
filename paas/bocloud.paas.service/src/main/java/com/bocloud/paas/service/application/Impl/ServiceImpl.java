package com.bocloud.paas.service.application.Impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.common.util.StringUtil;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.application.ApplicationImageInfoDao;
import com.bocloud.paas.dao.application.ApplicationLayoutInfoDao;
import com.bocloud.paas.dao.application.ConfigManageDao;
import com.bocloud.paas.dao.application.LayoutDao;
import com.bocloud.paas.dao.application.ServiceAlarmDao;
import com.bocloud.paas.dao.application.ServiceRelyInfoDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.ApplicationImageInfo;
import com.bocloud.paas.entity.ApplicationLayoutInfo;
import com.bocloud.paas.entity.ConfigManage;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.Layout;
import com.bocloud.paas.entity.ServiceAlarm;
import com.bocloud.paas.entity.ServiceRelyInfo;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.common.enums.DeployEnum;
import com.bocloud.paas.model.Labels;
import com.bocloud.paas.model.Volume;
import com.bocloud.paas.service.application.Service;
import com.bocloud.paas.service.application.model.ConfManage;
import com.bocloud.paas.service.application.model.DataMap;
import com.bocloud.paas.service.application.model.PodInfo;
import com.bocloud.paas.service.application.model.Port;
import com.bocloud.paas.service.application.model.ServiceBean;
import com.bocloud.paas.service.application.util.ApplicationClient;
import com.bocloud.paas.service.utils.EmailSender;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Job;
import io.fabric8.kubernetes.api.model.JobList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerList;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressList;
import io.fabric8.kubernetes.api.model.extensions.StatefulSet;
import io.fabric8.kubernetes.api.model.extensions.StatefulSetList;

@org.springframework.stereotype.Service("service")
public class ServiceImpl implements Service {

	private static Logger logger = LoggerFactory.getLogger(ServiceImpl.class);
	
	private final static String SERVICE = "Service";

	@Autowired
	private EnvironmentDao environmentDao;
	@Autowired
	private ApplicationDao applicationDao;
	@Autowired
	private LayoutDao layoutDao;
	@Autowired
	private ServiceRelyInfoDao serviceRelyInfoDao;
	@Autowired
	private ApplicationLayoutInfoDao applicationLayoutInfoDao;
	@Autowired
	private ApplicationImageInfoDao applicationImageInfoDao;
	@Autowired
	private ConfigManageDao configManageDao;
	@Autowired
	private ServiceAlarmDao serviceAlarmDao;
	@Autowired
	private EmailSender emailSender;

	public BsmResult list(Long applicationId) {
		JSONArray jsonArray = new JSONArray();

		// 获取namespace信息
		String namespace = getNamespace(applicationId);

		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "未获取到应用归属环境的链接");
		}

		// 获取详细的资源信息列表，包括service和deployment
		ServiceList services = (ServiceList) client.list(namespace, ApplicationEnum.RESOURCE.SERVICE);
		IngressList ingresses = (IngressList) client.list(namespace, ApplicationEnum.RESOURCE.INGRESS);
		
		//服务对实例数告警策略属性处理
		List<ServiceBean> serviceBeans = getServiceAlarmStatus(services.getItems(), applicationId);
		
		// 构建返回的service对象，转化成json字符串
		JSONObject serviceJson = new JSONObject();
		serviceJson.put("service", serviceBeans);
		serviceJson.put("ingress", ingresses);
		DeploymentList deployments = (DeploymentList) client.list(namespace, ApplicationEnum.RESOURCE.DEPLOYMENT);
		if (!deployments.getItems().isEmpty()) {
			jsonArray.add(deployments);
		}
		ReplicationControllerList replicationControllers = (ReplicationControllerList) client.list(namespace,
				ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
		if (!replicationControllers.getItems().isEmpty()) {
			jsonArray.add(replicationControllers);
		}
		StatefulSetList sflList = (StatefulSetList) client.list(namespace, ApplicationEnum.RESOURCE.STATEFULSETS);
		if (!sflList.getItems().isEmpty()) {
			jsonArray.add(sflList);
		}
		serviceJson.put("deployment", jsonArray);
		client.close();
		// String serviceStr = JSONObject.toJSONString(serviceJson);
		return new BsmResult(true, serviceJson, "获取服务列表成功");
	}
	/**
	 * 服务对实例数告警策略属性处理
	 * @param items
	 * @param applicationId
	 */
	private List<ServiceBean> getServiceAlarmStatus(List<io.fabric8.kubernetes.api.model.Service> items, Long applicationId){
		List<ServiceBean> list = new ArrayList<>();
		try {
			if (!ListTool.isEmpty(items)) {
				for (io.fabric8.kubernetes.api.model.Service service : items) {
					ServiceBean serviceBean = new ServiceBean();
					String alarmStatus = String.valueOf(ServiceAlarm.AlarmStatus.NOSTRATEGY.ordinal());//空，则无策略
					ServiceAlarm serviceAlarm = serviceAlarmDao
							.detail(applicationId, service.getMetadata().getName());
					if (null != serviceAlarm) {
						alarmStatus = serviceAlarm.getStatus();
					}
					BeanUtils.copyProperties(service, serviceBean);
					serviceBean.setAlarmStatus(alarmStatus);//赋予服务告警策略的状态值
					list.add(serviceBean);
				}
			}
		} catch (Exception e) {
			logger.error("获取服务告警状态值异常", e);
		}
		return list;
	}

	public BsmResult detail(Long applicationId, String name) {

		// 获取namespace信息
		String namespace = getNamespace(applicationId);

		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "未获取到服务归属环境的链接");
		}

		// 获取详细的资源信息，包括service和deployment
		io.fabric8.kubernetes.api.model.Service service = (io.fabric8.kubernetes.api.model.Service) client
				.detail(namespace, name, ApplicationEnum.RESOURCE.SERVICE);
		Ingress ingress = (Ingress) client.detail(namespace, name, ApplicationEnum.RESOURCE.INGRESS);
		// 构建返回的service对象，转化成json字符串
		JSONObject serviceJson = new JSONObject();
		serviceJson.put("service", service);
		serviceJson.put("ingress", ingress);
		Deployment deployment = (Deployment) client.detail(namespace, name, ApplicationEnum.RESOURCE.DEPLOYMENT);
		if (null == deployment) {
			ReplicationController replicationController = (ReplicationController) client.detail(namespace, name,
					ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
			serviceJson.put("deployment", replicationController);
		} else {
			serviceJson.put("deployment", deployment);
		}
		client.close();

		String serviceStr = JSONObject.toJSONString(serviceJson);
		return new BsmResult(true, serviceStr, "获取服务列表成功");

	}

	@Override
	public boolean existed(Long applicationId, String name) {
		// 获取namespace信息
		String namespace = getNamespace(applicationId);

		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return true;
		}

		// 判断service和rc是否存在
		boolean serviceExisted = client.existed(namespace, name, ApplicationEnum.RESOURCE.SERVICE);
//		boolean rcExisted = client.existed(namespace, name, ApplicationEnum.RESOURCE.DEPLOYMENT);
		client.close();

//		return serviceExisted || rcExisted;
		return serviceExisted;
	}
	
	@Override
	public void statistic(Long applicationId, String name) {
		// TODO Auto-generated method stub
	}

	@Override
	public BsmResult deploy(Long applicationId, Long layoutId) {
		//获取应用
		Application application = getApplication(applicationId);
		
		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "未获取到服务归属环境的链接");
		}
		// 执行编排文件
		File file = layoutFile(layoutId);
		
		List<HasMetadata> metadatas ;
		try {
			metadatas = client.load(file.getAbsolutePath());
		} catch (Exception e) {
			logger.error("layout format error", e);
			return new BsmResult(false, "文件格式不对,请确认后格式再创建");
		}
		
		//编排文件->名称格式校验
		StringBuffer formatBuffer = checkNameFormat(metadatas);
		if (formatBuffer.length() > 0) {
			return new BsmResult(false, "[" + formatBuffer + "], 名称格式不正确，只能输入英文小写字母、数字、中划线！");
		}
		
		//编排文件->名称校验
		StringBuffer buffer = checkLayout(client, application.getNamespace(), metadatas);
		if (buffer.length() > 0) {
			return new BsmResult(false, "该应用下资源名称["+buffer+"]已经存在, 请更改名称后再进行部署");
		}
		
		//校验编排文件是否同时包含svc 和 deploy/rc，检验有效的部署
		boolean checked = checkValidityDeploy(metadatas);
		if (!checked) {
			return new BsmResult(false, "请发布有效的服务，如 Service+Deployment 或 Service+ReplicationController");
		}
		
		//namespace替换
		for (HasMetadata metadata : metadatas) {
			metadata.getMetadata().setNamespace(application.getNamespace());
		}
		
		//执行文件
		metadatas = client.load(metadatas);
//		List<HasMetadata> metadatas = load(client, file, false);
		if (ListTool.isEmpty(metadatas)) {
			return new BsmResult(false, "服务部署失败");
		}
		
		client.close();
		
		if (!modifyStatus(application)) {
			return new BsmResult(true, "服务部署成功,修改应用状态失败");
		}
		if (!saveAppLay(applicationId, layoutId)) {//添加应用和文件的关系
			return new BsmResult(true, "服务部署成功,关联文件编排失败");
		}
		return new BsmResult(true, "服务部署成功");
	}
	
	/**
	 * 检验编排文件是否是有效部署
	 * @param metadatas
	 * @return
	 */
	private boolean checkValidityDeploy(List<HasMetadata> metadatas){
		List<String> arrayList = new ArrayList<>();
		for (HasMetadata hasMetadata : metadatas) {
			arrayList.add(hasMetadata.getKind());
		}
		if (arrayList.size() == 2 && arrayList.contains("Service") && arrayList.contains("Deployment") 
				|| arrayList.size() == 2 && arrayList.contains("Service") && arrayList.contains("ReplicationController")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 名称格式校验
	 * @param hasMetadatas
	 * @return
	 */
	private StringBuffer checkNameFormat(List<HasMetadata> hasMetadatas){
		StringBuffer buffer = new StringBuffer();
		//名称格式校验
		for (HasMetadata hasMetadata : hasMetadatas) {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			boolean matched = hasMetadata.getMetadata().getName().matches("^[0-9a-z-]+$");
			if (!matched) {
				buffer.append(hasMetadata.getMetadata().getName());
			}
		}
		
		return buffer;
	}

	
	/**
	 * 校验编排文件是否有namespace以及值得替换
	 * @param client
	 * @param filePath
	 * @param namespace
	 * @return
	 */
	private StringBuffer checkLayout(ApplicationClient client, String namespace, List<HasMetadata> hasMetadatas){
		StringBuffer buffer = new StringBuffer();
		//service 名称是否存在服务端校验
		for (HasMetadata hasMetadata : hasMetadatas) {
			String name = hasMetadata.getMetadata().getName();
			String kind = hasMetadata.getKind();
			switch (kind) {
			case SERVICE:
				Object service = client.detail(namespace, name, ApplicationEnum.RESOURCE.SERVICE);
				if (null != service) {
					if (buffer.length() > 0) {
						buffer.append(",");
					}
					buffer.append("Service["+name+"]");
				}
				break;
			default:
				Object rc = client.detail(namespace, name, ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
				Object deploy = client.detail(namespace, name, ApplicationEnum.RESOURCE.DEPLOYMENT);
				if (null != rc || null != deploy) {
					if (buffer.length() > 0) {
						buffer.append(",");
					}
					buffer.append(kind+"["+name+"]");
				}
				break;
			}
		}
		
		return buffer;
	}

	/**
	 * 保存应用和编排模板的关系
	 * @param applicationId
	 * @param layoutId
	 * @return
	 */
	private boolean saveAppLay(Long applicationId, Long layoutId){
		ApplicationLayoutInfo info = new ApplicationLayoutInfo();
		info.setApplicationId(applicationId);
		info.setLayoutId(layoutId);
		try {
			boolean inSuccess = applicationLayoutInfoDao.insert(info);
			if (inSuccess) {
				return true;
			}
			logger.warn("save application with layOut relation fail");
		} catch (Exception e) {
			logger.error("save application with layOut relation exception", e);
		}
		return false;
	}

	@Override
	public BsmResult deploy(Long applicationId, Object object) {
		JSONObject jsonObject = JSONObject.parseObject(object.toString());
		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "未获取到服务归属环境的链接");
		}
		//获取应用
		Application application = getApplication(applicationId);
		
		//生成Deployment模板
		String templateContent = "";
		try{
			templateContent = templateGenerator(client, application, jsonObject);
		}catch(Exception e){
			return new BsmResult(false, e.getMessage());
		}
		
		if ("".equals(templateContent) || null == templateContent) {
			return new BsmResult(false, "模板赋值失败");
		}
		//创建文件
		BsmResult bsmResult = FileUtil.createFile(FileUtil.filePath("resource_template") + "deployment_basic_temporary.yaml", templateContent);
		if (bsmResult.isSuccess()) {
			File file = (File) bsmResult.getData();
			//服务部署
			 List<HasMetadata> metadatas = load(client, file, true);
			 if (ListTool.isEmpty(metadatas)) {
				return new BsmResult(false, "服务部署失败");
			 }
			 
			client.close();
			//修改应用状态、保存服务依赖关系
			if (modifyStatus(application)) {
				//维护服务依赖关系
				if (save(jsonObject, StringUtil.convertPinyin(application.getName()).toLowerCase())) {
					//维护应用和镜像关系
					if (saveApplicationImage(jsonObject.getLong("imageId"), applicationId)) {
						return new BsmResult(true, "服务部署成功");
					}
					return new BsmResult(false, "服务部署成功, 保存应用镜像关系失败");
				}
				return new BsmResult(false, "服务部署成功, 保存服务依赖关系失败");
			} else {
				return new BsmResult(false, "服务部署成功, 修改应用状态失败");
			}
		} else {
			return new BsmResult(false, "创建部署文件失败");
		}
	}
	
	/**
	 * 维护应用和镜像的关系
	 * @param imageId
	 * @param applicationId
	 * @return
	 */
	private boolean saveApplicationImage(Long imageId, Long applicationId){
		ApplicationImageInfo applicationImageInfo = null;
		try {
			applicationImageInfo = new ApplicationImageInfo();
			applicationImageInfo.setApplicationId(applicationId);
			applicationImageInfo.setImageId(imageId);
			boolean created = applicationImageInfoDao.insert(applicationImageInfo);
			if (created) {
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("维护应用与镜像关系异常 "+e);
			return false;
		}
	}
	/**
	 * 修改应用状态、保存服务依赖关系
	 * @param application
	 * @param jsonObject
	 * @return
	 */
	private boolean modifyStatus(Application application){
		if (!"DEPLOY".equals(application.getStatus())) {
			application.setStatus("DEPLOY");
		}
		application.setGmtModify(new Date());
		try {
			if (applicationDao.update(application)) {
				return true;
			}
			logger.error("service deploy is success, modify Application info fail");
			return false;
		} catch (Exception e) {
			logger.error("deploy application exception", e);
			return false;
		}
	}
	/**
	 * 根据文件模板部署服务
	 * @param client 
	 * @param file 文件名称
	 * @param flag  load成功后，是否删除文件   true|删除  、 false|不删除
	 * @return
	 */
	private List<HasMetadata> load(ApplicationClient client, File file, boolean flag){
			List<HasMetadata> metadatas = null;
			try {
				metadatas = client.load(file);
			} catch (Exception e) {
				logger.error("deploy exception", e);
			}
			// 创建完成之后删除文件
			if (flag) { //如果true， 删除文件
				if (file.isFile() && file.exists()) {
					file.delete();
				}
			}
			return metadatas;
	}

	/**
	 * Deployment模板生成
	 * @param client
	 * @param application
	 * @param object
	 */
	private String templateGenerator(ApplicationClient client, Application application, JSONObject jsonObject) throws Exception{
		Template template = template();
		//判断部署类型是否支持
		validationDeployType(jsonObject.getString("deployStatus"), template);
		
		template.binding("namespace", application.getNamespace());
		template.binding("serviceName", jsonObject.get("name"));
		template.binding("image", jsonObject.get("image"));
		template.binding("replicas", jsonObject.get("replicas"));
		template.binding("appName", StringUtil.convertPinyin(application.getName()).toLowerCase());

		/**
		 * 节点调度
		 */
		template = makeNodeSelector(jsonObject, template);
		
		/**
		 * 端口处理 : 如果有端口需要暴露，则在svc里不显示不暴露的端口，因为一旦NodePort存在，系统会自动给没有nodePort的端口暴露
		 */
		template = makePort(jsonObject, template);
		
		/**
		 * 健康检查
		 */
		template = makeHealth(jsonObject, template);

		/**
		 * 资源限制
		 */
		template = makeResource(jsonObject, template);

		/**
		 * 环境
		 */
		template = makeEnv(jsonObject, template);
		
		
		/**
		 * 配置文件挂载
		 */
		template = makeCmvolume(jsonObject, template);
		
		/**
		 * pv
		 */
		template = makeVolume(jsonObject, template, client, application.getName());
		
		String templateContent = template.render();
		return templateContent;
	}
	
	/**
	 * 校验资源类型
	 * @param deployStatus
	 * @param template
	 * @return
	 * @throws Exception
	 */
	private void validationDeployType(String deployStatus, Template template) throws Exception{
		if(DeployEnum.DEPLOYMENT.getName().equals(deployStatus)){
			template.binding("apiVersion", "extensions/v1beta1");
			template.binding("kind", "Deployment");
			template.binding("JobType", null);
			
		} else if(DeployEnum.STATEFULSET.getName().equals(deployStatus)){
			template.binding("apiVersion", "apps/v1beta1");
		    template.binding("kind", "StatefulSet");
		    template.binding("JobType", null);
		    
		}else if(DeployEnum.JOB.getName().equals(deployStatus)){
			template.binding("apiVersion", "batch/v1");
			template.binding("kind", "Job");
			template.binding("JobType", "Job");
			
		}else {
			logger.warn("不支持的资源部署类型");
			throw new Exception("不支持的资源部署类型");
		}
	}
	
	//*********  镜像部署模板制作  ********//
	/**
	 * 模板存储卷-pv挂载数据处理
	 * @param jsonObject
	 * @param template
	 * @param client
	 * @param appName
	 * @return
	 */
	private Template makeVolume(JSONObject jsonObject, Template template, ApplicationClient client, String appName){
		String volumes = jsonObject.getString("volumes");
		JSONObject objVolume = JSONObject.parseObject(volumes);
		String volumeType = objVolume.getString("type");
		template.binding("volumeType", volumeType);
		if (StringUtils.hasText(volumeType)) {
			ArrayList<com.bocloud.paas.model.Volume> arrays = new ArrayList<>();
			JSONArray volumeArray = objVolume.getJSONArray("arrays");
			if ("tv".equals(volumeType)) {
				for (Object object : volumeArray) {
					Volume volume = JSONObject.parseObject(object.toString(), Volume.class);
					arrays.add(volume);
				}
				template.binding("volumes", arrays);
			} else if ("pv".equals(volumeType)) {
				for (Object object : volumeArray) {
					Volume volume = JSONObject.parseObject(object.toString(), Volume.class);
					if (StringUtils.hasText(volume.getPv())) {
						boolean createPvcByPv = client.createPvcByPv(volume.getPv(), jsonObject.get("name")+ "-" + volume.getPv() + "-claim",
								"application-" + StringUtil.convertPinyin(appName).toLowerCase());
						if (createPvcByPv) {
							System.out.println("success create pv :"+createPvcByPv);
							arrays.add(volume);
						}
					}
				}
				
				template.binding("volumes", arrays);
			}
		} else {
			template.binding("volumes", new ArrayList<>());
		}
		
		return template;
	}
	/**
	 * 模板配置管理存储卷挂载方式数据处理
	 * @param jsonObject
	 * @param template
	 * @return
	 */
	private Template makeCmvolume(JSONObject jsonObject, Template template){
		List<ConfManage> cmVolumes = new ArrayList<>();
		JSONArray cmVolumeArray = jsonObject.getJSONArray("cmVolume");
		if (!ListTool.isEmpty(cmVolumeArray)) {
			for (Object object : cmVolumeArray) {
				JSONObject cmvObject = JSONObject.parseObject(object.toString());
				ConfManage confManage = new ConfManage(cmvObject.getString("name"), cmvObject.getString("path"));
				cmVolumes.add(confManage);
			}
		}
		template.binding("cmVolumes", cmVolumes);
		
		return template;
	}
	/**
	 * 模板环境数据处理
	 * @param jsonObject
	 * @param template
	 * @return
	 */
	private Template makeEnv(JSONObject jsonObject, Template template){
		//环境-镜像自带环境变量
		List<Labels> envs = new ArrayList<>();
		Set<String> envkeys = jsonObject.getJSONObject("env").keySet();
		for (String key : envkeys) {
			String value = jsonObject.getJSONObject("env").getString(key).toString();
			if (!"-".equals(value)) {
				Labels labels = new Labels(key, value);
				envs.add(labels);
			}
			
		}
		template.binding("envs", envs);
		
		//环境-配置管理
		List<ConfManage> envFroms = new ArrayList<>();
		JSONArray envFromArray = jsonObject.getJSONArray("envFrom");
		if (!ListTool.isEmpty(envFromArray)) {
			for (Object object : envFromArray) {
				ConfManage confManage = new ConfManage(object.toString());
				envFroms.add(confManage);
			}
		}
		template.binding("envFroms", envFroms);
		
		return template;
	}
	/**
	 * 模板资源配额数据处理
	 * @param jsonObject
	 * @param template
	 * @return
	 */
	private Template makeResource(JSONObject jsonObject, Template template){
		JSONObject resources = jsonObject.getJSONObject("resources");
		if (null == resources || resources.isEmpty()) {
			template.binding("resourcesType", null);
		} else {
			template.binding("resourcesType", resources);
			String resourceKindType = resources.getString("type");
			template.binding("resourceKindType", resourceKindType);
			if("cpu".equals(resourceKindType)) {
				JSONObject requests = resources.getJSONObject("requests");
				JSONObject limits = resources.getJSONObject("limits");
				template.binding("requestsMemory", requests.get("memory") + "Mi");
				template.binding("requestsCpu", requests.get("cpu") + "m");
				template.binding("limitsMemory", limits.get("memory") + "Mi");
				template.binding("limitsCpu", limits.get("cpu") + "m");
			} else if ("gpu".equals(resourceKindType)) {
				template.binding("gpuNum", resources.getString("gpuNum"));
			}
			
		}
		
		return template;
	}
	/**
	 * 模板节点调度数据处理
	 * @param jsonObject
	 * @param template
	 * @return
	 */
	private Template makeNodeSelector(JSONObject jsonObject, Template template){
		JSONObject nodeSelector = jsonObject.getJSONObject("nodeSelector");
		template.binding("nodeSelectorType", nodeSelector.get("type"));
		List<String> selectors = new ArrayList<String>();
		Set<String> selectorKeys = nodeSelector.getJSONObject("value").keySet();
		for (String key : selectorKeys) {
			if ("nodeName".equals(key)) {
				template.binding("nodeName", nodeSelector.getJSONObject("value").get(key));
			} else {
				String label = key + ": " + nodeSelector.getJSONObject("value").get(key);
				selectors.add(label);
			}
		}
		template.binding("selectors", selectors);
		return template;
	}
	/**
	 * 模板端口数据处理
	 * @param jsonObject
	 * @param template
	 * @return
	 */
	private Template makePort(JSONObject jsonObject, Template template){
		List<Port> portList = new ArrayList<>();
		List<DataMap> containerPorts = new ArrayList<>();
		List<Port> nodePortList = new ArrayList<>();

		//服务暴露端口
		JSONArray ports = jsonObject.getJSONArray("ports");
		for (Object object : ports) {
			JSONObject portObject = JSONObject.parseObject(object.toString());
			String targetPort = String.valueOf(portObject.get("targetPort"));
			String protocol = String.valueOf(portObject.get("protocol").toString().toUpperCase());
			String port = String.valueOf(portObject.get("port"));
			String nodePort = String.valueOf(portObject.get("nodePort"));
			String portType = nodePort;
			Port por = new Port(targetPort, port, protocol, portType, nodePort);
			portList.add(por);
		}
		template.binding("ports", portList);
		
		//判断是否有端口被暴露
		String nodeType = null;
		for (Port port : portList) {
			if (StringUtils.hasText(port.getPortType())) {
				nodeType = "true";
				break;
			}
		}
		template.binding("nodeType", nodeType);
		
		//普通端口 targetPort 与 port的值是一致的
		JSONObject containerPortObject = jsonObject.getJSONObject("containerPorts");
		for (Entry<String, Object> entry : containerPortObject.entrySet()) {
			DataMap data = new DataMap();
			data.setKey(entry.getKey());
			data.setValue(String.valueOf(entry.getValue()).toUpperCase());
			containerPorts.add(data);
		}
		//服务内部端口  targetPort 与 port的值是不一致的
		JSONArray nodePorts = jsonObject.getJSONArray("nodePorts");
		for (Object object : nodePorts) {
			JSONObject nodePortObject = JSONObject.parseObject(object.toString());
			String targetPort = String.valueOf(nodePortObject.get("targetPort"));
			String protocol = String.valueOf(nodePortObject.get("protocol").toString().toUpperCase());
			String port = String.valueOf(nodePortObject.get("port"));
			Port por = new Port(targetPort, port, protocol);
			nodePortList.add(por);
		}
		template.binding("containerPorts", containerPorts);
		template.binding("nodePorts", nodePortList);
		
		return template;
	}
	/**
	 * 模板健康检查数据处理
	 * @param jsonObject
	 * @param template
	 * @return
	 */
	private Template makeHealth(JSONObject jsonObject, Template template){
		String livenessType = null;
		String readinessType = null;
		String healthType = null;
		JSONObject healthObject = jsonObject.getJSONObject("healthCheck");
		JSONObject livenessObject = healthObject.getJSONObject("livenessProbe");
		JSONObject readinessObject = healthObject.getJSONObject("readinessProbe");
		if (!livenessObject.isEmpty()) {
			livenessType = "true";
			template.binding("livenessType", livenessObject.getString("type"));
			template.binding("livenessPath", livenessObject.getString("path"));
			template.binding("livenessPort", livenessObject.getString("port"));
			template.binding("livenessInit", livenessObject.getString("initialDelaySeconds"));
			template.binding("livenessTimeout", livenessObject.getString("timeoutSeconds"));
			template.binding("livenessCommand", livenessObject.getJSONArray("command"));
		}
		if (!readinessObject.isEmpty()) {
			readinessType = "true";
			template.binding("readinessType", readinessObject.getString("type"));
			template.binding("readinessPath", readinessObject.getString("path"));
			template.binding("readinessPort", readinessObject.getString("port"));
			template.binding("readinessInit", readinessObject.getString("initialDelaySeconds"));
			template.binding("readinessTimeout", readinessObject.getString("timeoutSeconds"));
			template.binding("readinessCommand", readinessObject.getJSONArray("command"));
		}
		if (StringUtils.hasText(livenessType) || StringUtils.hasText(readinessType)) {
			healthType = "true";
		}
		
		template.binding("health", healthType);
		template.binding("readiness", readinessType);
		template.binding("liveness", livenessType);
		
		return template;
	}
	
	/**
	 * 获取应用信息
	 * @param applicationId
	 * @return
	 */
	private Application getApplication(Long applicationId){
		Application application;
		try {
			application = applicationDao.get(Application.class, applicationId);
			return application;
		} catch (Exception e) {
			logger.error("获取应用名称失败.", e);
			return null;
		}
	}
	/**
	 * 获取Deployment部署模板
	 * @return
	 */
	private Template template(){
		FileResourceLoader resourceLoader = new FileResourceLoader(FileUtil.filePath("resource_template"), "utf-8");
		GroupTemplate groupTemplate = null;
		try {
			groupTemplate = new GroupTemplate(resourceLoader, Configuration.defaultConfiguration());
		} catch (IOException e) {
			logger.error("create GroupTemplate failed.", e);
			return null;
		}
		return groupTemplate.getTemplate("deployment_basic.yaml");
	}
	/**
	 * 保存服务依赖信息
	 * @param jsonObject
	 * @param name 应用名称
	 * @return
	 */
	public boolean save(JSONObject jsonObject, String name){
		int deleted = 0;
		//保存服务依赖关系
		JSONArray relys = jsonObject.getJSONArray("rely");
		ServiceRelyInfo serviceRelyInfo = new ServiceRelyInfo();
		serviceRelyInfo.setCurrentName(jsonObject.get("name").toString());
		serviceRelyInfo.setCurrentNamespace("application-" + name);
		if (!ListTool.isEmpty(relys)) {
			for (Object object : relys) {
				serviceRelyInfo.setRelyName(JSONObject.parseObject(object.toString()).getString("name"));
				serviceRelyInfo.setRelyNamespace(JSONObject.parseObject(object.toString()).getString("namespace"));
				try {
					boolean result = serviceRelyInfoDao.insert(serviceRelyInfo);
					if (!result) {
						logger.error("save service rely fail");
						continue;
					} 
					deleted ++;
				} catch (Exception e) {
					logger.error("save service rely exception", e);
					continue;
				}
			}
		}
		if (deleted != relys.size()) {
			return false;
		}
		return true;
	}

	@Override
	public BsmResult scale(Long applicationId, String name, Integer number,
			Long userId) {
		//校验修改的实例数是否小于告警值
		Integer alarmNum = 0;
		// 获取服务告警策略值
		ServiceAlarm serviceAlarm = getServiceAlarm(applicationId, name);
		if (null != serviceAlarm) {
			alarmNum = serviceAlarm.getNumber();
		}
		if (null != alarmNum && 0 != alarmNum && number < alarmNum) {
			return new BsmResult(false, "修改的实例数不能低于服务告警策略值，可以先修改告警值后，再修改服务实例数");
		}
		
		// 获取namespace信息
		String namespace = getNamespace(applicationId);
		
		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "资源环境未获取到");
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// 执行实例数调整代码
				boolean scaleResult = client.scale(namespace, name, number);
				if (scaleResult) {
					logger.info("弹性伸缩-服务扩展状态成功");
				} else {
					logger.info("弹性伸缩-服务扩展状态失败");
				}
				client.close();
			}
		}).start();
		
		return new BsmResult(true, "弹性伸缩任务已经下发，正在执行……");
	}
	

	@Override
	public BsmResult scaleResource(Long applicationId, String name, Map<String, Quantity> limits, Map<String, Quantity> request,
			Long userId) {
		// 获取namespace信息
		String namespace = getNamespace(applicationId);

		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "资源环境未获取到");
		}
		// 执行实例数调整代码
		boolean scaleResult = client.scale(namespace, name, limits, request);
		client.close();

		if (scaleResult) {
			return new BsmResult(true, "服务扩展成功");
		}
		
		return new BsmResult(false, "服务扩展失败");

	}
	
	@Override
	public BsmResult resource(Long id, String name) {
		// 获取namespace信息
		String namespace = getNamespace(id);

		// 获取ApplicationClient
		ApplicationClient client = build(id);
		if (null == client) {
			return new BsmResult(false, "未获取到服务归属环境的链接");
		}
		Deployment deployment = (Deployment) client.detail(namespace, name, ApplicationEnum.RESOURCE.DEPLOYMENT);
		ResourceRequirements resource = null;
		if (null != deployment) {
			resource = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getResources();
		}else {
			ReplicationController rc = (ReplicationController) client.detail(namespace, name, ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
			resource = rc.getSpec().getTemplate().getSpec().getContainers().get(0).getResources();
		}
		PodInfo podResourceInfo = new PodInfo();
		if (null != resource.getLimits()) {
			if (null != resource.getLimits().get("cpu")) {
				podResourceInfo.setCpuLimit(resource.getLimits().get("cpu").getAmount());
			}
			if (null != resource.getLimits().get("memory")) {
				podResourceInfo.setMemoryLimit(resource.getLimits().get("memory").getAmount());
			}
		}
		if (null != resource.getRequests()) {
			if (null != resource.getRequests().get("cpu")) {
				podResourceInfo.setCpuRequest(resource.getRequests().get("cpu").getAmount());
			}
			if (null != resource.getRequests().get("memory")) {
				podResourceInfo.setMemoryRequest(resource.getRequests().get("memory").getAmount());
			}
		}
		return new BsmResult(true, podResourceInfo, "获取组件pod资源信息成功！");
	}

	@Override
	public BsmResult rolling(Long applicationId, String name, String image, Long userId) {
		BsmResult bsmResult = new BsmResult(false,"");
		// 获取namespace信息
		String namespace = getNamespace(applicationId);
		
		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "未获取到环境资源连接");
		}
		// 执行滚动变更镜像的代码
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean isSuccess = rolling(client, namespace, name, image);
				if (!isSuccess) {
					logger.info("滚动升级状态失败！");
				} else {
					logger.info("滚动升级状态成功！");
				}
			}
			
		}).start();
		bsmResult.setSuccess(true);
		bsmResult.setMessage("滚动升级任务已经下发，正在执行……");
		return bsmResult;
		
	}
	
	@Override
	public BsmResult rollBack(Long applicationId, String name, String paramInfo,Long userId) {
		// 获取namespace信息
		String namespace = getNamespace(applicationId);

		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "服务资源连接异常");
		}
		
		//回滚操作
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean isSuccess = client.rollBack(namespace, name, paramInfo);
				if (isSuccess) {
					logger.info("回滚状态成功");
				} else {
					logger.info("回滚状态失败");
				}
			}
			
		}).start();

		return new BsmResult(true, "回滚任务已经下发，正在执行……");
	}

	@Override
	public void autoScale(Long applicationId, String name, Map<String, Object> policy) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean remove(Long applicationId, String name) {
		// 获取namespace信息
		String namespace = getNamespace(applicationId);

		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return false;
		}

		// 根据服务名称删除服务信息
		boolean removed = client.remove(namespace, name, ApplicationEnum.RESOURCE.SERVICE)
				&& client.remove(namespace, name, ApplicationEnum.RESOURCE.INGRESS)
				&& client.remove(namespace, name, ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER)
				&& client.remove(namespace, name, ApplicationEnum.RESOURCE.DEPLOYMENT);
		client.close();

		return removed;
	}

	@Override
	public BsmResult remove(Long applicationId, List<String> names) {
		List<String> deployNames = new ArrayList<>();
		List<String> rcNames = new ArrayList<>();
		List<String> sfsNames = new ArrayList<>();
		List<String> pvcNames = new ArrayList<>();
		List<String> hpaNames = new ArrayList<>();
		
		// 获取namespace信息
		String namespace = getNamespace(applicationId);
		
		//服务删除前判断，服务是否被别的服务依赖
		String result = checkRely(names, namespace);
		if (StringUtils.hasText(result)) {
			return new BsmResult(false, "服务["+result+"]被其他服务依赖，请先解除服务依赖关系后，再删除！");
		}
		
		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "服务资源连接异常");
		}
		
		//获取有效的pvc的名称
		for (String name : names) {
			//获取该服务资源对象信息
			io.fabric8.kubernetes.api.model.Service service = (io.fabric8.kubernetes.api.model.Service)
					client.detail(namespace, name, ApplicationEnum.RESOURCE.SERVICE);
			Map<String, String> selector = service.getSpec().getSelector();
			
			//获取该服务下的pod
			PodList podList = (PodList) client.list(namespace, selector, ApplicationEnum.RESOURCE.POD);
			if (null == podList) {
				continue;
			}
			List<Pod> pods = podList.getItems();
			if (ListTool.isEmpty(pods)) {
				continue;
			}
			for (Pod pod : pods) {
				List<io.fabric8.kubernetes.api.model.Volume> volumes = pod.getSpec().getVolumes();
				if (ListTool.isEmpty(volumes)) {
					continue;
				}
				//获取该pod下的pvc
				for (io.fabric8.kubernetes.api.model.Volume volume : volumes) {
					PersistentVolumeClaimVolumeSource pvc = volume.getPersistentVolumeClaim();
					if (null == pvc) {
						continue;
					}
					pvcNames.add(pvc.getClaimName());
				}
			}
		}
		
		//删除前先判断deploy和 rc存在才删除，否则这个name不属于rc或者deploy, 而对此执行删除,会返回false
		
		for (String name : names) {
			StatefulSet sfs= (StatefulSet) client.detail(namespace, name, ApplicationEnum.RESOURCE.STATEFULSETS);
			Deployment deployment = (Deployment) client.detail(namespace, name, ApplicationEnum.RESOURCE.DEPLOYMENT);
			if (null != sfs) {
				sfsNames.add(name);
			} else if (null != deployment) {
				deployNames.add(name);
			} else {
				rcNames.add(name);
			}
		}
		
		//获取hpa
		for (String name : names) {
			if (null != client.detail(namespace, name, ApplicationEnum.RESOURCE.HORIZONTALPODAUTOSCALER)) {
				hpaNames.add(name);
			}
		}

		// 根据服务名称批量删除服务信息
		boolean removed = client.remove(namespace, names, ApplicationEnum.RESOURCE.SERVICE)
				&& client.remove(namespace, names) //删除ingress
				&& client.remove(namespace, pvcNames, ApplicationEnum.RESOURCE.PERSISTENTVOLUMECLAIMS)
				&& client.remove(namespace, rcNames, ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER)
				&& client.remove(namespace, deployNames, ApplicationEnum.RESOURCE.DEPLOYMENT)
				&& client.remove(namespace, sfsNames, ApplicationEnum.RESOURCE.STATEFULSETS)
		        && client.remove(namespace, hpaNames, ApplicationEnum.RESOURCE.HORIZONTALPODAUTOSCALER);
		
		client.close();
		
		//删除服务之间的依赖关系
		if (removed) {
//			//删除服务部署历史记录信息
//			if (!deleteDeployHistory(names, namespace)) {
//				return new BsmResult(false, "删除服务资源成功，删除服务部署历史记录信息失败");
//			}
			//删除服务告警策略
			deleteServiceAlarm(applicationId, names);
			
			if (!removeGplot(namespace, names)) {
				return new BsmResult(false, "删除服务资源成功，解除服务依赖关系失败");
			}
			return new BsmResult(true, "删除成功");
		}
		return new BsmResult(false, "删除服务资源失败");

	}
	/**
	 * 删除服务下的服务告警策略信息
	 * @param applicationId
	 * @param name
	 * @return
	 */
	private boolean deleteServiceAlarm(Long applicationId, List<String> names){
		int count = 0;
		if (ListTool.isEmpty(names)) {
			return true;
		}
		for (String name : names) {
			try {
				ServiceAlarm serviceAlarm = serviceAlarmDao.detail(applicationId, name);
				if (null != serviceAlarm) {
					 boolean deleted = serviceAlarmDao.delete(serviceAlarm);
					 if (deleted) {
						 count ++;
					} else {
						logger.warn("delete service alarm name = "+name+" fail ");
					}
				}
				logger.warn("service alarm name = "+name+" is not exit ");
			} catch (Exception e) {
				logger.error("删除服务 ["+name+"] 服务告警策略异常", e);
			}
		}
		
		if (count < names.size()) {
			logger.warn("删除一些服务的服务告警策略失败");
			return false;
		}
		
		return true;
	}
	/**
	 * 删除服务部署历史记录信息
	 * @param names
	 * @param namespace
	 * @return
	 */
//	private boolean deleteDeployHistory(List<String> names, String namespace){
//		String[] fileds = null;
//		int deleted = 0;
//		for (String name : names) {
//			fileds = new String[]{name, namespace};
//			try {
//				if (deployHistoryDao.delete(fileds)) {
//					deleted ++;
//				}
//			} catch (Exception e) {
//				logger.error("delete deploy_hostory exception", e);
//				continue;
//			}
//		}
//		
//		if (deleted == names.size()) {
//			return true;
//		}
//		return false;
//	}
	/**
	 * 检测这个服务是否被其他服务依赖
	 * @param name
	 * @param namespace
	 * @return
	 */
	private String checkRely(List<String> names, String namespace){
		StringBuffer buffer = new StringBuffer();
		for (String name : names) {
			try {
				List<ServiceRelyInfo> list = serviceRelyInfoDao.list(name, namespace);
				if (!ListTool.isEmpty(list)) {
					if (buffer.length() > 0) {
						buffer.append(",");
					}
					buffer.append(name);
				}
			} catch (Exception e) {
				logger.error("Get service relyed exception", e);
				return null;
			}
		}
		return buffer.toString();
	}

	/**
	 * 删除服务依赖
	 * @param namespace
	 * @param names
	 * @return
	 */
	public boolean removeGplot(String namespace, List<String> names){
		int deleted = 0;
		for (String name : names) {
			try {
				List<ServiceRelyInfo> serviceRelyInfos = serviceRelyInfoDao.select(name, namespace);
				if (!ListTool.isEmpty(serviceRelyInfos)) {
					for (ServiceRelyInfo serviceRelyInfo : serviceRelyInfos) {
						if (serviceRelyInfoDao.delete(serviceRelyInfo)) {
							deleted++;
						}
					}
					if (serviceRelyInfos.size() != deleted) {
						logger.warn("delete service rely ["+namespace+"/"+name+"] from database failed, please check jdbc environment");
						return false;
					}
				}
			} catch (Exception e) {
				logger.error("delete service rely ["+namespace+"/"+name+"] from database exception, please check jdbc environment");
				return false;
			}
		}
		return true;
	}

	/**
	 * 构建资源链接的client
	 * 
	 * @param applicationId
	 * @return
	 */
	private ApplicationClient build(Long applicationId) {
		// 获取proxy信息
		JSONObject proxy = getProxy(applicationId);
		if (null == proxy) {
			logger.warn("Get environment proxy is null");
			return null;
		}
		String url = proxy.getString("url"), port = proxy.getString("port");

		// 获取详细的资源信息，包括service和deployment
		ApplicationClient client = new ApplicationClient(url, port);
		return client;
	}

	/**
	 * 获取kubernetes环境链接的信息
	 * 
	 * @param applicationId
	 * @return
	 */
	private JSONObject getProxy(Long applicationId) {
	    Application application = getApplication(applicationId);
		if (null == application) {
			return null;
		}
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			logger.warn("环境不存在或环境不可用");
			return null;
		}
		JSONObject proxy = new JSONObject();
		proxy.put("url", environment.getProxy());
		proxy.put("port", environment.getPort());
		return proxy;

	}
	/**
	 * 获取环境对象
	 * @param envId
	 * @return
	 */
	private Environment getEnv(Long envId){
		try {
			return environmentDao.get(Environment.class, envId);
		} catch (Exception e) {
			logger.error("get Environment info exception.", e);
			return null;
		}
	}

	/**
	 * 获取编排模板信息，创建文件
	 * 
	 * @param layoutId
	 * @return
	 */
	private File layoutFile(Long layoutId) {
		String filePath = "", fileName = "";
		Layout layout = null;
		try {
			layout = layoutDao.query(layoutId);
		} catch (Exception e) {
			logger.debug("get layout file exception", e);
			return null;
		}
		filePath = layout.getFilePath();
		fileName = layout.getFileName();
		File file = new File(filePath + File.separatorChar + fileName);
		return file;

	}

	/**
	 * 获取kubernetes环境下应用的命名空间
	 * 
	 * @param applicationId
	 * @return
	 */
	private String getNamespace(Long applicationId) {
		Application application = getApplication(applicationId);
		return null == application || null == application.getNamespace() ? "default" : application.getNamespace();
	}

	@Override
	public BsmResult gplot(String currentName, String currentNamespace) {
		JSONObject data = new JSONObject();
		JSONArray datas = new JSONArray();
		try {
			List<ServiceRelyInfo> serviceRelyInfos = serviceRelyInfoDao.select(currentName, currentNamespace);
			if (!ListTool.isEmpty(serviceRelyInfos) && null != serviceRelyInfos.get(0).getRelyName()) {
				for (ServiceRelyInfo serviceRelyInfo : serviceRelyInfos) {
					JSONObject object = new JSONObject();
					object.put("sourceName", currentName);
					object.put("sourceCategory", "Computer");//源服务类型
					object.put("targetName", serviceRelyInfo.getRelyName());
					object.put("targetCategory", "Computer");//目标服务类型
					object.put("namespace", serviceRelyInfo.getRelyNamespace());
					datas.add(object);
				}
			}
			data.put("name", currentName);
			data.put("namespace", currentNamespace);
			data.put("resourceRelations", datas);
			return new BsmResult(true, data, "获取成功");
		} catch (Exception e) {
			logger.error("get service rely tree exception!");
		}
		return new BsmResult(false, "获取失败");
	}
	/**
	 * 执行滚动升级
	 * @param client
	 * @param namespace
	 * @param resourceName
	 * @param newImageName
	 * @return
	 */
	private boolean rolling(ApplicationClient client, String namespace, String resourceName, String newImageName){
		boolean isSuccess = false;
		String oldImage = null;
		Deployment deployment = (Deployment) client.detail(namespace, resourceName, ApplicationEnum.RESOURCE.DEPLOYMENT);
		if (null == deployment) {
			ReplicationController rc = (ReplicationController) client.detail(namespace, resourceName,
					ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
			if (null == rc) {
				return isSuccess;
			}
			oldImage = rc.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
			isSuccess = client.rolling(namespace, resourceName, newImageName, oldImage, ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
		} else {
			oldImage = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
			isSuccess = client.rolling(namespace, resourceName, newImageName, oldImage, ApplicationEnum.RESOURCE.DEPLOYMENT);
		}
		return isSuccess;
	}
	
	@Override
	public BsmResult createHpa(Long applicationId, String name, JSONObject hpa, Long userId) {
		String namespace = getNamespace(applicationId);
		hpa.put("namespace", namespace);
		hpa.put("name", name);
		//获取client
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "未获取到资源连接");
		}
		
		//辨别什么类型的部署，进行弹性伸缩
		hpa = getCategory(client, applicationId, hpa);
		
		//获取完整模板数据
		String filePath = FileUtil.filePath("resource_template");
		File file = FileUtil.templateToExecute(filePath, "hpa_basic.yaml",
				hpa, "temporary.yaml");
		
		
		List<HasMetadata> result = client.load(file);
		client.close();
		if (ListTool.isEmpty(result)) {
			return new BsmResult(false, "弹性伸缩失败");
		}
		return new BsmResult(true, "弹性伸缩成功");
	}

	@Override
	public BsmResult hpa(Long applicationId, String name) {
		//获取namespace
		String namespace = getNamespace(applicationId);
		//获取连接
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "资源连接未获取到");
		}
		Object object = client.detail(namespace, name, ApplicationEnum.RESOURCE.HORIZONTALPODAUTOSCALER);
		client.close();
		return new BsmResult(true, JSONObject.toJSON(object), "获取成功");
	}
	/**
	 * 创建hpa时，判断是什么类型的部署  rc/deploy
	 * @param applicationId
	 * @param object
	 * @return
	 */
	private JSONObject getCategory(ApplicationClient client, Long applicationId, JSONObject object){
		String namespace = object.getString("namespace");
		String name = object.getString("name");
		
		Deployment deployment = (Deployment) client.detail(namespace, name, ApplicationEnum.RESOURCE.DEPLOYMENT);
		if (null == deployment) {
			object.put("apiVersion", "v1");
			object.put("kind", "ReplicationController");
		} else {
			object.put("apiVersion", "extensions/v1beta1");
			object.put("kind", "Deployment");
		}
		return object;
	}

	@Override
	public BsmResult removeHpa(Long applicationId, String name) {
		//获取namespace
		String namespace = getNamespace(applicationId);
		//获取连接
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "资源连接未获取到");
		}
		
		boolean deleted = client.remove(namespace, name, ApplicationEnum.RESOURCE.HORIZONTALPODAUTOSCALER);
		if (deleted) {
			return new BsmResult(true, "删除成功");
		}
		return new BsmResult(false, "删除失败");
	}

	@Override
	public BsmResult configMap(Long applicationId, String name, String resourceType) {
		BsmResult bsmResult = new BsmResult();
		List<String> cmNames = null;
		List<ConfigManage> configMaps = new ArrayList<>();
		//获取应用信息
		Application application = getApplication(applicationId);
		if (null == application) {
			bsmResult.setMessage("未获取到该应用信息");
			return bsmResult;
		}
		
		//获取环境信息
		Environment env = getEnv(application.getEnvId());
		if (null == env) {
			bsmResult.setMessage("未获取到环境信息");
			return bsmResult;
		}
		
		//获取连接
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "资源连接未获取到");
		}
		
		switch (resourceType) {
		case "Deployment":
			//获取服务-部署对象
			Deployment deploy = (Deployment) client.detail(application.getNamespace(), name, ApplicationEnum.RESOURCE.DEPLOYMENT);
			//获取服务里的配置实例
			cmNames = getConfigMap(deploy);
			break;
			
		case "Job":
			Job job = (Job)client.detail(application.getNamespace(), name, ApplicationEnum.RESOURCE.JOB);
			//获取服务里的配置实例
			cmNames = getConfigMap(job);
			break;

		default:
			logger.warn("不支持的资源类型");
			break;
		}
		
		//获取配置实例在数据库中的信息
		if (!ListTool.isEmpty(cmNames)) {
			for (String cmName : cmNames) {
				//获取数据库中的配置信息
				ConfigManage configManage = getConfigManage(cmName, StringUtil.convertPinyin(application.getName()).toLowerCase(), env.getName());
				if (null == configManage) {
					continue;
				}
				configMaps.add(configManage);
			}
		}
		
		bsmResult.setMessage("获取成功");
		bsmResult.setSuccess(true);
		bsmResult.setData(configMaps);
		return bsmResult;
	}
	
	/**
	 * 获取服务中的配置实例
	 * @param deploy
	 * @return
	 */
	@SuppressWarnings({ "null", "unchecked" })
	private List<String> getConfigMap(Deployment deploy){
		List<String> cmNames = new ArrayList<>();
		List<Container> containers = deploy.getSpec().getTemplate().getSpec().getContainers();
		if (ListTool.isEmpty(containers)) {
			return cmNames;
		}
		
		for (Container container : containers) {
			//检测envFrom是否引用配置实例
			Map<String, Object> propertiesMap = container.getAdditionalProperties();
			if (null != propertiesMap || !propertiesMap.isEmpty()) {
				for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
					if ("envFrom".equals(entry.getKey())) {
						List<Object> objs = (List<Object>) entry.getValue();
						if (!ListTool.isEmpty(objs)) {
							for (Object object : objs) {
								String jsonString = JSON.toJSONString(object);
								JSONObject value = JSONObject.parseObject(jsonString);
								String cmName = value.getJSONObject("configMapRef").getString("name");
								cmNames.add(cmName);
							}
						}
					}
				}
			}
		}
		
		//检测volume是否引用配置实例
		List<io.fabric8.kubernetes.api.model.Volume> volumes = deploy.getSpec().getTemplate().getSpec().getVolumes();
		if (!ListTool.isEmpty(volumes)) {
			for (io.fabric8.kubernetes.api.model.Volume volume : volumes) {
				ConfigMapVolumeSource configMap = volume.getConfigMap();
				if (null != configMap) {
					cmNames.add(configMap.getName());
				}
			}
		}
		return cmNames;
		
	}
	
	/**
	 * 获取服务中的配置实例
	 * @param deploy
	 * @return
	 */
	@SuppressWarnings({ "null", "unchecked" })
	private List<String> getConfigMap(Job job){
		List<String> cmNames = new ArrayList<>();
		List<Container> containers = job.getSpec().getTemplate().getSpec().getContainers();
		if (ListTool.isEmpty(containers)) {
			return cmNames;
		}
		
		for (Container container : containers) {
			//检测envFrom是否引用配置实例
			Map<String, Object> propertiesMap = container.getAdditionalProperties();
			if (null != propertiesMap || !propertiesMap.isEmpty()) {
				for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
					if ("envFrom".equals(entry.getKey())) {
						List<Object> objs = (List<Object>) entry.getValue();
						if (!ListTool.isEmpty(objs)) {
							for (Object object : objs) {
								String jsonString = JSON.toJSONString(object);
								JSONObject value = JSONObject.parseObject(jsonString);
								String cmName = value.getJSONObject("configMapRef").getString("name");
								cmNames.add(cmName);
							}
						}
					}
				}
			}
		}
		
		//检测volume是否引用配置实例
		List<io.fabric8.kubernetes.api.model.Volume> volumes = job.getSpec().getTemplate().getSpec().getVolumes();
		if (!ListTool.isEmpty(volumes)) {
			for (io.fabric8.kubernetes.api.model.Volume volume : volumes) {
				ConfigMapVolumeSource configMap = volume.getConfigMap();
				if (null != configMap) {
					cmNames.add(configMap.getName());
				}
			}
		}
		return cmNames;
		
	}
	
	/**
	 * 获取配置实例对象
	 * @param name 配置实例名称
	 * @param appName 应用名称
	 * @param envName 环境名称
	 * @return
	 */
	private ConfigManage getConfigManage(String name, String appName, String envName){
		ConfigManage configManage = null;
		try {
			configManage = configManageDao.detail(name, appName, envName);
		} catch (Exception e) {
			logger.error("获取配置实例数据库信息异常", e);
		}
		
		return configManage;
	}

	@Override
	public BsmResult serviceAlarmDetail(Long applicationId, String name) {
		BsmResult bsmResult = new BsmResult(false, "获取服务告警策略信息失败");
		ServiceAlarm serviceAlarm = null;
		try {
			serviceAlarm = serviceAlarmDao.detail(applicationId, name);
			bsmResult.setMessage("获取成功");
			bsmResult.setSuccess(true);
			bsmResult.setData(serviceAlarm);
		} catch (Exception e) {
			logger.error("获取服务实例数告警策略数据库信息异常", e);
		}
		return bsmResult;
	}

	@Override
	public BsmResult serviceAlarmSave(ServiceAlarm serviceAlarm) {
		BsmResult bsmResult = new BsmResult(false, "设置实例数告警策略失败");
		boolean saved = false;
		try {
			serviceAlarm.setStatus(String.valueOf(ServiceAlarm.AlarmStatus.NORMAL.ordinal()));//默认正常状态
			saved = serviceAlarmDao.insert(serviceAlarm);
			if (saved) {
				bsmResult.setMessage("设置成功");
				bsmResult.setSuccess(true);
			}
		} catch (Exception e) {
			logger.error("设置服务实例数告警策略异常", e);
		}
		return bsmResult;
	}

	@Override
	public BsmResult serviceAlarmModify(ServiceAlarm serviceAlarm) {
		BsmResult bsmResult = new BsmResult(false, "修改实例数告警策略失败");
		boolean updated = false;
		try {
			updated = serviceAlarmDao.update(serviceAlarm);
			if (updated) {
				bsmResult.setMessage("修改成功");
				bsmResult.setSuccess(true);
			}
		} catch (Exception e) {
			logger.error("修改服务实例数告警策略异常", e);
		}
		return bsmResult;
	}
	
	@SuppressWarnings("unused")
	@Override
	public void serviceAlarmMonitor() {
		ApplicationClient client = null;
		try {
			logger.info("服务警告策略检测开始......");
			List<ServiceAlarm> serviceAlarms = serviceAlarmDao.select();
			for (ServiceAlarm serviceAlarm : serviceAlarms) {
				
				//获取应用信息
				Application application = getApplication(serviceAlarm.getApplicationId());
				if (null == application) {
					logger.error("未获取到应用信息");
					continue;
				}
				//获取环境信息
				Environment env = getEnv(application.getEnvId());
				if (null == env) {
					logger.error("未获取到环境信息");
					continue;
				}
				
				//获取环境资源连接
				client = new ApplicationClient(env.getProxy(), String.valueOf(env.getPort()));
				if (null == client) {
					logger.error("未获取到环境资源连接");
					continue;
				}
				
				//获取服务信息
				io.fabric8.kubernetes.api.model.Service service = (io.fabric8.kubernetes.api.model.Service) client.detail(application.getNamespace(), 
						serviceAlarm.getName(), ApplicationEnum.RESOURCE.SERVICE);
				Map<String, String> selector = service.getSpec().getSelector();
				
				//获取部署资源对象deply信息
				List<Deployment> deploys = null;
				DeploymentList deployList = (DeploymentList)client.list(application.getNamespace(), 
						selector, ApplicationEnum.RESOURCE.DEPLOYMENT);
				if (null != deployList) {
					deploys = deployList.getItems();
				}
				
				//获取部署资源对象rc信息
				List<ReplicationController> rcs = null;
				ReplicationControllerList rcList = (ReplicationControllerList) client
						.list(application.getNamespace(), selector, ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
				if (null != rcList) {
					rcs = rcList.getItems();
				}
				
				//获取部署资源对象statefulSet信息
				List<StatefulSet> statefulSets = null;
				StatefulSetList sfsList = (StatefulSetList) client
					.list(application.getNamespace(), selector, ApplicationEnum.RESOURCE.STATEFULSETS);
				if (null != sfsList) {
					statefulSets = sfsList.getItems();
				}
				
				Integer availableReplicas = 0;
				if (!ListTool.isEmpty(deploys)) {
					for (Deployment deployment : deploys) {
						availableReplicas = deployment.getStatus().getAvailableReplicas();
						if (null == availableReplicas || availableReplicas == 0 || availableReplicas < serviceAlarm.getNumber()) {
							//邮件发送
							sendEmail(serviceAlarm.getEmail(), application.getName(), 
									env.getName(), service.getMetadata().getName());
							//修改数据库中状态
							serviceAlarm.setStatus(String.valueOf(ServiceAlarm.AlarmStatus.ALARM.ordinal()));
							boolean updated = serviceAlarmDao.update(serviceAlarm);
							if (!updated) {
								logger.warn("修改服务告警策略状态失败");
							}
						}
					}
				}
				
				if (!ListTool.isEmpty(rcs)) {
					for (ReplicationController rc : rcs) {
						availableReplicas = rc.getStatus().getAvailableReplicas();
						if (null == availableReplicas || availableReplicas == 0 || availableReplicas < serviceAlarm.getNumber()) {
							//邮件发送
							sendEmail(serviceAlarm.getEmail(), application.getName(), 
									env.getName(), service.getMetadata().getName());
							//修改数据库中状态
							serviceAlarm.setStatus(String.valueOf(ServiceAlarm.AlarmStatus.ALARM.ordinal()));
							boolean updated = serviceAlarmDao.update(serviceAlarm);
							if (!updated) {
								logger.warn("修改服务告警策略状态失败");
							}
						}
					}
				}
				
				if (!ListTool.isEmpty(statefulSets)) {
					for (StatefulSet sfs : statefulSets) {
						availableReplicas = sfs.getStatus().getReplicas();
						if (null == availableReplicas || availableReplicas == 0 || availableReplicas < serviceAlarm.getNumber()) {
							//邮件发送
							sendEmail(serviceAlarm.getEmail(), application.getName(), 
									env.getName(), service.getMetadata().getName());
							//修改数据库中状态
							serviceAlarm.setStatus(String.valueOf(ServiceAlarm.AlarmStatus.ALARM.ordinal()));
							boolean updated = serviceAlarmDao.update(serviceAlarm);
							if (!updated) {
								logger.warn("修改服务告警策略状态失败");
							}
						}
					}
				}
				
				//当可用实例数恢复正常后，修改数据库状态信息
				if (availableReplicas >= serviceAlarm.getNumber() 
						&& serviceAlarm.getStatus().equals(String.valueOf(ServiceAlarm.AlarmStatus.ALARM.ordinal()))) {
					serviceAlarm.setStatus(String.valueOf(ServiceAlarm.AlarmStatus.NORMAL.ordinal()));
					boolean updated = serviceAlarmDao.update(serviceAlarm);
					if (!updated) {
						logger.warn("修改服务告警策略状态失败");
					}
				}
			}
		} catch (Exception e) {
			logger.error("检测服务可用实例数出现异常", e);
		}
		if (null != client) {
			client.close();
		}
		logger.info("服务警告策略检测结束......");
	}
	/**
	 * 服务告警-邮件发送
	 * @param email
	 * @param appName
	 * @param envName
	 * @param svcName
	 */
	private void sendEmail(String email, String appName, String envName, String svcName){
		String subject = "服务实例数异常告警";
		String receiver = email;
		String mailContent = "环境【"+envName+"】\\ 应用 【"+appName+"】中，"
				+ "服务【"+svcName+"】可用实例数低于服务告警策略值，"
				+ "请及时进行修复！";
		emailSender.send(subject, receiver, mailContent);
	}
	
	/**
	 * 获取服务告警策略信息
	 * @param applicationId
	 * @param name
	 * @return
	 */
	private ServiceAlarm getServiceAlarm(Long applicationId, String name){
		ServiceAlarm serviceAlarm = null;
		try {
			serviceAlarm = serviceAlarmDao.detail(applicationId, name);
		} catch (Exception e) {
			logger.error("获取服务告警策略信息异常", e);
		}
		return serviceAlarm;
	}
	@Override
	public BsmResult listJob(Long applicationId) {
		// 获取namespace信息
		String namespace = getNamespace(applicationId);

		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "未获取到应用归属环境的链接");
		}

		// 获取详细的资源信息列表，包括service和deployment
		JobList jobs = (JobList) client.list(namespace, ApplicationEnum.RESOURCE.JOB);
		
		return new BsmResult(true, jobs.getItems(), "获取成功");
		
	}
	
	@Override
	public BsmResult deleteJob(Long applicationId, String jobName) {
		
		// 获取namespace信息
		String namespace = getNamespace(applicationId);

		// 获取ApplicationClient
		ApplicationClient client = build(applicationId);
		if (null == client) {
			return new BsmResult(false, "未获取到应用归属环境的链接");
		}

		//删除pvc
		boolean removePvc = removePvc(client, namespace, jobName);
		if(!removePvc){
			return new BsmResult(false, "删除批处理任务的pvc失败");
		}

		// 删除批处理任务信息
		boolean removed = client.remove(namespace, jobName, ApplicationEnum.RESOURCE.JOB);
		
		client.close();
		
		//删除服务之间的依赖关系
		if (removed) {
			return new BsmResult(true, "删除成功");
		}
		return new BsmResult(false, "删除失败");

	
	}
	
	/**
	 * 删除job关联的pvc
	 * @param client
	 * @param namespace
	 * @param jobName
	 * @return
	 */
	private boolean removePvc(ApplicationClient client, String namespace, String jobName){
		List<String> pvcNames = new ArrayList<>();
		Job job = (Job)client.detail(namespace, jobName, ApplicationEnum.RESOURCE.JOB);
		Map<String, String> labels = job.getSpec().getTemplate().getMetadata().getLabels();
		
		//获取该服务下的pod
		PodList podList = (PodList) client.list(namespace, labels, ApplicationEnum.RESOURCE.POD);
		if (null != podList) {
			List<Pod> pods = podList.getItems();
			if (!ListTool.isEmpty(pods)) {
				for (Pod pod : pods) {
					List<io.fabric8.kubernetes.api.model.Volume> volumes = pod.getSpec().getVolumes();
					if (ListTool.isEmpty(volumes)) {
						continue;
					}
					//获取该pod下的pvc
					for (io.fabric8.kubernetes.api.model.Volume volume : volumes) {
						PersistentVolumeClaimVolumeSource pvc = volume.getPersistentVolumeClaim();
						if (null == pvc) {
							continue;
						}
						pvcNames.add(pvc.getClaimName());
					}
				}
			}
		}
		return client.remove(namespace, pvcNames, ApplicationEnum.RESOURCE.PERSISTENTVOLUMECLAIMS);
	}
	
}
