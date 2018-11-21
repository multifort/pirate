package com.bocloud.paas.service.statistic.impl;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BaseResult;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.enums.EnvironmentEnum;
import com.bocloud.paas.common.enums.HostEnum;
import com.bocloud.paas.common.util.AddressConUtil;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.application.ApplicationImageInfoDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.dao.environment.HostDao;
import com.bocloud.paas.dao.environment.PersistentVolumeDao;
import com.bocloud.paas.dao.repository.ImageDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.ApplicationImageInfo;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.Host;
import com.bocloud.paas.entity.Image;
import com.bocloud.paas.entity.Volume;
import com.bocloud.paas.model.ApplicationBean;
import com.bocloud.paas.model.StatisticBean;
import com.bocloud.paas.service.application.util.ApplicationClient;
import com.bocloud.paas.service.application.util.MonitorClient;
import com.bocloud.paas.service.environment.EnvironmentService;
import com.bocloud.paas.service.statistic.AppTask;
import com.bocloud.paas.service.statistic.StatisticService;
import com.bocloud.paas.service.user.UserService;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service("statisticService")
public class StatisticServiceImpl implements StatisticService {

	private static Logger logger = LoggerFactory.getLogger(StatisticServiceImpl.class);
	@Autowired
	private EnvironmentDao environmentDao;
	@Autowired
	private EnvironmentService environmentService;
	@Autowired
	private ImageDao imageDao;
	@Autowired
	private HostDao hostDao;
	@Autowired
	private ApplicationDao applicationDao;
	@Autowired
	private ApplicationImageInfoDao applicationImageInfoDao;
	@Autowired
	private PersistentVolumeDao persistentVolumeDao;
	private String PV_STATUS_BOUND = "Bound";
	@Autowired
	private UserService userService;
	@Autowired
	private MonitorClient monitorClient;

