package com.bocloud.paas.service.application.Impl;

import java.net.InetAddress;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocloud.common.model.BsmResult;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.service.application.InstanceService;
import com.bocloud.paas.service.application.model.PodInfo;
import com.bocloud.paas.service.application.util.ApplicationUtil;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service("instanceService")
public class InstanceServiceImpl implements InstanceService {

	private static Logger logger = LoggerFactory.getLogger(InstanceServiceImpl.class);
	@Autowired
	private ApplicationDao applicationDao;
	@Autowired
	private EnvironmentDao enviromentDao;
	@Autowired
	private ApplicationUtil appUtil;

	@Override
	public BsmResult getPodResource(Long appId, String namespace, String resourceName, String resourceType) {
		BsmResult bsmResult = getEnvProxy(appId);
		if (!bsmResult.isSuccess() || null == bsmResult.getData()) {
			return bsmResult;
		}
		PodInfo podResourceInfo = new PodInfo();
		ResourceRequirements respurce = null;
		String proxyUrl = bsmResult.getData().toString();
		if (resourceType.equals("ReplicationController")) {
			if (null == (bsmResult = appUtil.getRc(proxyUrl, namespace, resourceName)).getData()) {
				return bsmResult;
			}
			ReplicationController rc = (ReplicationController) bsmResult.getData();
			respurce = rc.getSpec().getTemplate().getSpec().getContainers().get(0).getResources();
			// TODO 暂不支持Deployment资源的管理，请不要删除
			// } else if (resourceType.equals("Deployment")) {
			// if (null == (bsmResult = appUtil.getRc(proxyUrl, namespace,
			// resourceName)).getData()) {
			// return bsmResult;
			// }
			// Deployment deployment = (Deployment) bsmResult.getData();
			// respurce =
			// deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getResources();
		} else {
			return new BsmResult(false, "获取组件pod资源信息失败！");
		}
		if (null == respurce) {
			return new BsmResult(false, "获取组件pod资源信息失败！");
		}
		if (null != respurce.getLimits()) {
			if (null != respurce.getLimits().get("cpu")) {
				podResourceInfo.setCpuLimit(respurce.getLimits().get("cpu").getAmount());
			}
			if (null != respurce.getLimits().get("memory")) {
				podResourceInfo.setMemoryLimit(respurce.getLimits().get("memory").getAmount());
			}
		}
		if (null != respurce.getRequests()) {
			if (null != respurce.getRequests().get("cpu")) {
				podResourceInfo.setCpuRequest(respurce.getRequests().get("cpu").getAmount());
			}
			if (null != respurce.getRequests().get("memory")) {
				podResourceInfo.setMemoryRequest(respurce.getRequests().get("memory").getAmount());
			}
		}
		return new BsmResult(true, podResourceInfo, "获取组件pod资源信息成功！");
	}

	@Override
	public BsmResult getPodStatus(Long appId, String name, String namespace) {
		BsmResult bsmResult = getEnvProxy(appId);
		if (!bsmResult.isSuccess() || null == bsmResult.getData()) {
			return bsmResult;
		}
		bsmResult = appUtil.getPod(bsmResult.getData().toString(), namespace, name);
		if (!bsmResult.isSuccess()) {
			return bsmResult;
		}
		PodList podList = (PodList) (bsmResult.getData());
		int successResult = 0;
		for (Pod pod : podList.getItems()) {
			bsmResult.setData(pod.getStatus().getPhase());
			if (pod.getStatus().getPhase().equals("Running") || pod.getStatus().getPhase().equals("Succeeded")) {
				successResult++;
			} else if (pod.getStatus().getPhase().equals("Failed") || pod.getStatus().getPhase().equals("Unknown")) {
				// bsmResult.setStatus(ApplicationEnum.Status.ABNORMAL.toString());
				bsmResult.setMessage("应用实例运行失败！");
				return bsmResult;
			}
			if (successResult == podList.getItems().size()) {
				// bsmResult.setStatus(ApplicationEnum.Status.RUN.toString());
				bsmResult.setMessage("应用实例运行成功！");
				return bsmResult;
			}
		}
		// bsmResult.setStatus(ApplicationEnum.Status.EXECUTE_DEPLOY.toString());
		bsmResult.setMessage("应用实例部署中！");
		return bsmResult;
	}

	private BsmResult getEnvProxy(Long appId) {
		try {
			Application application = applicationDao.get(Application.class, appId);
			if (null == application) {
				return new BsmResult(false, "获取应用信息失败");
			} else {
				Environment environment = enviromentDao.query(application.getEnvId());
				if (null == environment 
						|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
					return new BsmResult(false, "环境不存在或环境状态异常");
				} else {
					return new BsmResult(true, environment.getProxy(), "获取proxyUrl成功");
				}
			}
		} catch (Exception e) {
			logger.error("获取proxyUrl失败", e);
			return new BsmResult(false, "获取proxyUrl失败");
		}
	}
	
	@Override
	public BsmResult getLog(String namespace, String resourceName, String containerName, String status, Long envId) {
		try {
			// 1、获取集群代理节点信息
			if (null == envId) {
				return new BsmResult(true, "所传环境Id为空");
			}
			Environment environment = enviromentDao.query(envId);
			if (null == environment 
					|| !Arrays.asList(environment.getStatuses()).contains(environment.getStatus())) {
				return new BsmResult(false, "环境不存在或环境状态异常");
			}
			// 判断环境代理IP是否存在
			if (null == environment.getProxy() || "".equals(environment.getProxy())) {
				return new BsmResult(false, "环境代理IP为空，无法获取环境下信息");
			}
			// 判断该主机是否可达
			boolean isCon = InetAddress.getByName(environment.getProxy()).isReachable(3000);
			if (!isCon) {
				return new BsmResult(false, "主机不可达，获取环境信息失败");
			}
			// 2、获取client
			KubernetesClient client = appUtil.getKubernetesClient(environment.getProxy());
			// 3、返回正常日志信息
			if ("ImagePullBackOff".equals(status) || "ContainerCreating".equals(status) || "ErrImagePull".equals(status)
					|| "InvalidImageName".equals(status)) {
				return new BsmResult(true, null, "获取成功");
			}
			String log = client.pods().inNamespace(namespace).withName(resourceName).inContainer(containerName)
					.tailingLines(10).getLog();
			return new BsmResult(true, log, "获取成功");
		} catch (Exception e) {
			logger.error("获取" + resourceName + "日志信息失败 : ", e);
			return new BsmResult(false, "获取" + resourceName + "日志信息失败");
		}
	}
}
