package com.bocloud.paas.service.application.Impl;

import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LimitRange;
import io.fabric8.kubernetes.api.model.LimitRangeItem;
import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.DaemonSet;
import io.fabric8.kubernetes.api.model.extensions.DaemonSetList;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressList;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.common.util.StringUtil;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.application.ApplicationImageInfoDao;
import com.bocloud.paas.dao.application.ApplicationLayoutInfoDao;
import com.bocloud.paas.dao.application.ConfigManageDao;
import com.bocloud.paas.dao.application.ServiceAlarmDao;
import com.bocloud.paas.dao.application.ServiceRelyInfoDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.ApplicationImageInfo;
import com.bocloud.paas.entity.ApplicationLayoutInfo;
import com.bocloud.paas.entity.ConfigManage;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.Monitor;
import com.bocloud.paas.entity.ServiceAlarm;
import com.bocloud.paas.entity.ServiceRelyInfo;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.common.enums.ResourceQuotaEnum;
import com.bocloud.paas.service.application.ApplicationService;
import com.bocloud.paas.service.application.model.DataMap;
import com.bocloud.paas.service.application.util.ApplicationClient;
import com.bocloud.paas.service.application.util.ApplicationResource;
import com.bocloud.paas.service.application.util.MonitorClient;
import com.bocloud.paas.service.environment.EnvironmentService;
import com.bocloud.paas.service.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Maps;

/**
 * @author zjm
 * @date 2017年3月17日
 */
@Service("applicationService")
public class ApplicationServiceImpl implements ApplicationService {

	private static Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);
	@Autowired
	private ApplicationDao applicationDao;
	@Autowired
	private ApplicationLayoutInfoDao applicationLayoutInfoDao;
	@Autowired
	private ApplicationImageInfoDao applicationImageInfoDao;
	@Autowired
	private UserService userService;
	@Autowired
	private UserDao userDao;
	@Autowired
	private EnvironmentDao environmentDao;
	@Autowired
	private MonitorClient monitorClient;
	@Autowired
	private EnvironmentService environmentService;
	@Autowired
	private ServiceRelyInfoDao serviceRelyInfoDao;
	@Autowired
	private ConfigManageDao configManageDao;
	@Autowired
	private ServiceAlarmDao serviceAlarmDao;

	@Override
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser user) {
		List<Application> applications;
		List<SimpleBean> beans;
		GridBean gridBean;
		try {
			//获取当前用户所在的组织机构以及组织机构下的子机构ID
			String deptIds = userService.listDept(user.getId());
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			int total = 0;
			total = applicationDao.count(params, deptIds);
			if (simple) {
				beans = applicationDao.list(params, sorter, deptIds);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				applications = applicationDao.list(page, rows, params, sorter, deptIds);
				// 获取每个应用的服务数量
//				JSONArray array = batch(applications, monitorClient, environmentService);

				gridBean = GridHelper.getBean(page, rows, total, applications);
			}
			return new BsmResult(true, gridBean, "应用查询成功");
		} catch (Exception e) {
			logger.error("list application failure:", e);
			return new BsmResult(false, "应用查询失败");
		}
	}

	/**
	 * 应用列表线程池
	 * 
	 * @param applications
	 * @param monitorClient
	 * @param environmentService
	 * @return
	 * @throws Exception
	 */