	@Override
	public BsmResult statisticTotal(RequestUser requestUser) {
		try {
			//获取当前用户所在的组织机构以及组织机构下的子机构ID
			String deptIds = userService.listDept(requestUser.getId());
			
			// 查询所有的环境
			List<Environment> environments = environmentDao.queryAll(deptIds);

			// 应用总数
			int appTotal = 0;
			// 运行中应用总数
			int appRunNum = 0;
			// pod总数
			int podTotal = 0;
			// 运行中实例数
			int podRunTotal = 0;
			// 应用实例数
			int appPodNum = 0;
			// 运行中应用实例总数
			int appRunPodNum = 0;
			// 服务实例总数
			int servicePodNum = 0;
			// 运行中服务实例总数
			int serviceRunPodNum = 0;
			// persistentVolume总数
			int pvTotal = 0;
			// pv使用中的个数
			int pvUsedNum = 0;
			// pv总容量
			int pvCapacityTotal = 0;
			// pv使用中总数
			int pvUsedTotal = 0;
			// 机器总数
			int nodeTotal = 0;
			// 正常机器数量：不在环境中但状态正常的机器数量
			int normalHostNum = 0;
			// 异常机器数量：不在环境中单状态异常的机器数量
			int abnormalHostNum = 0;
			// 可调度机器总数
			int scheduNodeNum = 0;
			// 不可调度机器总数
			int unscheduNodeNum = 0;
			// 添加中主机数量
			int addingHostNum = 0;
			// 移出中主机数量
			int outingHostNum = 0;
			// 镜像总数
			int imageTotal = 0;
			// 公有镜像总数
			int publicImageNum = 0;
			// 私有镜像总数
			int privateImageNum = 0;
			// 系统运行状态
			String status = "健康";

			// 获取应用的总数（从数据库中查询）
			List<Application> applications = applicationDao.queryAll(deptIds);
			// 应用总数
			appTotal = applications.size();
			//
			List<ApplicationBean> appsDateList = new ArrayList<ApplicationBean>();
			List<ApplicationBean> appsImageNumList = new ArrayList<ApplicationBean>();
			// 运行中应用总数
			for (Application application : applications) {
				if ("DEPLOY".equals(application.getStatus())) {
					appRunNum++;
				}
				ApplicationBean appBean = new ApplicationBean();

				appBean.setName(application.getName());
				appBean.setGmtModify(application.getGmtModify());
				List<ApplicationImageInfo> applicationImageInfos = applicationImageInfoDao
						.select(application.getId());
				appsDateList.add(appBean);

				appBean.setImageNum(applicationImageInfos.size());
				appsImageNumList.add(appBean);
			}

			Map<String, List<ApplicationBean>> appSortMap = new HashMap<String, List<ApplicationBean>>();
			// 对apps中的应用按创建时间进行排序，从近到远
			SortClass sortClass = new SortClass(); // 调用此方法之后对存放应用的list按创建时间排序
			Collections.sort(appsDateList, sortClass);
			appSortMap.put("DateSort", appsDateList);

			// Map中存放根据使用镜像数量排名之后的应用
			SortClasstwo sortClassTwo = new SortClasstwo();
			Collections.sort(appsImageNumList, sortClassTwo);
			appSortMap.put("imageNumSort", appsImageNumList);

			// 获取镜像信息：镜像总数；共有镜像数量；私有镜像数量
			List<Image> images = imageDao.queryAll();
			imageTotal = images.size();
			for (Image image : images) {
				if (image.getProperty() == 0) {
					// 共有镜像的数量
					publicImageNum++;
				} else if (image.getProperty() == 1) {
					// 私有镜像的数量
					privateImageNum++;
				}
			}

			for (Environment environment : environments) {
				List<Volume> volumes = persistentVolumeDao.queryByEnvId(environment.getId());
				pvTotal += volumes.size();// 存储卷数量
				for (Volume volume : volumes) {
					if(volume.getCapacity().contains("G")){
						volume.setCapacity(volume.getCapacity().substring(0,volume.getCapacity().length()-1));
					}
					if(volume.getCapacity().contains("Gi")){
						volume.setCapacity(volume.getCapacity().substring(0,volume.getCapacity().length()-1));
					}
					pvCapacityTotal += Integer.parseInt(volume.getCapacity());
					if(PV_STATUS_BOUND.equals(volume.getStatus())){
						pvUsedTotal += Integer.parseInt(volume.getCapacity());
						// pv使用中的个数
						pvUsedNum++;
					}
				}
				
				if (null == environment.getProxy()) {
					continue;
				}
				if(null == environment.getPort()){
					continue;
				}
				
				// 判断环境的状态，如果有任何一个环境处于不正常状态，则系统运行状态为“不健康”
				if (EnvironmentEnum.ABNORMAL.getCode().equals(environment.getStatus()) || EnvironmentEnum.DEAD.getCode().equals(environment.getStatus())) {
					status = "不健康";
				}
				
				// 判断该主机是否可达
				boolean isCon = InetAddress.getByName(environment.getProxy()).isReachable(3000);
				if (!isCon) {
					status = "不健康";
					continue;
				}
				
				boolean con = new AddressConUtil().connect(environment.getProxy(), environment.getPort());
				if(!con){
					status = "不健康";
					continue;
				}
				
				KubernetesClient client = new ApplicationClient(environment.getProxy(), environment.getPort().toString())
						.getKubeClient();

				// 获取pod总数
				List<Pod> pods = client.pods().inAnyNamespace().list().getItems();
				// 实例总数
				podTotal += pods.size();
				for (Pod pod : pods) {
					if ("Running".equals(pod.getStatus().getPhase())) {
						podRunTotal++;
					}
				}

				// 获取应用实例数(从集群中查询，命名空间的名称与应用的名称相同)
				List<Application> apps = applicationDao.queryByEnvId(environment.getId());
				for (Application app : apps) {
					List<Pod> podList = client.pods().inNamespace(app.getNamespace()).list().getItems();
					// 应用实例总数
					appPodNum = appPodNum + podList.size();
					for (Pod pod : podList) {
						if ("Running".equals(pod.getStatus().getPhase())) {
							// 运行中应用实例总数
							appRunPodNum++;
						}
					}
				}
				
				List<Pod> servicePodList2 = new ArrayList<>();
				List<Pod> servicePodList3 = new ArrayList<>();
				List<Namespace> namespaces = client.namespaces().list().getItems();
				for (Namespace namespace : namespaces) {
					if(namespace.getMetadata().getName().equals("kube-system")){
						servicePodList2 = client.pods().inNamespace("kube-system").list().getItems();
					}
					
					if(namespace.getMetadata().getName().equals("kube-public")){
					    servicePodList3 = client.pods().inNamespace("kube-public").list().getItems();
					}
				}
				
				// 服务实例的总数
				servicePodNum = servicePodNum + servicePodList2.size() + servicePodList3.size();

				for (Pod pod : servicePodList2) {
					if ("Running".equals(pod.getStatus().getPhase())) {
						serviceRunPodNum++;
					}
				}
				for (Pod pod : servicePodList3) {
					if ("Running".equals(pod.getStatus().getPhase())) {
						serviceRunPodNum++;
					}
				}

				// 获取pv总个数
				/*List<PersistentVolume> pvs = client.persistentVolumes().list().getItems();
				pvTotal += pvs.size();
				if (!ListTool.isEmpty(pvs)) {
					for (PersistentVolume pv : pvs) {
						String amount = pv.getSpec().getCapacity().get("storage").getAmount();
						if(amount.contains("Gi")){
							pvCapacityTotal += Integer.parseInt(amount.substring(0, amount.length() - 2));
						}else{
							pvCapacityTotal +=  Integer.parseInt(amount);
						}
						if (PV_STATUS_BOUND.equals(pv.getStatus().getPhase())) {
							if(amount.contains("Gi")){
								pvUsedTotal += Integer.parseInt(amount.substring(0, amount.length() - 2));
							}else{
								pvUsedTotal += Integer.parseInt(amount);
							}
							// pv使用中的个数
							pvUsedNum++;
						}
					}
				}*/
				

				// 获取机器总数
			/*	List<Node> nodes = client.nodes().list().getItems();
				nodeTotal += nodes.size();
				// 获取可调度与不可调度机器数量
				if (!ListTool.isEmpty(nodes)) {
					for (Node node : nodes) {
						List<NodeCondition> conditions = node.getStatus().getConditions();
						if (!ListTool.isEmpty(conditions)) {
							for (NodeCondition condition : conditions) {
								if("Ready".equals(condition.getType())){
									if("Unknown".equals(condition.getStatus())){
										abnormalHostNum++;
									}else{
										if (node.getSpec().getUnschedulable() != null && node.getSpec().getUnschedulable()) {
											// 不可调度节点数量
											unscheduNodeNum++;
										} else {
											// 可调度节点数量
											scheduNodeNum++;
										}
									}
								}
							}
						}
					}
				}*/
			}

			// 获取不处于环境中主机的数量
			List<Host> hosts = hostDao.queryAll();
			nodeTotal = hosts.size();
			for (Host host : hosts) {
				if (host.getStatus().equals(HostEnum.ABNORMAL.getCode())) {
					abnormalHostNum++;
				} else if (host.getStatus().equals(HostEnum.NORMAL.getCode())) {
					normalHostNum++;
				}else if(HostEnum.SCHEDUABLE.getCode().equals(host.getStatus())){
					// 可调度节点数量
					scheduNodeNum++;
				}else if(HostEnum.UNSCHEDUABLE.getCode().equals(host.getStatus())){
					// 不可调度节点数量
					unscheduNodeNum++;
				}else if(HostEnum.ADDING.getCode().equals(host.getStatus())){
					addingHostNum++;
				}else if(HostEnum.OUTING.getCode().equals(host.getStatus())){
					outingHostNum++;
				}
			}

			StatisticBean statistic = new StatisticBean(appTotal, appRunNum, podTotal, podRunTotal, appPodNum,
					appRunPodNum, servicePodNum, serviceRunPodNum, pvTotal, pvCapacityTotal, nodeTotal, scheduNodeNum,
					unscheduNodeNum, imageTotal, publicImageNum, privateImageNum, environments, status, appSortMap,
					pvUsedTotal, pvUsedNum, abnormalHostNum, normalHostNum,addingHostNum,outingHostNum);
			return new BsmResult(true, statistic, "获取信息成功");
		} catch (Exception e) {
			logger.error("获取数据出现异常", e);
			return new BsmResult(false, "获取数据异常，请查看环境是否正常");
		}
	}

