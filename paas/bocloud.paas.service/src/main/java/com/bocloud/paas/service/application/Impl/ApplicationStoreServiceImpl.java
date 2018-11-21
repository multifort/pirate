package com.bocloud.paas.service.application.Impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.DateTools;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.common.util.StringUtil;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.application.ApplicationStoreDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.repository.ImageDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.ApplicationStore;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.Image;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.common.enums.RepositoryType;
import com.bocloud.paas.model.Labels;
import com.bocloud.paas.model.Volume;
import com.bocloud.paas.service.application.ApplicationStoreService;
import com.bocloud.paas.service.application.config.ApplicationConfig;
import com.bocloud.paas.service.application.model.DataMap;
import com.bocloud.paas.service.application.model.Port;
import com.bocloud.paas.service.application.util.ApplicationClient;
import com.bocloud.paas.service.repository.ImageService;
import com.bocloud.paas.service.repository.model.ImageInfo;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.api.model.extensions.StatefulSetList;

@Service("applicationStoreService")
public class ApplicationStoreServiceImpl implements ApplicationStoreService {

	private static Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

	@Autowired
	private ApplicationStoreDao applicationStoreDao;

	@Autowired
	private EnvironmentDao environmentDao;

	@Autowired
	private ApplicationDao applicationDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageDao imageDao;

	@Autowired
	private ApplicationConfig config;

	@Value("${applicationStore.picture.storage.path}")
	private String picturePath;
	@Value("${applicationStore.file.storage.path}")
	private String templatePath;

	@Override
	public BsmResult list(String name) {
		List<ApplicationStore> applicationStores = new ArrayList<>();
		try {
			applicationStores = applicationStoreDao.select(name);
		} catch (Exception e) {
			logger.error("Get application components error", e);
		}
		Map<String, List<ApplicationStore>> applicationStoreMap = new HashMap<>();
		if (!applicationStores.isEmpty()) {
			// 根据选择的数据进行分组处理
			for (ApplicationStore ApplicationStore : applicationStores) {
				List<ApplicationStore> storeList = applicationStoreMap.get(ApplicationStore.getType());
				/* 如果取不到数据,那么直接new一个空的ArrayList **/
				if (storeList == null) {
					storeList = new ArrayList<>();
					storeList.add(ApplicationStore);
					applicationStoreMap.put(ApplicationStore.getType(), storeList);
				} else {
					/* 某个applicationStore之前已经存放过了,则直接追加数据到原来的List里 **/
					storeList.add(ApplicationStore);
				}
			}
			return new BsmResult(true, applicationStoreMap, "获取应用组件成功");
		}
		return new BsmResult(true, applicationStoreMap, "获取应用组件成功");
	}

	/**
	 * 说明下, 应用实例部署-应用商店方式部署 envId=null, 应用商店部署：选应用，appId !=null, 不选应用，appId=null
	 **/
	@SuppressWarnings("unused")
	@Override
	public BsmResult deploy(Long envId, Long applicationId, Long storeId, JSONObject paramJson, String deployType,
			RequestUser user) {
		List<HasMetadata> metadatas = null;
		Application application = null;
		String namespace = "application-store";// 默认namespace
		String appName = "store";// 默认部署在"store"应用下

		// 获取商品数据库信息
		ApplicationStore store = getAppStore(storeId);
		if (null == store) {
			return new BsmResult(false, "从数据库里，未获取到该商品信息, 请检测该商品信息是否存在");
		}

		// 判断用户是否选择应用，没有选择应用则默认部署在"store"应用下
		if (null != applicationId) {
			// 获取应用信息
			application = getApplication(applicationId);
			if (null == application) {
				return new BsmResult(false, "未获取到应用信息，应用ID=" + applicationId);
			}

			envId = application.getEnvId();
			namespace = application.getNamespace();
			appName = StringUtil.convertPinyin(application.getName()).toLowerCase();
			paramJson.put("APPLICATION_NAME", appName);
		} else {
			// 创建store应用
			application = createApplication(envId, user);
			applicationId = application.getId();
			if (null == application) {
				return new BsmResult(false, "创建应用失败");
			}
			paramJson.put("APPLICATION_NAME", null);
		}

		// 创建环境连接
		ApplicationClient client = this.build(envId);

		// 组件名称校验
		StringBuffer buffer = checkLayout(client, paramJson.getString("NAME"), namespace);
		if (buffer.length() > 0) {
			return new BsmResult(false, "该应用下资源名称[" + buffer + "]已经存在, 请更改服务名称后再进行部署");
		}

		try {
			// 判断模板：1、判断是平台初始化还是用户上传模板 2、判断是集群还是单节点模板
			JSONObject temObj = judgeTemplate(deployType, store);
			// 生成一个可执行的内容
			String templateContent = templateGenerator(client, appName, paramJson, temObj.getString("fileDir"),
					temObj.getString("templateName"));
			// 创建一个可执行的临时文件
			File newFile = FileUtil.createTemporaryFile(temObj.getString("fileDir"), "simple_template.yaml",
					templateContent);
			if (newFile.getAbsolutePath().contains("simple_template.yaml")) {
				// 服务部署
				metadatas = load(client, newFile, true);
			}

			if (ListTool.isEmpty(metadatas)) {
				return new BsmResult(false, "部署组件失败");
			}

			// 修改应用状态
			if (!modifyStatus(application)) {
				return new BsmResult(true, "组件部署成功,修改应用状态失败");
			}

			// 修改数据库信息
			applicationStoreDao.update(store.getId(), store.getDeployNumber() + 1);
		} catch (Exception e1) {
			logger.error("deploy applicationStore exception", e1);
			return new BsmResult(false, "部署组件失败");
		} finally {
			client.close();
		}

		return new BsmResult(true, applicationId, "组件部署成功");

	}