//	private JSONArray batch(List<Application> applications, MonitorClient monitorClient,
//			EnvironmentService environmentService) throws Exception {
//		JSONArray objectList = new JSONArray();
//		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
//		CompletionService<BaseResult<Application>> completionService = new ExecutorCompletionService<>(executor);
//		if (ListTool.isEmpty(applications)) {
//			return objectList;
//		}
//		for (Application application : applications) {
//			AppTask task = new AppTask(application, monitorClient, environmentService);
//			completionService.submit(task);
//		}
//		logger.info("查询的应用总数："+applications.size());
//		int completeTask = 0;
//		int timeout = 1000 * 60 * 10;
//		long begingTime = System.currentTimeMillis();
//		while (completeTask < applications.size()) {
//			logger.info("线程池完成数："+completeTask);
//			try {
//				Future<BaseResult<Application>> take = completionService.take();
//				if (null != take) {
//					BaseResult<Application> result = take.get();
//					if (result.isSuccess()) {
//						objectList.add(result.getData());
//					}
//					completeTask++;
//					if (System.currentTimeMillis() - begingTime > timeout) {
//						break;
//					}
//				}
//			} catch (Exception e) {
//				logger.error("线程池获取任务结果信息异常", e);
//				continue;
//			}
//		}
//		executor.shutdown();
//		return objectList;
//	}

	@Override
	public BsmResult create(RequestUser requestUser, Application application) {
		try {
			//校验名称是否包含特殊字符
			if (!application.getName().matches("^[0-9A-Za-z\\-\u4e00-\u9fa5]+$")) {
				return new BsmResult(false, "名称格式不正确，只能输入中文、数字、大小写字母、中划线");
			}
			//校验应用名称是否已经存在
			Application app = applicationDao.query(application.getName(), application.getEnvId());
			if (null != app) {
				logger.warn("应用创建失败！应用名称已经存在！");
				return new BsmResult(false, "应用创建失败！应用名称已经存在！");
			}
			//校验namespace是否存在
			Application detail = applicationDao.detail(application.getEnvId(), "application-" + StringUtil.convertPinyin(application.getName()).toLowerCase());
			if (null != detail) {
				logger.warn("应用创建失败！该应用名称对应的命名空间已经存在！");
				return new BsmResult(false, "应用创建失败！该应用名称对应的命名空间已经存在！");
			}
			
			BsmResult bsmResult = new BsmResult();
			User user = null;
			if (null == (user = queryUser(requestUser.getId(), bsmResult))) {
				return bsmResult;
			}
			Environment environment = environmentDao.query(application.getEnvId());
			if (null == environment) {
				return new BsmResult(false, "应用运行环境不存在，请重新选择");
			}

			// 创建namespace
			application.setNamespace("application-" + StringUtil.convertPinyin(application.getName()).toLowerCase());
			String filePath = FileUtil.filePath("resource_template");
			File file = FileUtil.templateToExecute(filePath, "namespace.yaml", (JSONObject) JSON.toJSON(application),
					"temporary.yaml");
			ApplicationClient client = new ApplicationClient(environment.getProxy(),
					String.valueOf(environment.getPort()));
			client.load(file);
			file.delete();

			// 保存应用信息
			application.setQuotaStatus(String.valueOf(ApplicationEnum.QuotaStatus.NOT_QUOTA));
			application.setStatus(ApplicationEnum.Status.NOT_DEPLOY.toString());
			application.setDeleted(false);
			application.setCreaterId(requestUser.getId());
			application.setGmtCreate(new Date());
			application.setCreaterId(requestUser.getId());
			application.setMenderId(requestUser.getId());
			application.setOwnerId(requestUser.getId());
			application.setTenantId(requestUser.getTenantId());
			application.setDeptId(user.getDepartId());
			applicationDao.save(application);
			return new BsmResult(true, "应用创建成功!");
		} catch (Exception e) {
			logger.error("create application failure:", e);
			return new BsmResult(false, "应用创建失败!");
		}

	}
	
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = Exception.class)
	public BsmResult remove(List<Long> ids) {
		BsmResult bsmResult = new BsmResult(false, "");
		ApplicationClient client = new ApplicationClient();
		for (Long id : ids) {
			try {
				Application application = applicationDao.get(Application.class, id);
				Environment environment = environmentDao.get(Environment.class, application.getEnvId());
				if (null == environment) {
					return new BsmResult(false, "应用运行环境不存在，请重新选择");
				}
				client.setProxyUrl(environment.getProxy());
				client.setPort(String.valueOf(environment.getPort()));
				client.build();
				if (client.remove(null, application.getNamespace(), ApplicationEnum.RESOURCE.NAMESPACE)) {
					applicationDao.delete(Application.class, id);

					// 删除应用和文件编排关系
					deleteAppLay(application.getId());

					// 删除该应用下所有服务依赖关系表
					deleteServiceRely(application.getNamespace());
						
					//解除应用和镜像的关联关系
					delete(application.getId());
						
					//删除coonfigmap在数据库中的信息
					deleteConfigMap(application.getId());
					
					//删除该应用下的所有服务告警策略
					deleteServiceAlarm(id);
						
				} else {
					continue;
				}
			} catch (Exception e) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				logger.error("delete application infos exception", e);
				bsmResult.setMessage("删除应用异常！");
			}
		}
		bsmResult.setSuccess(true);
		bsmResult.setMessage("应用删除成功");
		return bsmResult;
	}
	/**
	 * 删除该应用下的所有服务告警策略
	 * @param id
	 * @return
	 */
	private boolean deleteServiceAlarm(Long id){
		int count = 0;
		//获取应用下的所有服务告警策略 
		List<ServiceAlarm> serviceAlarms = getServiceAlarm(id);
		if (ListTool.isEmpty(serviceAlarms)) {
			return true;
		}
		
		for (ServiceAlarm serviceAlarm : serviceAlarms) {
			try {
				boolean deleted = serviceAlarmDao.delete(serviceAlarm);
				if (deleted) {
					count ++;
					continue;
				}
				logger.warn("删除应用下的服务告警策略失败");
			} catch (Exception e) {
				logger.error("删除应用下的服务告警策略异常", e);
			}
		}
		
		if (count < serviceAlarms.size()) {
			logger.warn("删除应用下的一些服务告警策略失败");
			return false;
		}
		return true;
	}
	/**
	 * 获取应用下的所有服务告警策略
	 * @return
	 */
	private List<ServiceAlarm> getServiceAlarm(Long applicationId){
		List<ServiceAlarm> serviceAlarms = null;
		try {
			serviceAlarms = serviceAlarmDao.select(applicationId);
		} catch (Exception e1) {
			logger.error("获取应用下的所有服务告警策略异常", e1);
		}
		return serviceAlarms;
	}
	
	/**
	 * 删除该应用下的某些配置管理对象
	 * @param appId
	 * @return
	 */
	private boolean deleteConfigMap(Long appId){
		//获取该应用下的所有配置管理对象
		List<ConfigManage> cms = listConfigMap(appId);
		if (ListTool.isEmpty(cms)) {
			return true;
		}
		//执行删除操作
		int count = 0;
		for (ConfigManage configManage : cms) {
			try {
				boolean deleted = configManageDao.delete(ConfigManage.class, configManage.getId());
				if (deleted) {
					count ++;
				} else {
					logger.warn("删除id="+configManage.getId()+"失败");
				}
			} catch (Exception e) {
				logger.error("删除id="+configManage.getId()+"异常");
				continue;
			}
		}
		
		if (count != cms.size()) {
			logger.warn("删除该应用下的某些配置管理对象失败");
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 获取该应用下的所有配置管理对象
	 * @param appId
	 * @return
	 */
	private List<ConfigManage> listConfigMap(Long appId){
		List<ConfigManage> cms = null;
		try {
			cms = configManageDao.list(appId);
		} catch (Exception e) {
			logger.error("获取该应用下的所有配置管理对象异常", e);
		}
		return cms;
	}

	/**
	 * 删除该namespace下所有的服务依赖关系信息
	 * 
	 * @param namespace
	 * @return
	 */
	private boolean deleteServiceRely(String namespace) {
		int deleted = 0;
		try {
			List<ServiceRelyInfo> relys = serviceRelyInfoDao.select(null, namespace);
			if (!ListTool.isEmpty(relys)) {
				for (ServiceRelyInfo serviceRelyInfo : relys) {
					if (serviceRelyInfoDao.delete(serviceRelyInfo)) {
						deleted++;
					}
				}
				if (deleted == relys.size()) {
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("delete service rely exception", e);
		}
		return false;
	}

	public BsmResult modify(Application applicationBean, Long userId) {
		Application application;
		try {
			application = applicationDao.query(applicationBean.getId());
		} catch (Exception e) {
			logger.error("get application error：", e);
			return new BsmResult(false, "应用查询详情失败");
		}
		application.setMenderId(userId);
		application.setGmtModify(new Date());
		application.setRemark(applicationBean.getRemark());
		try {
			if (applicationDao.update(application)) {
				return new BsmResult(true, "应用修改成功");
			} else {
				return new BsmResult(false, "应用修改失败");
			}
		} catch (Exception e) {
			logger.error("modify application exception:", e);
			return new BsmResult(false, "应用修改失败");
		}
	}

	public BsmResult detail(Long id) {
		try {
			Application application = this.applicationDao.query(id);
			if (null == application) {
				return new BsmResult(false, "应用详情查询失败");
			}
			// 获取namespace信息
			String namespace = application.getNamespace();

			// 获取ApplicationClient
			Environment environment = environmentDao.query(application.getEnvId());
			if(null == environment 
					|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())){
				logger.warn("环境不存在或环境不可用");
				return new BsmResult(true, "");
			}
			String url = environment.getProxy(), port = String.valueOf(environment.getPort());
			ApplicationClient client = new ApplicationClient(url, port);

			int serviceNum = ((ServiceList) client.list(namespace, ApplicationEnum.RESOURCE.SERVICE)).getItems().size();
			// TODO 增加运行的pod数量
			application.setServiceNum(serviceNum);
			return new BsmResult(true, application, "应用查询详情成功");
		} catch (Exception e) {
			logger.error("get application exception：", e);
			return new BsmResult(false, "应用查询详情异常");
		}
	}


	/**
	 * 删除应用和文件编排的关系
	 * 
	 * @param applicationLayoutInfo
	 * @return
	 */
	private boolean deleteAppLay(Long applicationId) {
		ApplicationLayoutInfo applicationLayoutInfo = detailAppLay(applicationId);
		if (null == applicationLayoutInfo) {
			return true;
		}
		try {
			boolean isDelete = applicationLayoutInfoDao.delete(applicationLayoutInfo);
			if (isDelete) {
				return true;
			}
			logger.warn("delete application with layOut fail");
		} catch (Exception e) {
			logger.warn("delete application with layOut exception");
		}
		return false;
	}

	/**
	 * 获取应用和编排的关系
	 * 
	 * @param applicationId
	 * @return
	 */
	private ApplicationLayoutInfo detailAppLay(Long applicationId) {
		try {
			return applicationLayoutInfoDao.query(applicationId);
		} catch (Exception e) {
			logger.warn("delete application with layOut exception");
			return null;
		}
	}
	
	/**
	 * 解除应用和镜像的关系
	 * @param applicationId
	 * @return
	 */
	private boolean delete(Long applicationId) {
		int deleted = 0;
		List<ApplicationImageInfo> list = list(applicationId);
		if (ListTool.isEmpty(list)) {
			return true;
		}
		
		for (ApplicationImageInfo applicationImageInfo : list) {
			try {
				
				if (applicationImageInfoDao.delete(applicationImageInfo)) {
					deleted ++;
				}
				
			} catch (Exception e) {
				logger.error("解除应用与镜像关系表异常", e);
				continue;
			}
		}
		
		if (deleted == list.size()) {
			return true;
		}
		return false;
	}


	@Transactional
	private boolean removeAppInfos(Long id, Long userId) {
		try {
			return applicationDao.delete(id, userId) && applicationDao.deleteAppLayoutInfo(id)
					&& applicationDao.deleteAppImageInfo(id);
		} catch (Exception e) {
			logger.error("Remove application infos exception:", e);
			return false;
		}
	}
	
	/**
	 * 获取应用下的镜像信息
	 * @param applicationId
	 * @return
	 */
	private List<ApplicationImageInfo> list(Long applicationId) {
		List<ApplicationImageInfo> list = null;
		try {
			list = applicationImageInfoDao.select(applicationId);
		} catch (Exception e) {
			logger.error("查询应用镜像关联数据失败！应用ID = "+applicationId, e);
			
		}
		return list;
	}

	/*
	 * private String getUserDeptId(Long userId) throws Exception { String
	 * deptId = userService.listDept(userId); //
	 * 判断当前用户是否拥有查看“容器平台”的权限，如果有则可以查看所有的镜像 User user = null; if (null == (user =
	 * queryUser(userId, new BsmResult()))) { return null; } List<Role> roles =
	 * roleDao.listByUid(user.getId()); for (Role role : roles) {
	 * List<Authority> authorities = authDao.listByRid(role.getId()); for
	 * (Authority authority : authorities) { // TODO 和威哥少龙沟通后暂时写死判断“容器平台”权限的id
	 * if (authority.getId().equals(new Long(132))) { deptId = null; break; } }
	 * } return deptId; }
	 */

	private User queryUser(Long userId, BsmResult bsmResult) {
		User user = null;
		try {
			user = userDao.query(userId);
		} catch (Exception e) {
			logger.error("获取用户信息失败！", e);
			bsmResult.setMessage("获取用户信息失败！");
		}
		if (null == user) {
			bsmResult.setMessage("获取用户信息为空！");
		}
		return user;
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
			logger.error("获取应用信息失败.", e);
			return null;
		}
	}

	/**
	 * 获取环境信息
	 * 
	 * @param applicationId
	 * @return
	 */
	private Environment getEnv(Long envId) {
		Environment environment = null;
		try {
			environment = environmentDao.get(Environment.class, envId);
		} catch (Exception e) {
			logger.error("get environment is exception");
		}
		return environment;
	}

	// ================================================监控代码================================================//
	@Override
	public BsmResult monitor(Monitor monitor) {
		// 最终储存数值的对象
		JSONObject data = new JSONObject();

		// 获取监控地址
		monitor = getAddress(monitor);
		if (null == monitor) {
			return new BsmResult(false, data, "未获取到监控地址:请确认是否已按规定部署kubernetes cpu监控插件");
		}

		// 时间
		JSONArray dates = new JSONArray();
		// network格式处理需要的对象
		JSONObject networkRx = new JSONObject();
		JSONObject networkTx = new JSONObject();
		JSONArray values = new JSONArray();
		JSONObject networkObject = new JSONObject();
		// cpu 格式处理需要的对象
		JSONObject cpuObject = new JSONObject();
		// 磁盘格式处理需要的初始化对象
		JSONObject fileUsage = new JSONObject();
		JSONObject fileLimit = new JSONObject();
		JSONArray fileValues = new JSONArray();
		JSONObject fileObject = new JSONObject();
		try {
			// 参数处理
			if ("application".equals(monitor.getType())) {
				monitor.setStartTime("30m");
			}
			List<Monitor> monitors = param(monitor);
			// 线程池
			List<JSONObject> objects = monitorClient.batch(monitors);
			for (JSONObject object : objects) {
				String type = String.valueOf(object.get("type"));
				switch (type) {
				case "CPU":
					cpuObject = (JSONObject) object.get("CPU");
					break;
					
                case "MEMORY":
                	JSONObject memory = (JSONObject) object.get(type);
					dates = (JSONArray) memory.get("keys");
					data.put("MEMORY", memory);
					break;
					
                case "NETWORKRX":
                	networkRx = (JSONObject) object.get(type);
					break;
					
                case "NETWORKTX":
                	networkTx = (JSONObject) object.get(type);
					break;
					
                case "FILEUSAGE":
                	fileUsage = (JSONObject) object.get(type);
					break;
					
                case "FILELIMIT":
                	fileLimit = (JSONObject) object.get(type);
					break;

				default:
					break;
				}
			}
			// network格式处理
			values.add(networkTx);
			values.add(networkRx);
			networkObject.put("keys", dates);// 时间轴
			networkObject.put("values", values);
			data.put("NETWORK", networkObject);
			//磁盘格式处理
			fileValues.add(fileUsage);
			fileValues.add(fileLimit);
			fileObject.put("keys", dates);// 时间轴
			fileObject.put("values", fileValues);
			data.put("FILE", fileObject);
			// 为cpu赋予时间值
			cpuObject.put("keys", dates);// 时间轴
			data.put("CPU", cpuObject);
			// 5、整理数据
			return new BsmResult(true, data, "获取成功");
		} catch (Exception e) {
			logger.error("获取容器平台监控异常", e);
			return new BsmResult(false, data, "获取容器平台监控异常");
		}

	}

	/**
	 * 参数集合
	 * 
	 * @param monitor
	 * @return
	 */
	public List<Monitor> param(Monitor monitor) {
		List<Monitor> monitors = new ArrayList<Monitor>();
		if ("node".equals(monitor.getType())) {
			monitor.setResourceName(monitor.getResourceName().replace(".", "%5C."));
		}
		// 1、memory参数
		Monitor memoryMonitor = new Monitor();
		BeanUtils.copyProperties(monitor, memoryMonitor);
		memoryMonitor.setDescripteName("memory/usage");
		memoryMonitor.setResourceType("memory");
		// 2、cpu
		Monitor cpuMonitor = new Monitor();
		BeanUtils.copyProperties(monitor, cpuMonitor);
		cpuMonitor.setDescripteName("cpu/usage_rate");
		cpuMonitor.setResourceType("cpu");
		// 3、networkRx
		Monitor netRxMonitor = new Monitor();
		BeanUtils.copyProperties(monitor, netRxMonitor);
		netRxMonitor.setDescripteName("network/rx_rate");
		netRxMonitor.setResourceType("networkRx");
		// 4、networkTx
		Monitor netTxMonitor = new Monitor();
		BeanUtils.copyProperties(monitor, netTxMonitor);
		netTxMonitor.setDescripteName("network/tx_rate");
		netTxMonitor.setResourceType("networkTx");
		
		// 5、参数集合
		monitors.add(memoryMonitor);
		monitors.add(cpuMonitor);
		monitors.add(netRxMonitor);
		monitors.add(netTxMonitor);
		
		if ("node".equals(monitor.getType())) {
			//6、fileUsage
			Monitor fileUsageMonitor = new Monitor();
			BeanUtils.copyProperties(monitor, fileUsageMonitor);
			fileUsageMonitor.setDescripteName("filesystem/usage");
			fileUsageMonitor.setResourceType("fileUsage");
			
			//7、fileLimit
			Monitor fileLimitMonitor = new Monitor();
			BeanUtils.copyProperties(monitor, fileLimitMonitor);
			fileLimitMonitor.setDescripteName("filesystem/limit");
			fileLimitMonitor.setResourceType("fileLimit");
			monitors.add(fileUsageMonitor);
			monitors.add(fileLimitMonitor);
		}
		
		return monitors;
	}

	// ================================================应用中饼状图代码=========================================//
	



	// =====================应用列表，cpu/memory使用量======================//
	/**
	 * 获取监控服务地址
	 * 
	 * @param monitor
	 * @return
	 */
	private Monitor getAddress(Monitor monitor) {
		Long envId = null;
		if (null != monitor.getAppId()) {
			// 获取应用信息
			Application application = getApplication(monitor.getAppId());
			if (null == application) {
				logger.warn("application is null");
				return null;
			}
			// 获取namespace
			monitor.setNamespace(application.getNamespace());
			envId = application.getEnvId();
		} else {
			envId = monitor.getEnvId();
		}

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

	@Override
	public BsmResult createIngress(Long applicationId, String serviceName, int servicePort) {
		// 获取连接资源
		Application application = getApplication(applicationId);
		if (null == application) {
			return new BsmResult(false, "未获取到应用信息");
		}
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "未获取到环境信息或环境不可用");
		}
		ApplicationClient client = new ApplicationClient(environment.getProxy(), String.valueOf(environment.getPort()));
		
		// 获取ingress
		IngressList ingressList = (IngressList) client.list(application.getNamespace(),
				ApplicationEnum.RESOURCE.INGRESS);
		
		if (0 == ingressList.getItems().size()) {//创建ingress前，先检查Ingress插件是否安装，以及状态是否正常
			
			//判断环境是否安装过ingress插件
			boolean plugined = checkIngressPlugin(client);
			if (!plugined) {
				return new BsmResult(false, "该环境未安装Ingress插件，请先安装后再使用");
			}
			
			//判断ingress插件是否能正常使用
			boolean statused = checkIngressStatus(client);
			if (!statused) {
				return new BsmResult(false, "该环境下Ingress插件状态异常，请在状态正常情况下使用");
			}
			
			// 创建ingress
			Ingress ingress = client.createServerIngress(StringUtil.convertPinyin(application.getName()).toLowerCase(), serviceName,
					new IntOrString(servicePort), application.getNamespace());
			if (null == ingress) {
				return new BsmResult(false, "服务暴露未成功");
			}
			return new BsmResult(true, "服务暴露任务正在下发执行，需要等待几秒...");
		} else {//已有创建好的ingress，则判断Ingress的状态是否正常
			
			//判断ingress插件是否能正常使用
			boolean statused = checkIngressStatus(client);
			if (!statused) {
				return new BsmResult(false, "该环境下Ingress插件状态异常，请在状态正常情况下使用");
			}
			// 把服务路径添加到ingress
			boolean isSuccess = client.loadServicePath(StringUtil.convertPinyin(application.getName()).toLowerCase(), 
					application.getNamespace(), serviceName, new IntOrString(servicePort));
			if (!isSuccess) {
				return new BsmResult(false, "服务暴露未成功");
			}
			return new BsmResult(true, "服务暴露任务正在下发执行，需要等待几秒...");
		}
	}

	@Override
	public BsmResult getIngress(Long applicationId) {
		JSONObject data = new JSONObject();
		// 获取环境信息
		Application application = getApplication(applicationId);
		if (null == application) {
			return new BsmResult(false, "未获取到应用信息");
		}
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "未获取到环境信息或环境不可用");
		}
		ApplicationClient client = new ApplicationClient(environment.getProxy(), String.valueOf(environment.getPort()));

		// 获取ingress名单
		JSONObject ingObject = getIngress(client, application.getNamespace(), environment.getProxy());

		// 获取所有的服务名单
		ServiceList serviceList = (ServiceList) client.list(application.getNamespace(),
				ApplicationEnum.RESOURCE.SERVICE);
		List<io.fabric8.kubernetes.api.model.Service> services = serviceList.getItems();

		data.put("ingress", ingObject);
		data.put("services", services);
		return new BsmResult(true, data, "获取成功");
	}
	
	/**
	 * 判断ingress的状态是否正常
	 * @param client
	 * @return
	 */
	private boolean checkIngressStatus(ApplicationClient client){
		int healthyCount = 0;
		
		//获取环境namesapce=kube-system 下的pod
		PodList podList = (PodList) client.list("kube-system", ApplicationEnum.RESOURCE.POD);
		List<Pod> pods = podList.getItems();
		if (ListTool.isEmpty(pods)) {
			logger.warn("ingress插件状态异常，请检查后再使用");
			return false;
		}
		
		for (Pod pod : pods) {
			if (pod.getMetadata().getName().contains("nginx-ingress")) {
				String status = null;
				List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
				ContainerStatus containerStatus = containerStatuses.get(0);
				if (containerStatus.getRestartCount() >= 0) {
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
				}
				
				if (null != status && "running".equals(status)) {
					healthyCount ++;
				}
			}
		}
		
		//获取环境节点数
		NodeList nodeList = (NodeList) client.list(ApplicationEnum.RESOURCE.NODE);
		List<Node> nodes = nodeList.getItems();
		
		if (ListTool.isEmpty(nodes)) {
			logger.warn("该环境节点异常，未获取到节点信息");
			return false;
		}
		
		//判断ingress的健康pod数==节点数，说明ingress状态正常
		if (healthyCount == nodes.size()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 判断环境是否安装ingress插件
	 * @param client
	 * @return
	 */
	private boolean checkIngressPlugin(ApplicationClient client){
		DaemonSetList dSetList = (DaemonSetList) client.list("kube-system", ApplicationEnum.RESOURCE.DAEMONSETS);
		List<DaemonSet> dSets = dSetList.getItems();
		if (ListTool.isEmpty(dSets)) {
			logger.warn("环境未安装ingress插件");
			return false;
		}
		for (DaemonSet daemonSet : dSets) {
			if (daemonSet.getMetadata().getName().contains("nginx-ingress")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取ingress已暴露服务的名单
	 * 
	 * @param client
	 * @param namespace
	 * @param envProxy
	 */
	private JSONObject getIngress(ApplicationClient client, String namespace, String envProxy) {
		JSONObject ingObject = new JSONObject();
		IngressList ingressList = (IngressList) client.list(namespace, ApplicationEnum.RESOURCE.INGRESS);
		if (!ListTool.isEmpty(ingressList.getItems())) {
			List<IngressRule> rules = ingressList.getItems().get(0).getSpec().getRules();
			for (IngressRule ingressRule : rules) {
				String serviceName = ingressRule.getHttp().getPaths().get(0).getBackend().getServiceName();
				String path = ingressRule.getHttp().getPaths().get(0).getPath();
				List<LoadBalancerIngress> list = ingressList.getItems().get(0).getStatus().getLoadBalancer()
						.getIngress();
				if (!ListTool.isEmpty(list)) {
					ingObject.put(serviceName, "https://" + list.get(0).getIp() + path);
				}
			}
		}
		return ingObject;
	}

	@Override
	public BsmResult getServicePort(Long applicationId, String type, String serviceName) {

		// 获取环境信息
		Application application = getApplication(applicationId);
		if (null == application) {
			return new BsmResult(false, "未获取到应用信息");
		}
		Environment environment = getEnv(application.getEnvId());
		if (null == environment 
				|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
			return new BsmResult(false, "未获取到环境信息或环境不可用");
		}
		ApplicationClient client = new ApplicationClient(environment.getProxy(), String.valueOf(environment.getPort()));

		// 获取ingress名单
		JSONArray ingArray = new JSONArray();
		IngressList ingressList = (IngressList) client.list(application.getNamespace(),
				ApplicationEnum.RESOURCE.INGRESS);
		if (!ListTool.isEmpty(ingressList.getItems())) {
			List<IngressRule> rules = ingressList.getItems().get(0).getSpec().getRules();
			for (IngressRule ingressRule : rules) {
				String name = ingressRule.getHttp().getPaths().get(0).getBackend().getServiceName();
				IntOrString servicePort = ingressRule.getHttp().getPaths().get(0).getBackend().getServicePort();
				ingArray.add(name + "/" + servicePort.getIntVal());
			}
		}

		// 获取满足类型并且没有被暴露的服务端口
		JSONArray portArray = new JSONArray();
		io.fabric8.kubernetes.api.model.Service service = (io.fabric8.kubernetes.api.model.Service) client
				.detail(application.getNamespace(), serviceName, ApplicationEnum.RESOURCE.SERVICE);
		List<ServicePort> ports = service.getSpec().getPorts();
		if (!ListTool.isEmpty(ports)) {
			for (ServicePort port : ports) {
//				if (type.equals(port.getName())
//						&& !ingArray.contains(service.getMetadata().getName() + "/" + port.getPort())) {
//					portArray.add(port.getPort());
//				}
				if (!ingArray.contains(service.getMetadata().getName() + "/" + port.getPort())) {
					portArray.add(port.getPort());
				}
			}
		}

		return new BsmResult(true, portArray, "获取成功");
	}

	@Override
	public BsmResult getResource(Long applicationId) {
		try {
			Application application = applicationDao.query(applicationId);
			if (null != application) {
				ApplicationResource applicationResource = new ApplicationResource(monitorClient, environmentService);
				Application app = applicationResource.getApplication(application);
				return new BsmResult(true, app, "获取成功");
			}
		} catch (Exception e) {
			logger.error("获取应用资源信息异常", e);
		}
		return new BsmResult(true, null, "获取成功");
	}
	
	@Override
	public BsmResult applicationTopology(Long applicationId) {
		BsmResult bsmResult = new BsmResult();
		JSONArray appArray = new JSONArray(); //存放多个应用数组
		JSONObject appObject = new JSONObject(); //存放一个应用对象信息 
		JSONArray serviceArray = new JSONArray(); //存放多个服务数组
		
		/**
		 * 1、应用层信息处理
		 */
		//获取应用信息
		Application application = getApplication(applicationId);
		
		if (null == application) {
			bsmResult.setMessage("未获取到该应用信息");
			return bsmResult;
		}
		
		//采集应用信息
		appObject.put("name", application.getName());
		appObject.put("targetCategory", "apply");
		
		/**
		 * 2、服务层信息处理
		 */
		//获取该应用所在的环境资源信息
		Environment env = getEnv(application.getEnvId());
		if (null == env || 
				!Arrays.asList(env.getStatuses()).contains(env.getStatus())) {
			logger.warn("未获取到该应用所在的环境信息， 或环境状态["+env.getStatus()+"]不可用");
			appObject.put("tagetJson", serviceArray);
			appArray.add(appObject);
			bsmResult.setSuccess(true);
			bsmResult.setData(appArray);
			return bsmResult;
		}
		
		//获取环境资源连接
		ApplicationClient client = 
				new ApplicationClient(env.getProxy(), String.valueOf(env.getPort()));
		
		//获取服务信息
		ServiceList serviceList = (ServiceList) 
				client.list(application.getNamespace(), ApplicationEnum.RESOURCE.SERVICE);
		
		if (null == serviceList) {
			logger.warn("未获取到该应用下的服务信息");
			appObject.put("tagetJson", serviceArray);
			appArray.add(appObject);
			
			bsmResult.setSuccess(true);
			bsmResult.setData(appArray);
			return bsmResult;
		}
		
		List<io.fabric8.kubernetes.api.model.Service> services = serviceList.getItems();
		if (ListTool.isEmpty(services)) {
			logger.warn("该应用下未添加服务");
			appObject.put("tagetJson", serviceArray);
			appArray.add(appObject);
			
			bsmResult.setSuccess(true);
			bsmResult.setData(appArray);
			return bsmResult;
		}
		
		for (io.fabric8.kubernetes.api.model.Service service : services) {
			JSONArray hostArray = new JSONArray();
			JSONObject serviceObject = new JSONObject();
			serviceObject.put("targetName", service.getMetadata().getName());
			serviceObject.put("targetCategory", "service");
			
			Map<String, String> selector = service.getSpec().getSelector();
			PodList podList = (PodList) 
					client.list(application.getNamespace(), selector, ApplicationEnum.RESOURCE.POD);
			if (null == podList) {
				logger.warn("未获取到该应用下的服务所在的主机信息");
				serviceObject.put("target", hostArray);
				serviceArray.add(serviceObject);
				continue;
			}
			
			List<Pod> pods = podList.getItems();
			if (ListTool.isEmpty(pods)) {
				logger.warn("未获取到该应用下的服务无运行实例");
				serviceObject.put("target", hostArray);
				serviceArray.add(serviceObject);
				continue;
			}
			
			/**
			 * 3、主机层信息处理
			 */
			Set<String> set = new HashSet<String>();
			for (Pod pod : pods) {
				set.add(pod.getSpec().getNodeName());
			}
			// 遍历set
			for (String name : set) {
				if (StringUtils.isEmpty(name)) {
					continue;
				}
				JSONObject hostObject = new JSONObject();
				Node node = (Node) client.detail(application.getNamespace(), name, ApplicationEnum.RESOURCE.NODE);
				if (null == node) {
					logger.warn("未获取到该应用下的服务所在的主机信息");
					serviceObject.put("target", hostArray);
					serviceArray.add(serviceObject);
					continue;
				}
				
				// 获取节点信息
				hostObject.put("targetName", node.getMetadata().getName());
				hostObject.put("targetCategory", "host");
				List<NodeCondition> conditions = node.getStatus().getConditions();
				for (NodeCondition nodeCondition : conditions) {
					String alarm = null;
					if (!nodeCondition.getType().equals("Ready".trim())) {
						alarm = "二级警告";
					}
					hostObject.put("alarm", alarm);
				}
				hostArray.add(hostObject);
			}
			
			serviceObject.put("target", hostArray);
			serviceArray.add(serviceObject);
		}
		
		appObject.put("tagetJson", serviceArray);
		appArray.add(appObject);
		bsmResult.setSuccess(true);
		bsmResult.setData(appArray);
		return bsmResult;
	}
	
	@Override
	public BsmResult deleteServiceIngress(Long applicationId, String svcName) {
		BsmResult bsmResult = new BsmResult();
		
		//获取应用信息
		Application application = getApplication(applicationId);
		if (null == application) {
			logger.warn("未获取到该应用信息, 请检查该应用在数据库中是否存在");
			bsmResult.setMessage("未获取到该应用信息");
			return bsmResult;
		}
		
		//获取环境信息
		Environment env = getEnv(application.getEnvId());
		if (null == env) {
			logger.warn("未获取到该应用所在的环境信息, 请检查该环境是否在数据库中存在");
			bsmResult.setMessage("未获取到该应用所在的环境信息");
			return bsmResult;
		}
		
		//获取环境资源连接
		ApplicationClient client = new ApplicationClient(env.getProxy(), String.valueOf(env.getPort()));
		
		//执行移除操作
		boolean removed = client.removeServicePath(application.getNamespace(), 
				StringUtil.convertPinyin(application.getName()).toLowerCase(), svcName);
		if (removed) {
			bsmResult.setSuccess(true);
			bsmResult.setMessage("移除成功");
			return bsmResult;
		}
		
		bsmResult.setMessage("移除失败");
		return bsmResult;
	}
	

	@Override
	public BsmResult templatable(Long id,String namespace,String name, Long userId) {
		try {
			//根据应用实例的id查询出对应的编排文件（json格式）
			//1、根据应用id查询对应的环境ip及端口号
			Application application = getApplication(id);
			if (null == application) {
				return new BsmResult(false, "未获取到应用信息");
			}
			Environment environment = getEnv(application.getEnvId());
			if (null == environment 
					|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
				return new BsmResult(false, "未获取到环境信息或环境不可用");
			}
			ApplicationClient client = new ApplicationClient(environment.getProxy(), String.valueOf(environment.getPort()));
			io.fabric8.kubernetes.api.model.Service service = (io.fabric8.kubernetes.api.model.Service) 
					client.detail(namespace, name, ApplicationEnum.RESOURCE.SERVICE);
			Map<String, String> selector = service.getSpec().getSelector();
			DeploymentList list = (DeploymentList) client.list(namespace, selector, ApplicationEnum.RESOURCE.DEPLOYMENT);
			List<Deployment> items = list.getItems();
			Deployment deployment = items.get(0);
			   
			String jsonString = JSONObject.toJSONString(service);
			//2、jsonToYaml
			JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString);
			String jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
			
			String jsonString2 = JSONObject.toJSONString(deployment);
			JsonNode jsonNodeTree2 = new ObjectMapper().readTree(jsonString2);
			String jsonAsYaml2 = new YAMLMapper().writeValueAsString(jsonNodeTree2);
			
			String jsonAsYaml3 = jsonAsYaml + jsonAsYaml2;
			//3、返回给前端
			return new BsmResult(true,jsonAsYaml3,"应用实例模板化查询文件内容成功");
		} catch (Exception e) {
			logger.error("应用实例模板化查询文件内容失败", e);
			return new BsmResult(false, "获取失败：应用实例模板化查询文件内容时发生异常");
		}
	}

	@Override
	public BsmResult createResourceQuota(Long applicationId, JSONObject object, JSONObject limitRangeObject) {
		BsmResult bsmResult = new BsmResult();
		JSONObject temObject = new JSONObject();
		//获取应用信息
		Application application = getApplication(applicationId);
		if (null == application) {
			bsmResult.setMessage("未获取到该应用在数据库的信息");
			return bsmResult;
		}
		
		//获取环境信息
		Environment environment = getEnv(application.getEnvId());
		if (null == environment) {
			bsmResult.setMessage("未获取到该应用所在的环境信息");
			return bsmResult;
		}
		
		//hard数据格式处理
		List<DataMap> dataMaps = new ArrayList<>();
		Set<String> keySet = object.keySet();
		for (String key : keySet) {
			DataMap dataMap = new DataMap();
			dataMap.setKey(key);
			dataMap.setValue(object.getString(key));
			dataMaps.add(dataMap);
		}
		temObject.put("hards", dataMaps);
		temObject.put("name", StringUtil.convertPinyin(application.getName()).toLowerCase());
		temObject.put("namespace", application.getNamespace());
		
		String filePath = FileUtil.filePath("resource_template");
		File file = FileUtil.templateToExecute(filePath, "resource_quota.yaml", temObject,
				"resource_quota_temporary.yaml");
		ApplicationClient client = new ApplicationClient(environment.getProxy(),
				String.valueOf(environment.getPort()));
		client.load(file);
		file.delete();
		
		//是否需要创建LimitRange的处理
		if (!limitRangeObject.isEmpty()) {
			client.createLimitRange(StringUtil.convertPinyin(application.getName()).toLowerCase(), 
					application.getNamespace(), limitRangeObject);
		}
		
		//修改应用资源配额字段
		boolean modified = modifyQuotaStatus(application, String.valueOf(ApplicationEnum.QuotaStatus.QUOTA));
		if (!modified) {
			bsmResult.setMessage("创建资源配额成功，修改应用配额状态失败");
			return bsmResult;
		}
		bsmResult.setSuccess(true);
		bsmResult.setMessage("创建资源配额成功");
		return bsmResult;
	}
	
	/**
	 * 修改应用资源配额的状态
	 * @param application
	 * @param quotaStatus 需要修改的应用的目标状态
	 * @return
	 */
	private boolean modifyQuotaStatus(Application application, String quotaStatus){
		switch (quotaStatus) {
		case "QUOTA":
			if (String.valueOf(ApplicationEnum.QuotaStatus.NOT_QUOTA).equals(application.getQuotaStatus())) {
				application.setQuotaStatus(String.valueOf(ApplicationEnum.QuotaStatus.QUOTA));
			} else {
				return true;
			}
			break;
        case "NOT_QUOTA":
        	if (String.valueOf(ApplicationEnum.QuotaStatus.QUOTA).equals(application.getQuotaStatus())) {
				application.setQuotaStatus(String.valueOf(ApplicationEnum.QuotaStatus.NOT_QUOTA));
			} else {
				return true;
			}
			break;
		}
		
		application.setGmtModify(new Date());
		try {
			if (applicationDao.update(application)) {
				return true;
			}
			logger.error("modify Application resourceQuota status fail");
			return false;
		} catch (Exception e) {
			logger.error("modify Application resourceQuota status  exception", e);
			return false;
		}
	}

	@SuppressWarnings("null")
	@Override
	public BsmResult detailResourceQuota(Long applicationId) {
		BsmResult bsmResult = new BsmResult();
		JSONObject enumMap = new JSONObject();
		//获取应用信息
		Application application = getApplication(applicationId);
		if (null == application) {
			bsmResult.setMessage("未获取到该应用在数据库的信息");
			return bsmResult;
		}
		
		//获取环境信息
		Environment environment = getEnv(application.getEnvId());
		if (null == environment) {
			bsmResult.setMessage("未获取到该应用所在的环境信息");
			return bsmResult;
		}
		
		//获取环境资源连接
		ApplicationClient client = new ApplicationClient(environment.getProxy(),
				String.valueOf(environment.getPort()));
		
		ResourceQuota resourceQuota = (ResourceQuota) client.detail(application.getNamespace(), 
				StringUtil.convertPinyin(application.getName()).toLowerCase(), ApplicationEnum.RESOURCE.RESOURCEQUOTA);
		
		LimitRange limitRange = (LimitRange) client.detail(application.getNamespace(), 
				StringUtil.convertPinyin(application.getName()).toLowerCase(), ApplicationEnum.RESOURCE.LIMITRANGE);
		
		client.close();
		if (null == resourceQuota) {
			bsmResult.setMessage("未获取到服务端中的资源配额信息");
			return bsmResult;
		}
		
		//对resourceQuota中的hard属性数据处理
		Map<String, Quantity> hardMap = resourceQuota.getSpec().getHard();
		if (null != hardMap || !hardMap.isEmpty()) {
			for (Map.Entry<String, Quantity> entry: hardMap.entrySet()) {
				String newKey = entry.getKey().toUpperCase().replace(".", "_");
				//对hard值进行分类
				enumMap = quotaHandle(newKey, entry.getValue().getAmount(), enumMap);
			}
		} else {
			enumMap.put(ResourceQuotaEnum.type.COMPUTE_RESOURCE.toString(), null);
			enumMap.put(ResourceQuotaEnum.type.OBJECT_COUNT.toString(), null);
		}
		
		
		//对LimitRange中的属性数据处理
		enumMap = limitRangeHandle(limitRange, enumMap);
		
		bsmResult.setSuccess(true);
		bsmResult.setMessage("获取成功");
		bsmResult.setData(enumMap);
		return bsmResult;
	}

	@Override
	public BsmResult modifyResourceQuota(Long applicationId, Map<String, Quantity> hardMap, JSONObject limitRangeObject) {
		BsmResult bsmResult = new BsmResult();
		
		//获取应用信息
		Application application = getApplication(applicationId);
		if (null == application) {
			bsmResult.setMessage("未获取到该应用在数据库的信息");
			return bsmResult;
		}
		
		//获取环境信息
		Environment environment = getEnv(application.getEnvId());
		if (null == environment) {
			bsmResult.setMessage("未获取到该应用所在的环境信息");
			return bsmResult;
		}
		
		//获取环境资源连接
		ApplicationClient client = new ApplicationClient(environment.getProxy(),
				String.valueOf(environment.getPort()));
		
		//修改ResourceQuota对象
		ResourceQuota quota = client.editQuota(application.getNamespace(), 
				StringUtil.convertPinyin(application.getName()).toLowerCase(), hardMap);
		
		if (null == quota) {
			bsmResult.setMessage("修改资源配额失败");
		} else {
			boolean edited = client.editLimitRange(StringUtil.convertPinyin(application.getName()).toLowerCase(), 
					application.getNamespace(), limitRangeObject);
			if (edited) {
				bsmResult.setMessage("修改资源配额成功");
				bsmResult.setSuccess(true);
			} else {
				bsmResult.setMessage("修改资源配额成功, 修改极限对象LimitRange失败");
			}
		}
		
		client.close();
		return bsmResult;
	}
	
	/**
	 * 对用户创建的LimitRange对象中的数据信息，进行格式处理
	 * @param limitRange
	 * @param enumMap
	 * @return
	 */
	private JSONObject limitRangeHandle(LimitRange limitRange, JSONObject enumMap){
		//limitRange为空，则认为用户在创建资源配额的时候，未选择附加配置选项进行创建limitRange
		if (null == limitRange 
				|| (null != limitRange && ListTool.isEmpty(limitRange.getSpec().getLimits()))) {
			enumMap.put(ResourceQuotaEnum.type.LIMIT_RANGE.toString(), null);
		} else {
			enumMap.put(ResourceQuotaEnum.type.LIMIT_RANGE.toString(), "true");
			List<LimitRangeItem> limits = limitRange.getSpec().getLimits();
			for (LimitRangeItem limitRangeItem : limits) {
				String type = limitRangeItem.getType();
				
				//对pod类型的数据处理
				if (type.toUpperCase().equals(String.valueOf(ResourceQuotaEnum.limitRange.POD))) {
					JSONObject podObject = new JSONObject();
					podObject.put("maxPodCpu", limitRangeItem.getMax().get("cpu").getAmount());
					podObject.put("maxPodMemory", limitRangeItem.getMax().get("memory").getAmount());
					podObject.put("minPodCpu", limitRangeItem.getMin().get("cpu").getAmount());
					podObject.put("minPodMemory", limitRangeItem.getMin().get("memory").getAmount());
					enumMap.put(ResourceQuotaEnum.type.LIMIT_RANGE_POD.toString(), podObject);
				} else {
					enumMap.put(ResourceQuotaEnum.type.LIMIT_RANGE_POD.toString(), null);
				}
				
				//对container类型的数据处理
				if (type.toUpperCase().equals(String.valueOf(ResourceQuotaEnum.limitRange.CONTAINER))) {
					//判断处理container类型的最大、最小 cpu和memory数据
					if (null == limitRangeItem.getMax() || limitRangeItem.getMax().isEmpty()) {
						enumMap.put(ResourceQuotaEnum.type.LIMIT_RANGE_CONTAINER.toString(), null);
					} else {
						JSONObject containerObject = new JSONObject();
						containerObject.put("maxContainerCpu", limitRangeItem.getMax().get("cpu").getAmount());
						containerObject.put("maxContainerMemory", limitRangeItem.getMax().get("memory").getAmount());
						containerObject.put("minContainerCpu", limitRangeItem.getMin().get("cpu").getAmount());
						containerObject.put("minContainerMemory", limitRangeItem.getMin().get("memory").getAmount());
						enumMap.put(ResourceQuotaEnum.type.LIMIT_RANGE_CONTAINER.toString(), containerObject);
					}
					
					//判断处理container类型的默认的cpu和memory数据
					if (null == limitRangeItem.getDefault() || limitRangeItem.getDefault().isEmpty()) {
						enumMap.put(ResourceQuotaEnum.type.LIMIT_RANGE_DEFAULT_CONTAINER.toString(), null);
					} else {
						JSONObject defaultContainerObject = new JSONObject();
						defaultContainerObject.put("defaultCpu", limitRangeItem.getDefault().get("cpu").getAmount());
						defaultContainerObject.put("defaultMemory", limitRangeItem.getDefault().get("memory").getAmount());
						defaultContainerObject.put("defaultReqCpu", limitRangeItem.getDefaultRequest().get("cpu").getAmount());
						defaultContainerObject.put("defaultReqMemory", limitRangeItem.getDefaultRequest().get("memory").getAmount());
						enumMap.put(ResourceQuotaEnum.type.LIMIT_RANGE_DEFAULT_CONTAINER.toString(), defaultContainerObject);
					}
				}
			}
		}
		
		return enumMap;
	}
	
	/**
	 * 对配额数据中的hard属性数据按枚举类型进行分类
	 * @param newKey
	 * @param value
	 * @param enumMap
	 * @return
	 */
	private JSONObject quotaHandle(String newKey, String value, JSONObject enumMap){
		int computeResource = 0; //用户计算是否有计算资源的配置
		int objectCount = 0;//用户计算是否有对象数的配置
		EnumSet<ResourceQuotaEnum.computeResource> computeSet = EnumSet.allOf(ResourceQuotaEnum.computeResource.class);
		EnumSet<ResourceQuotaEnum.objectCount> objectSet = EnumSet.allOf(ResourceQuotaEnum.objectCount.class);
		
		//计算资源
		if (computeSet.toString().contains(newKey)) {
			JSONObject computeObject = enumMap.getJSONObject(ResourceQuotaEnum.type.COMPUTE_RESOURCE.toString());
			if (null == computeObject) {
				computeObject = new JSONObject();
				computeObject.put(newKey, value);
				enumMap.put(ResourceQuotaEnum.type.COMPUTE_RESOURCE.toString(), computeObject);
			} else {
				computeObject.put(newKey, value);
			}
			computeResource ++;
		}
		
		//对象数
		if (objectSet.toString().contains(newKey)) {
			JSONObject object = enumMap.getJSONObject(ResourceQuotaEnum.type.OBJECT_COUNT.toString());
			if (null == object) {
				object = new JSONObject();
				object.put(newKey, value);
				enumMap.put(ResourceQuotaEnum.type.OBJECT_COUNT.toString(), object);
			} else {
				object.put(newKey, value);
			}
			objectCount ++;
		}
		
		if (computeResource == 0) {
			enumMap.put(ResourceQuotaEnum.type.COMPUTE_RESOURCE.toString(), null);
		}
		if (objectCount == 0) {
			enumMap.put(ResourceQuotaEnum.type.OBJECT_COUNT.toString(), null);
		}
		
		return enumMap;
	}

	@Override
	public BsmResult getUsedResourceQuota(Long applicationId) {
        BsmResult bsmResult = new BsmResult();
        JSONObject handleMap = new JSONObject();
		
		//获取应用信息
		Application application = getApplication(applicationId);
		if (null == application) {
			bsmResult.setMessage("未获取到该应用在数据库的信息");
			return bsmResult;
		}
		
		//获取环境信息
		Environment environment = getEnv(application.getEnvId());
		if (null == environment) {
			bsmResult.setMessage("未获取到该应用所在的环境信息");
			return bsmResult;
		}
		
		//获取环境资源连接
		ApplicationClient client = new ApplicationClient(environment.getProxy(),
				String.valueOf(environment.getPort()));
		
		ResourceQuota quota = (ResourceQuota) client.detail(application.getNamespace(), 
				StringUtil.convertPinyin(application.getName()).toLowerCase(), ApplicationEnum.RESOURCE.RESOURCEQUOTA);
		
		client.close();
		
		if (null == quota) {
			bsmResult.setMessage("未获取到服务端中的资源配额信息");
			return bsmResult;
		}
		
		Map<String, Quantity> usedMap = quota.getStatus().getUsed();
		for (Map.Entry<String, Quantity> entry: usedMap.entrySet()) {
			String newKey = entry.getKey().toUpperCase().replace(".", "_");
			//对hard值进行分类
			handleMap = quotaHandle(newKey, entry.getValue().getAmount(), handleMap);
		}
		
		bsmResult.setMessage("未获取到服务端中的资源配额信息");
		bsmResult.setData(handleMap);
		bsmResult.setSuccess(true);
		return bsmResult;
	}

	@Override
	public BsmResult deleteResourceQuota(Long applicationId) {
		BsmResult bsmResult = new BsmResult();
		
		//获取应用信息
		Application application = getApplication(applicationId);
		if (null == application) {
			bsmResult.setMessage("未获取到该应用在数据库的信息");
			return bsmResult;
		}
		
		//获取环境信息
		Environment environment = getEnv(application.getEnvId());
		if (null == environment) {
			bsmResult.setMessage("未获取到该应用所在的环境信息");
			return bsmResult;
		}
		
		//获取环境资源连接
		ApplicationClient client = new ApplicationClient(environment.getProxy(),
				String.valueOf(environment.getPort()));
		
		boolean removed = client.remove(application.getNamespace(), 
				StringUtil.convertPinyin(application.getName()).toLowerCase(), ApplicationEnum.RESOURCE.RESOURCEQUOTA);
		
		if (removed) {
			//修改应用资源配额字段
			boolean modified = modifyQuotaStatus(application, String.valueOf(ApplicationEnum.QuotaStatus.NOT_QUOTA));
			if (!modified) {
				bsmResult.setMessage("删除资源配额成功，修改应用配额状态为[NOT_QUOTA]失败");
				return bsmResult;
			}
			//删除limitRange
			LimitRange limitRange = (LimitRange) client.detail(application.getNamespace(), 
					StringUtil.convertPinyin(application.getName()).toLowerCase(), ApplicationEnum.RESOURCE.LIMITRANGE);
			//查询判断是否创建了LimitRange，如果没有创建，直接删除会报错
			if (null != limitRange) {
				boolean deleted = client.remove(application.getNamespace(), 
						StringUtil.convertPinyin(application.getName()).toLowerCase(), ApplicationEnum.RESOURCE.LIMITRANGE);
				if (!deleted) {
					bsmResult.setMessage("删除失败，资源配额[ResourceQuota]删除成功，资源极限范围[LimitRange]删除失败，需手动处理！");
					return bsmResult;
				}
			}
			
			bsmResult.setMessage("删除成功");
			bsmResult.setSuccess(true);
			return bsmResult;
		}
		
		bsmResult.setMessage("删除失败");
		return bsmResult;
	}
	
	

}