	/**
	 * 获取应用cpu/memory使用量
	 * 
	 * @param page
	 * @param rows
	 * @return
	 */
	@Override
	public BsmResult getAppResource(RequestUser user) {
		JSONObject object = new JSONObject();
		
		//获取当前用户所在的组织机构以及组织机构下的子机构ID
		String deptIds = userService.listDept(user.getId());

		try {
			List<Application> applications = applicationDao.select(deptIds);
			// 获取每个应用的资源请求数据
			List<Application> appList = batch(applications, monitorClient, environmentService);
			
			if (ListTool.isEmpty(applications)) {
				logger.warn("当前平台所有环境中，未部署应用");
				return new BsmResult(true, object, "获取成功");
			}
			object.put("cpu", appCpuSort(appList));
			object.put("memory", appMemorySort(appList));
			
		} catch (Exception e) {
			logger.error("Get application monitor resource exception", e);
		}

		return new BsmResult(true, object, "获取成功");
	}
	
	private List<Application> appCpuSort(List<Application> applications){
		ApplicationCpuSort sortClass = new ApplicationCpuSort(); // 调用此方法之后对存放应用的list按cpu大小排序
		Collections.sort(applications, sortClass);
		return applications;
	}
	
	private List<Application> appMemorySort(List<Application> applications){
		ApplicationMemorySort sortClass = new ApplicationMemorySort(); // 调用此方法之后对存放应用的list按memory大小排序
		Collections.sort(applications, sortClass);
		return applications;
	}
	