	/**
	 * 判断模板：1、判断是平台初始化还是用户上传模板 2、判断是集群还是单节点模板
	 * 
	 * @param deployType
	 * @param store
	 * @return
	 */
	private JSONObject judgeTemplate(String deployType, ApplicationStore store) {
		JSONObject object = new JSONObject();
		// 判断平台初始化模板或用户上传模板，来获取模板路径和名称
		String templateName = null;
		String fileDir = null;
		if (StringUtils.hasText(store.getFilePath())) {
			fileDir = store.getFilePath() + File.separatorChar;
			templateName = store.getTemplate();
		} else {
			// 判断单节点部署或集群部署， 来获取模板路径和名称
			Map<String, String> map = deployTypes(deployType, store.getTemplate());
			for (Map.Entry<String, String> entrySet : map.entrySet()) {
				fileDir = FileUtil.filePath("applicationstore/" + entrySet.getKey());
				templateName = entrySet.getValue();
			}
		}
		object.put("templateName", templateName);
		object.put("fileDir", fileDir);
		return object;
	}

	/**
	 * Deployment模板生成
	 * 
	 * @param client
	 * @param appName
	 * @param jsonObject
	 */
	private String templateGenerator(ApplicationClient client, String appName, JSONObject jsonObject, String fileDir,
			String templateName) {
		Template template = template(fileDir, templateName);

		Iterator<String> iterator = jsonObject.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			if (key.equals("volumes")) {
				// 处理下存储卷的格式
				String volumes = jsonObject.getString(key);
				template = volumes(volumes, template, client, jsonObject.getString("NAME"), appName);
				continue;
			}
			if (key.equals("nodeSelector")) {
				// 处理下节点调度节点调度
				String nodeSelector = jsonObject.getString(key);
				template = nodeSelector(nodeSelector, template);
				continue;
			}
			if (key.equals("portsExpose")) {
				// 处理服务端口暴露
				String portsExpose = jsonObject.getString(key);
				template = portsExpose(portsExpose, template);
				continue;
			}
			template.binding(key, jsonObject.get(key));
		}