	/**
	 * describe: 应用cpu使用量排行
	 * @author Zaney
	 * @data 2017年11月20日
	 */
	class ApplicationCpuSort implements Comparator<Application> {
		@Override
		public int compare(Application a1, Application a2) {
			// 按字典顺序比较两个字符串。该比较基于字符串中各个字符的 Unicode 值。
			// 按字典顺序将此 String 对象表示的字符序列与参数字符串所表示的字符序列进行比较。
			// 如果按字典顺序此 String 对象位于参数字符串之前，则比较结果为一个负整数。
			// 如果按字典顺序此 String 对象位于参数字符串之后，则比较结果为一个正整数。
			// 如果这两个字符串相等，则结果为 0；compareTo 只在方法 equals(Object) 返回 true 时才返回 0
			Double double1 = Double.valueOf(a1.getCurrentCpu().split("m")[0]);
			Double double2 = Double.valueOf(a2.getCurrentCpu().split("m")[0]);
			int flag = new BigDecimal(double1).compareTo(new BigDecimal(double2));
			return -flag;
		}
	}
	
	/**
	 * describe: 应用内存排行
	 * @author Zaney
	 * @data 2017年11月20日
	 */
	class ApplicationMemorySort implements Comparator<Application> {
		@Override
		public int compare(Application a1, Application a2) {
			// 按字典顺序比较两个字符串。该比较基于字符串中各个字符的 Unicode 值。
			// 按字典顺序将此 String 对象表示的字符序列与参数字符串所表示的字符序列进行比较。
			// 如果按字典顺序此 String 对象位于参数字符串之前，则比较结果为一个负整数。
			// 如果按字典顺序此 String 对象位于参数字符串之后，则比较结果为一个正整数。
			// 如果这两个字符串相等，则结果为 0；compareTo 只在方法 equals(Object) 返回 true 时才返回 0
			Double double1 = Double.valueOf(a1.getCurrentMemory().split("Mi")[0]);
			Double double2 = Double.valueOf(a2.getCurrentMemory().split("Mi")[0]);
			int flag = new BigDecimal(double1).compareTo(new BigDecimal(double2));
			return -flag;
		}
	}

	class SortClass implements Comparator<ApplicationBean> {
		@Override
		public int compare(ApplicationBean a1, ApplicationBean a2) {
			// 按字典顺序比较两个字符串。该比较基于字符串中各个字符的 Unicode 值。
			// 按字典顺序将此 String 对象表示的字符序列与参数字符串所表示的字符序列进行比较。
			// 如果按字典顺序此 String 对象位于参数字符串之前，则比较结果为一个负整数。
			// 如果按字典顺序此 String 对象位于参数字符串之后，则比较结果为一个正整数。
			// 如果这两个字符串相等，则结果为 0；compareTo 只在方法 equals(Object) 返回 true 时才返回 0
			int flag = a1.getGmtModify().compareTo(a2.getGmtModify());
			// 按时间排序：按创建时间从最近时间开始
			return -flag;
		}
	}

	class SortClasstwo implements Comparator<ApplicationBean> {
		@Override
		public int compare(ApplicationBean a1, ApplicationBean a2) {
			// 按字典顺序比较两个字符串。该比较基于字符串中各个字符的 Unicode 值。
			// 按字典顺序将此 String 对象表示的字符序列与参数字符串所表示的字符序列进行比较。
			// 如果按字典顺序此 String 对象位于参数字符串之前，则比较结果为一个负整数。
			// 如果按字典顺序此 String 对象位于参数字符串之后，则比较结果为一个正整数。
			// 如果这两个字符串相等，则结果为 0；compareTo 只在方法 equals(Object) 返回 true 时才返回 0
			int flag = a1.getImageNum().compareTo(a2.getImageNum());
			// 按时间排序：按创建时间从最近时间开始
			return -flag;
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
	private List<Application> batch(List<Application> applications, MonitorClient monitorClient, 
			EnvironmentService environmentService) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
		CompletionService<BaseResult<Application>> completionService = new ExecutorCompletionService<>(executor);
		if (ListTool.isEmpty(applications)) {
			return null;
		}
		for (Application application : applications) {
			AppTask task = new AppTask(application, monitorClient, environmentService);
			completionService.submit(task);
		}
		List<Application> appList = new ArrayList<>();
		int completeTask = 0;
		int timeout = 1000 * 60 * 10;
		long begingTime = System.currentTimeMillis();
		while (completeTask < applications.size()) {
			try {
				Future<BaseResult<Application>> take = completionService.take();
				if (null != take) {
					BaseResult<Application> result = take.get();
					if (result.isSuccess()) {
						appList.add(result.getData());
					}
					completeTask++;
					if (System.currentTimeMillis() - begingTime > timeout) {
						break;
					}
				}
			} catch (Exception e) {
				logger.error("线程池执行任务结果异常", e);
				continue;
			}
			
		}
		executor.shutdown();
		return appList;
	}

}