		String templateContent = template.render();
		return templateContent;
	}

	/**
	 * 端口暴露
	 * 
	 * @param portsExpose
	 * @param template
	 * @return
	 */
	private Template portsExpose(String portsExpose, Template template) {
		List<DataMap> containerPorts = new ArrayList<>();
		List<Port> portList = new ArrayList<>();
		List<Port> nodePortList = new ArrayList<>();

		JSONObject objPortsExpose = JSONObject.parseObject(portsExpose.toString());
		// 服务映射端口
		JSONArray ports = objPortsExpose.getJSONArray("ports");
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
		// 判断是否有端口被暴露
		String nodeType = null;
		for (Port port : portList) {
			if (StringUtils.hasText(port.getPortType())) {
				nodeType = "true";
				break;
			}
		}
		template.binding("NODE_TYPE", nodeType);

		// 如果有端口需要暴露，则不显示不暴露的端口，因为一旦NodePort存在，系统会自动给没有nodePort的端口暴露
		if (!StringUtils.hasText(nodeType)) {
			// 普通端口
			JSONObject containerPortObject = objPortsExpose.getJSONObject("containerPorts");
			for (Entry<String, Object> entry : containerPortObject.entrySet()) {
				DataMap data = new DataMap();
				data.setKey(entry.getKey());
				data.setValue(String.valueOf(entry.getValue()).toUpperCase());
				containerPorts.add(data);
			}

			// 服务内部端口
			JSONArray nodePorts = objPortsExpose.getJSONArray("nodePorts");
			for (Object object : nodePorts) {
				JSONObject nodePortObject = JSONObject.parseObject(object.toString());
				String targetPort = String.valueOf(nodePortObject.get("targetPort"));
				String protocol = String.valueOf(nodePortObject.get("protocol").toString().toUpperCase());
				String port = String.valueOf(nodePortObject.get("port"));
				Port por = new Port(targetPort, port, protocol);
				nodePortList.add(por);
			}
		}

		template.binding("containerPorts", containerPorts);
		template.binding("nodePorts", nodePortList);

		return template;
	}

	/**
	 * 节点调度格式处理
	 * 
	 * @param nodeSelector
	 * @param template
	 * @return
	 */
	private Template nodeSelector(String nodeSelector, Template template) {
		JSONObject objNodeSelector = JSONObject.parseObject(nodeSelector.toString());
		template.binding("nodeSelectorType", objNodeSelector.get("type"));
		List<String> selectors = new ArrayList<String>();
		Set<String> selectorKeys = objNodeSelector.getJSONObject("value").keySet();
		for (String key : selectorKeys) {
			if ("nodeName".equals(key)) {
				template.binding("nodeName", objNodeSelector.getJSONObject("value").get(key));
			} else {
				String label = key + ": " + objNodeSelector.getJSONObject("value").get(key);
				selectors.add(label);
			}
		}
		template.binding("selectors", selectors);

		return template;
	}

	/**
	 * 存储卷格式处理
	 * 
	 * @param volumes
	 * @param template
	 * @param client
	 * @param svnName
	 * @param appName
	 * @return
	 */
	private Template volumes(Object volumes, Template template, ApplicationClient client, String svnName,
			String appName) {
		JSONObject objVolume = JSONObject.parseObject(volumes.toString());
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
						boolean createPvcByPv = client.createPvcByPv(volume.getPv(),
								svnName + "-" + volume.getPv() + "-claim", "application-" + appName);
						if (createPvcByPv) {
							logger.info("success create pv :" + createPvcByPv);
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
	 * 根据文件模板部署服务
	 * 
	 * @param client
	 * @param file
	 *            文件名称
	 * @param flag
	 *            load成功后，是否删除文件 true|删除 、 false|不删除
	 * @return
	 */
	private List<HasMetadata> load(ApplicationClient client, File file, boolean flag) {
		List<HasMetadata> metadatas = null;
		try {
			metadatas = client.load(file);
		} catch (Exception e) {
			logger.error("deploy exception", e);
		}
		// 创建完成之后删除文件
		if (flag) { // 如果true， 删除文件
			if (file.isFile() && file.exists()) {
				file.delete();
			}
		}
		return metadatas;
	}

	/**
	 * 校验编排文件是否有namespace以及值得替换
	 * 
	 * @param client
	 * @param serviceName
	 * @param namespace
	 * @return
	 */
	private StringBuffer checkLayout(ApplicationClient client, String serviceName, String namespace) {
		StringBuffer buffer = new StringBuffer();

		Object service = client.detail(namespace, serviceName, ApplicationEnum.RESOURCE.SERVICE);
		Object deploy = client.detail(namespace, serviceName, ApplicationEnum.RESOURCE.DEPLOYMENT);
		Object rc = client.detail(namespace, serviceName, ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
		if (null != service) {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			buffer.append("Service[" + serviceName + "]");
		}

		if (null != deploy) {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			buffer.append("Deployment[" + serviceName + "]");
		}

		if (null != rc) {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			buffer.append("ReplicationController[" + serviceName + "]");
		}

		return buffer;
	}

	/**
	 * 创建某环境下的应用
	 * 
	 * @param envId
	 * @param reqUser
	 * @return
	 */
	private Application createApplication(Long envId, RequestUser reqUser) {
		try {
			// 判断该环境下的应用是否存在
			Application application = applicationDao.query("store", envId);
			if (null != application) {
				return application;
			}
			application = new Application();
			application.setName("store");
			application.setEnvId(envId);
			application.setNamespace("application-store");

			// 判断环境是否存在 或 状态是否可用
			Environment environment = getEnvironment(envId);
			if (null == environment || !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
				logger.warn("环境信息不存在或环境不可用");
				return null;
			}

			String filePath = FileUtil.filePath("resource_template");
			File file = FileUtil.templateToExecute(filePath, "namespace.yaml", (JSONObject) JSON.toJSON(application),
					"temporary.yaml");
			ApplicationClient client = new ApplicationClient(environment.getProxy(),
					String.valueOf(environment.getPort()));
			client.load(file);
			file.delete();

			// 获取用户信息
			User user = getUser(reqUser.getId());
			if (null != user) {
				application.setDeptId(user.getDepartId());
			}

			application.setStatus(ApplicationEnum.Status.DEPLOY.toString());
			application.setRemark("应用商店");
			application.setDeleted(false);
			application.setCreaterId(reqUser.getId());
			application.setOwnerId(reqUser.getId());
			application.setMenderId(reqUser.getId());
			application.setGmtCreate(new Date());
			application.setGmtModify(new Date());
			application.setStatus("NOT_DEPLOY");

			// 数据库保存信息
			applicationDao.save(application);
			return application;
		} catch (Exception e) {
			logger.error("创建应用失败：" + e);
			return null;
		}
	}

	/**
	 * 修改应用状态、保存服务依赖关系
	 * 
	 * @param application
	 * @return
	 */
	private boolean modifyStatus(Application application) {
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
	 * 获取应用信息
	 * 
	 * @param applicationId
	 * @return
	 */
	private Application getApplication(Long applicationId) {
		Application application = null;
		try {
			application = applicationDao.query(applicationId);

		} catch (Exception e) {
			logger.error("获取应用信息异常，应用ID = " + applicationId);
		}
		return application;
	}

	/**
	 * 获取环境信息
	 * 
	 * @param envId
	 * @return
	 */
	private Environment getEnvironment(Long envId) {
		Environment environment = null;
		try {
			environment = environmentDao.query(envId);

		} catch (Exception e) {
			logger.error("获取环境信息异常，环境ID = " + envId);
		}
		return environment;
	}

	/**
	 * 获取用户信息
	 * 
	 * @param userId
	 * @return
	 */
	private User getUser(Long userId) {
		User user = null;
		try {
			user = userDao.query(userId);
		} catch (Exception e) {
			logger.error("获取当前登录用户信息异常： " + e);
		}
		return user;
	}

	@Override
	public BsmResult detail(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BsmResult content(Long storeId, String deployType) {
		ApplicationStore appStore = getAppStore(storeId);
		if (null == appStore) {
			return new BsmResult(false, "未获取到商品信息");
		}
		// 判断模板：1、判断是平台初始化还是用户上传模板 2、判断是集群还是单节点模板
		JSONObject temObj = judgeTemplate(deployType, appStore);
		String templateName = temObj.getString("templateName");
		String fileDir = temObj.getString("fileDir");

		String fileContent = FileUtil.readFile(fileDir + templateName);

		return new BsmResult(true, fileContent, "获取编排文件内容成功");
	}

	/**
	 * 根据部署类型获取模板路径以及模板名称
	 * 
	 * @param deployType
	 * @param template
	 * @return
	 */
	private Map<String, String> deployTypes(String deployType, String template) {
		Map<String, String> map = new HashMap<>();
		String templatePath = "";
		String name = "";

		String[] names = template.split(",");

		switch (deployType) {
		case "单节点":
			templatePath = "single";
			name = names[0];
			break;

		case "集群":
			templatePath = "cluster";
			if (names.length == 1) {
				name = names[0];
			} else {
				name = names[1];
			}
			break;
		}
		map.put(templatePath, name);
		return map;
	}

	/**
	 * 获取商品信息
	 * 
	 * @param storeId
	 * @return
	 */
	private ApplicationStore getAppStore(Long storeId) {
		try {
			return applicationStoreDao.query(storeId);
		} catch (Exception e) {
			logger.error("获取该商品信息异常，请检查网络连接", e);
			return null;
		}
	}

	/**
	 * 构建资源链接的client
	 * 
	 * @param envId
	 * @return
	 */
	private ApplicationClient build(Long envId) {
		// 获取proxy信息
		JSONObject proxy = getProxy(envId);
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
	 * @param envId
	 * @return
	 */
	private JSONObject getProxy(Long envId) {
		try {
			Environment environment = environmentDao.query(envId);
			if (null == environment || !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
				logger.warn("环境不存在或环境不可用");
				return null;
			}
			JSONObject proxy = new JSONObject();
			proxy.put("namespace", config.getNamespace());
			proxy.put("url", environment.getProxy());
			proxy.put("port", environment.getPort());
			return proxy;
		} catch (Exception e) {
			logger.error("获取proxyUrl失败", e);
			return null;
		}

	}

	@Override
	public BsmResult upload(ApplicationStore applicationStore) {
		BsmResult bsmResult = new BsmResult();
		String fileDir;
		String fileName;
		try {
			// 名称校验
			ApplicationStore checkResult = applicationStoreDao.detail(applicationStore.getName());
			if (null != checkResult) {
				bsmResult.setMessage("该组件名称已经存在");
				return bsmResult;
			}

			// 生成模板文件目录
			fileDir = templatePath + File.separatorChar + DateTools.formatTime2String(new Date(), "yyyyMMddHHmmssSSS")
					+ "-" + applicationStore.getName();
			applicationStore.setFilePath(fileDir);

			// 生成模板文件名称
			fileName = fileDir + File.separatorChar + applicationStore.getName() + ".yaml";
			applicationStore.setTemplate(applicationStore.getName() + ".yaml");

			// 创建模板文件
			bsmResult = FileUtil.createFile(fileName, applicationStore.getContext());
		} catch (Exception e) {
			logger.error("file upload failed", e);
			bsmResult.setSuccess(false);
			bsmResult.setMessage("上传文件异常！");
			return bsmResult;
		}
		if (bsmResult.isSuccess()) {
			// 获取模板镜像版本
			String name = applicationStore.getImage().substring(applicationStore.getImage().lastIndexOf("\\") + 1);
			if (name.contains(":")) {
				applicationStore.setVersion(name.substring(name.lastIndexOf(":") + 1));
			} else {
				applicationStore.setVersion("latest");
			}

			applicationStore.setDeployNumber(0L);
			applicationStore.setFilePath(fileDir);
			applicationStore.setDeleted(false);
			applicationStore.setDeployType("0");// 默认上传的模板为单节点部署方式
			try {
				// 数据库保存信息
				applicationStoreDao.save(applicationStore);
				bsmResult.setSuccess(true);
				bsmResult.setData(applicationStore);
				bsmResult.setMessage("上传商品文件成功！");
			} catch (Exception e) {
				logger.error("applicationStore file save failed", e);
				boolean deleted = FileUtil.deleteDirectory(new File(fileName));
				logger.info("删除文件路径： " + fileName + ", 结果： " + deleted);
				bsmResult.setSuccess(false);
				bsmResult.setMessage("上传商品文件失败！");
			}
		} else {
			boolean deleted = FileUtil.deleteDirectory(new File(fileName));
			logger.info("删除文件路径： " + fileName + ", 结果： " + deleted);
			bsmResult.setSuccess(false);
			bsmResult.setMessage("上传文件异常！");
		}
		return bsmResult;
	}

	@Override
	public BsmResult versionUpgrade(Long applicationStoreId, Long imageId, String deployType) {
		BsmResult bsmResult = new BsmResult();

		try {
			ApplicationStore applicationStore = applicationStoreDao.query(applicationStoreId);

			// 判断模板：1、判断是平台初始化还是用户上传模板 2、判断是集群还是单节点模板
			JSONObject temObj = judgeTemplate(deployType, applicationStore);
			String templateName = temObj.getString("templateName");
			String fileDir = temObj.getString("fileDir");
			String fileName = fileDir + templateName;

			// 获取新镜像的env信息
			BsmResult result = imageService.inspect(imageId);
			if (result.isFailed()) {
				return new BsmResult(false, "未获取到镜像基础信息详情");
			}
			ImageInfo imageInfo = (ImageInfo) result.getData();

			// 获取镜像全名
			String images = imageFullName(imageInfo.getRepositoryType(), imageInfo.getRepositoryAddress(),
					imageInfo.getRepositoryPort(), imageInfo.getName(), imageInfo.getNamespace(), imageInfo.getTag());

			// 升级镜像名称
			String context = FileUtil.imageUpgrade(fileName, "image", images);
			if (!StringUtils.hasText(context)) {
				return new BsmResult(false, "升级组件失败");
			}

			// 修改模板
			boolean successed = FileUtil.modifyFile(fileName, context);
			if (successed) {
				// 修改数据库信息
				String tag = "latest";
				if (StringUtils.hasLength(imageInfo.getTag())) {
					tag = imageInfo.getTag();
				}
				boolean updated = applicationStoreDao.update(applicationStore.getId(), tag);
				if (!updated) {
					bsmResult.setSuccess(false);
					bsmResult.setMessage("升级组件成功！修改数据库信息失败");
				}
				bsmResult.setSuccess(true);
				bsmResult.setMessage("升级组件成功！");
			} else {
				bsmResult.setSuccess(false);
				bsmResult.setMessage("升级组件失败！");
			}
		} catch (Exception e) {
			logger.error("升级组件异常！", e);
			bsmResult.setSuccess(false);
			bsmResult.setMessage("升级组件异常！");
		}
		return bsmResult;
	}

	@Override
	public BsmResult down(Long applicationStoreId) {
		try {
			ApplicationStore applicationStore = applicationStoreDao.query(applicationStoreId);
			if (null == applicationStore) {
				return new BsmResult(false, "该组件信息不存在");
			}

			if (!StringUtils.hasText(applicationStore.getFilePath())) {
				return new BsmResult(false, "该组件为平台初始化数据，不允许删除");
			}

			// 删除文件
			boolean deleted = FileUtil.deleteDirectory(new File(applicationStore.getFilePath()));
			if (!deleted) {
				return new BsmResult(false, "删除组件文件失败");
			}

			if (StringUtils.hasText(applicationStore.getPicturePath())) {
				// 删除图片
				String picturePath = applicationStore.getPicturePath();
				deleted = FileUtil.deleteDirectory(new File(picturePath));
				if (!deleted) {
					return new BsmResult(false, "删除组件图片失败");
				}
			}

			// 逻辑删除数据库信息
			deleted = applicationStoreDao.delete(applicationStoreId);
			if (!deleted) {
				return new BsmResult(false, "删除组件数据库信息失败");
			}
			return new BsmResult(true, "组件下架成功");
		} catch (Exception e) {
			logger.error("组件下架异常", e);
			return new BsmResult(false, "组件下架异常");
		}
	}

	@Override
	public BsmResult readTemplate(Long applicationStoreId, String deployType) {
		ApplicationStore applicationStore = null;
		try {
			applicationStore = applicationStoreDao.query(applicationStoreId);
			if (null == applicationStore) {
				return new BsmResult(false, "组件信息不存在");
			}
		} catch (Exception e) {
			logger.error("获取组件信息异常", e);
			return new BsmResult(false, "获取组件信息异常");
		}

		// 判断模板：1、判断是平台初始化还是用户上传模板 2、判断是集群还是单节点模板
		JSONObject temObj = judgeTemplate(deployType, applicationStore);
		String templateName = temObj.getString("templateName");
		String fileDir = temObj.getString("fileDir");

		// 判断平台初始化模板或用户上传模板，来获取模板路径和名称
		String templateAttribute = FileUtil.readParameters(new File(fileDir + templateName));

		return new BsmResult(true, JSONObject.parseObject(templateAttribute), "读取模板成功");
	}

	@Override
	public BsmResult getTemplateImage(Long applicationStoreId, String deployType) {
		JSONArray array = new JSONArray();
		ApplicationStore applicationStore = null;
		try {
			applicationStore = applicationStoreDao.query(applicationStoreId);
			if (null == applicationStore) {
				return new BsmResult(false, "组件信息不存在");
			}
		} catch (Exception e) {
			logger.error("获取组件信息异常", e);
			return new BsmResult(false, "获取组件信息异常");
		}

		// 判断模板：1、判断是平台初始化还是用户上传模板 2、判断是集群还是单节点模板
		JSONObject temObj = judgeTemplate(deployType, applicationStore);
		String templateName = temObj.getString("templateName");
		String fileDir = temObj.getString("fileDir");

		// 判断平台初始化模板或用户上传模板，来获取模板路径和名称
		String image = FileUtil.getTemplateImage(fileDir + templateName);

		logger.info("获取模板中镜像名称：" + image);
		array.add(image);
		return new BsmResult(true, array, "获取成功");
	}

	@Override
	public BsmResult makeTemplate(Long imageId, JSONObject jsonObject) {
		BsmResult result = imageService.inspect(imageId);
		if (result.isFailed()) {
			return new BsmResult(false, "请先填选镜像");
		}
		ImageInfo imageInfo = (ImageInfo) result.getData();
		jsonObject.put("imageName", imageInfo.getName());
		/**
		 * 获取镜像全名
		 */
		String images = imageFullName(imageInfo.getRepositoryType(), imageInfo.getRepositoryAddress(),
				imageInfo.getRepositoryPort(), imageInfo.getName(), imageInfo.getNamespace(), imageInfo.getTag());
		jsonObject.put("image", images);
		jsonObject.put("envs", imageInfo.getEnv());
		// 获取容器端口
		List<DataMap> portList = new ArrayList<>();
		JSONObject portMap = JSONObject.parseObject(imageInfo.getExposedPort());
		if (null == portMap) {
			return new BsmResult(false, "该镜像内部没有任何端口数据，请上传一个有端口数据的镜像");
		}
		if (null != portMap) {
			for (Entry<String, Object> entry : portMap.entrySet()) {
				DataMap data = new DataMap();
				data.setKey(entry.getKey().split("/")[0]);// port
				data.setValue(String.valueOf(entry.getKey().split("/")[1]).toUpperCase());// TCP
				portList.add(data);
			}
		}

		jsonObject.put("exposedPort", portList);

		String templateGenerator = imageToTemplate(jsonObject);
		return new BsmResult(true, templateGenerator, "获取成功");
	}

	/**
	 * 根据参股类型，获取镜像的全名
	 * 
	 * @param registryType
	 *            仓库类型
	 * @param registryAddress
	 *            仓库地址
	 * @param registryPort
	 *            仓库端口号
	 * @param name
	 *            镜像名称
	 * @param namespace
	 *            镜像工作空间
	 * @param tag
	 *            镜像版本号
	 * @return
	 */
	private String imageFullName(Integer registryType, String registryAddress, Integer registryPort, String name,
			String namespace, String tag) {
		StringBuffer images = new StringBuffer();
		RepositoryType repositoryType = RepositoryType.values()[registryType];
		switch (repositoryType) {
		// DOCKER_REGISTRY
		case DOCKER_REGISTRY:
			images.append(registryAddress + ":" + registryPort + "/");
			if (StringUtils.hasText(namespace)) {
				images.append(namespace + "/");
			}
			images.append(name);
			if (StringUtils.hasText(tag)) {
				images.append(":" + tag);
			}
			break;
		// HARBOR
		case HARBOR:
			images.append(registryAddress + "/");
			if (StringUtils.hasText(namespace)) {
				images.append(namespace + "/");
			}
			images.append(name);
			if (StringUtils.hasText(tag)) {
				images.append(":" + tag);
			}
			break;
		default:
			break;
		}

		return images.toString();
	}

	/**
	 * Deployment模板生成
	 * 
	 * @param jsonObject
	 */
	@SuppressWarnings("unchecked")
	private String imageToTemplate(JSONObject jsonObject) {
		Template template = template(FileUtil.filePath("applicationstore/automation"), "image_to_template.yaml");
		template.binding("NAME", "${NAME}");
		template.binding("IMAGE_NAME", jsonObject.get("imageName"));
		template.binding("IMAGE", jsonObject.get("image"));
		template.binding("REPLICAS", "1");
		template.binding("APPLICATION_NAME", null);
		template.binding("CONTAINER_PORTS", jsonObject.get("exposedPort"));

		// 环境
		List<Labels> envs = new ArrayList<>();
		List<String> envList = JSONObject.parseObject(jsonObject.getString("envs"), List.class);
		for (String str : envList) {
			Labels labels = new Labels(str.split("=")[0], str.split("=")[1]);
			envs.add(labels);
		}

		template.binding("ENVS", envs);

		template = templateGenerate(jsonObject, template);

		String templateContent = template.render();

		return templateContent;
	}

	/**
	 * 根据用户选择的选项，生成对应的模板
	 * 
	 * @param jsonObject
	 */
	private Template templateGenerate(JSONObject jsonObject, Template template) {
		// svc端口
		String svcPortsContext = FileUtil
				.readFile(FileUtil.filePath("applicationstore/automation") + "svc_ports_expose.yaml");
		template.binding("PORT_EXPOSE", svcPortsContext);
		// 标签
		String labelsContext = FileUtil.readFile(FileUtil.filePath("applicationstore/automation") + "labels.yaml");
		template.binding("LABELS", labelsContext);

		// 选择器
		String selectorContext = FileUtil.readFile(FileUtil.filePath("applicationstore/automation") + "selector.yaml");
		template.binding("SELECTOR", selectorContext);

		// namespace 和 labels
		String namespaceContext = FileUtil
				.readFile(FileUtil.filePath("applicationstore/automation") + "namespaceLabels.yaml");
		template.binding("NAMESPACE_LABELS", namespaceContext);

		// 节点调度
		template.binding("NODE_TYPE", jsonObject.getString("NODE_TYPE"));
		if (StringUtils.hasText(jsonObject.getString("NODE_TYPE"))) {
			String nodeSelectorContext = FileUtil
					.readFile(FileUtil.filePath("applicationstore/automation") + "nodeSelector.yaml");
			template.binding("NODE_SELECTOR", nodeSelectorContext);
		}

		// 资源配置
		template.binding("RESOURCE_TYPE", jsonObject.getString("RESOURCE_TYPE"));
		if (StringUtils.hasText(jsonObject.getString("RESOURCE_TYPE"))) {
			String resourceContext = FileUtil
					.readFile(FileUtil.filePath("applicationstore/automation") + "resource.yaml");
			template.binding("RESOURCE", resourceContext);
		}

		// 储存卷
		template.binding("VOLUME_TYPE", jsonObject.getString("VOLUME_TYPE"));
		if (StringUtils.hasText(jsonObject.getString("VOLUME_TYPE"))) {
			String volumeContext = FileUtil.readFile(FileUtil.filePath("applicationstore/automation") + "volume.yaml");
			String volumeMountContext = FileUtil
					.readFile(FileUtil.filePath("applicationstore/automation") + "volumeMount.yaml");
			template.binding("VOLUME", volumeContext);
			template.binding("VOLUME_MOUNT", volumeMountContext);
		}
		return template;
	}

	/**
	 * 获取组件部署模板
	 * 
	 * @return
	 */
	private Template template(String fileDir, String fileName) {
		FileResourceLoader resourceLoader = new FileResourceLoader(fileDir, "utf-8");
		GroupTemplate groupTemplate = null;
		try {
			groupTemplate = new GroupTemplate(resourceLoader, Configuration.defaultConfiguration());
		} catch (IOException e) {
			logger.error("create GroupTemplate failed.", e);
			return null;
		}
		return groupTemplate.getTemplate(fileName);
	}

	@Override
	public BsmResult getZookeeper(Long envId, Long applicationId, String type, RequestUser user) {
		JSONArray zks = new JSONArray();
		Application application = null;
		// 如果应用实例-添加服务入口；envId==null, appId!=null
		if (null != applicationId) {
			application = getApplication(applicationId);
			envId = application.getEnvId();
		}

		// 获取环境连接,获取资源信息
		ApplicationClient client = build(envId);
		ServiceList serviceList = (ServiceList) client.list(ApplicationEnum.RESOURCE.SERVICE);
		List<io.fabric8.kubernetes.api.model.Service> items = serviceList.getItems();
		if (ListTool.isEmpty(items)) {
			return new BsmResult(true, zks, "获取成功");
		}

		for (io.fabric8.kubernetes.api.model.Service service : items) {
			JSONObject zkObject = null;
			String name = service.getMetadata().getName();
			String namespace = service.getMetadata().getNamespace();

			// 匹配zk服务，获取zk服务
			if (name.matches(".*(?i)(zk|zookeeper).*")) {

				// 获取应用名称
				application = getApplication(envId, namespace);
				if (null == application) {
					logger.warn("application[namespace is " + namespace + ", envId is " + envId
							+ "] info is null, please check dataSource info");
					continue;
				}

				// 获取该svc的选择器标签
				Map<String, String> selector = service.getSpec().getSelector();
				switch (type) {
				case "单节点":
					zkObject = getSingleZK(client, namespace, selector, 
							StringUtil.convertPinyin(application.getName()).toLowerCase(), name);
					break;

				case "集群":
					zkObject = getClusterZK(client, namespace, selector, 
							StringUtil.convertPinyin(application.getName()).toLowerCase(), name);
					break;
				}
				zks.add(zkObject);
			}
		}

		return new BsmResult(true, zks, "获取成功");
	}

	/**
	 * 获取单节点的zk服务名称
	 * 
	 * @param client
	 * @return
	 */
	private JSONObject getSingleZK(ApplicationClient client, String namespace, Map<String, String> selector,
			String appName, String svcName) {
		JSONObject object = new JSONObject();

		DeploymentList deployList = (DeploymentList) client.list(namespace, selector,
				ApplicationEnum.RESOURCE.DEPLOYMENT);
		if (!ListTool.isEmpty(deployList.getItems())) {
			String deployName = deployList.getItems().get(0).getMetadata().getName();
			object.put("name", svcName + "/" + appName);
			object.put("ZK_NAME", deployName);
			object.put("ZK_SVC_NAME", svcName + "." + namespace);
		}
		return object;
	}

	/**
	 * 获取集群的zk服务名称
	 * 
	 * @param client
	 * @return
	 */
	private JSONObject getClusterZK(ApplicationClient client, String namespace, Map<String, String> selector,
			String appName, String svcName) {
		JSONObject object = new JSONObject();
		StatefulSetList sfsList = (StatefulSetList) client.list(namespace, selector,
				ApplicationEnum.RESOURCE.STATEFULSETS);
		if (!ListTool.isEmpty(sfsList.getItems())) {
			String sfsName = sfsList.getItems().get(0).getMetadata().getName();
			object.put("name", svcName + "/" + appName);
			object.put("ZK_NAME", sfsName);
			object.put("ZK_SVC_NAME", svcName + "." + namespace);
		}

		return object;
	}

	/**
	 * 获取环境信息
	 * 
	 * @param envId
	 * @param namespace
	 * @return
	 */
	private Application getApplication(Long envId, String namespace) {
		Application application = null;
		try {
			application = applicationDao.detail(envId, namespace);
		} catch (Exception e) {
			logger.error("Get appication info exception ", e);
		}
		return application;
	}

	@Override
	public BsmResult getImagePort(Long storeId, String deployType) {
		BsmResult bsmResult = new BsmResult();
		ApplicationStore store = getAppStore(storeId);
		if (null == store) {
			bsmResult.setMessage("该组件信息不存在");
			return bsmResult;
		}

		// 判断模板：1、判断是平台初始化还是用户上传模板 2、判断是集群还是单节点模板
		JSONObject temObj = judgeTemplate(deployType, store);
		String templateName = temObj.getString("templateName");
		String fileDir = temObj.getString("fileDir");

		// 读模板，获取镜像全名
		String imageName = FileUtil.getTemplateImage(fileDir + templateName);

		// 将镜像名称进行解析
		JSONObject object = analysisImage(imageName);

		// 从数据库中获取该镜像的ID,并获取镜像的端口
		String address = object.getString("ip"), namespace = object.getString("namespace"),
				name = object.getString("name"), tag = object.getString("tag");
		ImageInfo imageInfo = getImageDetail(address, namespace, name, tag);
		if (null == imageInfo) {
			bsmResult.setMessage("未获取到镜像详情, 可能是不存在这样的仓库或该仓库中无此镜像");
			return bsmResult;
		}

		// 获取镜像端口号
		List<DataMap> portList = new ArrayList<>();
		JSONObject portMap = JSONObject.parseObject(imageInfo.getExposedPort());

		if (null != portMap) {
			for (Entry<String, Object> entry : portMap.entrySet()) {
				DataMap data = new DataMap();
				data.setKey(entry.getKey().split("/")[0]);// port
				data.setValue(entry.getKey().split("/")[1]);// tcp
				portList.add(data);
			}
		}

		bsmResult.setData(portList);
		bsmResult.setSuccess(true);
		return bsmResult;
	}

	/**
	 * 获取镜像详情
	 * 
	 * @param address
	 * @param namespace
	 * @param name
	 * @param tag
	 * @return
	 */
	private ImageInfo getImageDetail(String address, String namespace, String name, String tag) {
		try {
			Image image = imageDao.query(address, namespace, name, tag);
			if (null == image) {
				logger.warn(
						"数据库中不存在：地址[" + address + "] 项目[" + namespace + "] 名称[" + name + "] 版本号[" + tag + "] 这样的镜像");
				return null;
			}
			BsmResult result = imageService.inspect(image.getId());
			if (result.isFailed()) {
				logger.warn(result.getMessage());
				return null;
			}
			return (ImageInfo) result.getData();

		} catch (Exception e) {
			logger.error("从数据库中获取：地址[" + address + "] 项目[" + namespace + "] 名称[" + name + "] 版本号[" + tag + "] 的镜像异常",
					e);
			return null;
		}
	}

	/**
	 * 解析镜像
	 * 
	 * @param imageName
	 */
	private JSONObject analysisImage(String imageName) {
		JSONObject object = new JSONObject();
		String ip = null;
		String port = null;
		String namespace = null;
		String name = null;
		String tag = null;

		if (imageName.contains("'")) {
			imageName = imageName.replace("'", "");
		}
		String[] fields = imageName.split("/");

		String address = fields[0];
		if (address.contains(":")) {
			ip = address.split(":")[0];
			port = address.split(":")[1];
		} else {
			ip = address;
		}

		if (fields.length == 3) {
			namespace = fields[1];
		}

		if (fields[2].contains(":")) {
			name = fields[2].split(":")[0];
			tag = fields[2].split(":")[1];
		} else {
			name = fields[2];
		}
		object.put("ip", ip);
		object.put("port", port);
		object.put("namespace", namespace);
		object.put("name", name);
		object.put("tag", tag);

		return object;
	}

}
