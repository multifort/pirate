package com.bocloud.paas.service.application.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.http.HttpClient;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Result;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.paas.common.util.ExtendHttpClient;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSet;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

/**
 * openshift/k8s资源操作工具类
 *
 * @author zjm
 * @date 2017年3月17日
 */
@Component("openshiftUtil")
public class ApplicationUtil {

	private static final String OPENSHIFT_PORT = ":8080";
	private static final String HTTPS_PROXY = "http://";

	/**
	 * 对所有k8s的资源组件追加一个label，并固定label的key值，value值是应用名，便于按照应用查找组件
	 */
	public static final String UNIFIED_LABEL_KEY = "UNIFIED_APP_NAME";
	/**
	 * 对所有k8s的资源组件追加一个label，并固定label的key值，value值是应用名-随机数，防止资源label完全相同
	 */
	public static final String DIFF_LABEL_KEY = "DIFF_APP_NAME";

	// private static final String INGRESS_HOST_PRE = "bocloud.";

	// private static final String INGRESS_HOST_AFT = ".com";

	private static final Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);
	// @Autowired
	// private OpenshiftClusterDao openshiftClusterDao;

	/**
	 * 通过openshift获取kubernetes客户端
	 *
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public KubernetesClient getKubernetesClient(String proxyUrl) {
		try {
			// OpenshiftCluster cluster =
			// openshiftClusterDao.getOpenshiftClusterByproxyUrl(proxyUrl);
			String master = HTTPS_PROXY + proxyUrl + OPENSHIFT_PORT;
			Config config = new ConfigBuilder().withMasterUrl(master).build();
			// config.setTrustCerts(false);
			// config.setOauthToken(cluster.getToken());
			return new DefaultKubernetesClient(config);
		} catch (Exception e) {
			logger.error("获取连接失败", e);
			return null;
		}
	}

	/**
	 * 创建k8s service
	 *
	 * @param service
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult createService(String proxyUrl, Service service) {
		Service createService = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			createService = client.services().create(service);
		} catch (KubernetesClientException e) {
			logger.error("service name [" + service.getMetadata().getName() + "]创建失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "service name [" + service.getMetadata().getName() + "]创建失败: " + error);
		} catch (Exception e) {
			logger.error("service name [" + service.getMetadata().getName() + "]创建失败: ", e);
			return new BsmResult(false,
					"service name [" + service.getMetadata().getName() + "]创建失败: " + e.getMessage());
		}
		if (null == createService) {
			return new BsmResult(false, "service name [" + service.getMetadata().getName() + "]创建失败！");
		}
		return new BsmResult(true, createService, "service name [" + createService.getMetadata().getName() + "]创建成功！");
	}

	/**
	 * 创建k8s rc
	 *
	 * @param replicationController
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult createReplicationController(String proxyUrl, ReplicationController replicationController) {
		ReplicationController createReplicationController = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			createReplicationController = client.replicationControllers().create(replicationController);
		} catch (KubernetesClientException e) {
			logger.error("replicationController name [" + replicationController.getMetadata().getName() + "]创建失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false,
					"replicationController name [" + replicationController.getMetadata().getName() + "]创建失败: " + error);
		} catch (Exception e) {
			logger.error("replicationController name [" + replicationController.getMetadata().getName() + "]创建失败: ", e);
			return new BsmResult(false, "replicationController name [" + replicationController.getMetadata().getName()
					+ "]创建失败: " + e.getMessage());
		}
		if (null == createReplicationController) {
			return new BsmResult(false,
					"replicationController name [" + replicationController.getMetadata().getName() + "]创建失败！");
		}
		return new BsmResult(true, createReplicationController,
				"replicationController name [" + createReplicationController.getMetadata().getName() + "]创建成功！");
	}

	/**
	 * 创建k8s deployment
	 *
	 * @param deployment
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult createDeployment(String proxyUrl, Deployment deployment) {
		Deployment createDeployment = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			createDeployment = client.extensions().deployments().create(deployment);
		} catch (KubernetesClientException e) {
			logger.error("deployment name [" + deployment.getMetadata().getName() + "]创建失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "deployment name [" + deployment.getMetadata().getName() + "]创建失败: " + error);
		} catch (Exception e) {
			logger.error("deployment name [" + deployment.getMetadata().getName() + "]创建失败: ", e);
			return new BsmResult(false,
					"deployment name [" + deployment.getMetadata().getName() + "]创建失败: " + e.getMessage());
		}
		if (null == createDeployment) {
			return new BsmResult(false, "deployment name [" + deployment.getMetadata().getName() + "]创建失败！");
		}
		return new BsmResult(true, createDeployment,
				"deployment name [" + createDeployment.getMetadata().getName() + "]创建成功！");
	}

	/**
	 * 按照k8s资源文件创建资源
	 *
	 * @param filePath
	 *            资源路径
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult layoutDeploy(String proxyUrl, String appName, String filePath) {
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			// 最终需要添加的资源
			List<HasMetadata> hasMetadatas = new ArrayList<>();
			// 获取文件中资源
			List<HasMetadata> resourceList = client.load(new FileInputStream(filePath)).get();
			// 设置label value追加的日期和随机字符串
			String diffLabelValue = "-" + RandomStringUtils.randomAlphanumeric(7).toLowerCase();
			for (HasMetadata resource : resourceList) {
				// 判断资源的命名空间是否存在，如果不存在则创建这个命名空间
				if (null == (getNamespace(proxyUrl, resource.getMetadata().getNamespace()).getData())) {
					client.namespaces().createNew().withNewMetadata().withName(resource.getMetadata().getNamespace())
							.endMetadata().done();
				}
				Map<String, String> labels = resource.getMetadata().getLabels();
				if (null == labels) {
					labels = new HashMap<>();
				}
				// 追加资源标签
				labels.put(UNIFIED_LABEL_KEY, appName);
				labels.put(DIFF_LABEL_KEY, appName + diffLabelValue);
				// 如果资源是rc或者deployment则给资源中的pod追加label
				if ("ReplicationController".equals(resource.getKind())) {
					// 如果rc资源已经存在则直接返回不允许创建这个文件
					if (null != getRc(proxyUrl, resource.getMetadata().getNamespace(), resource.getMetadata().getName())
							.getData()) {
						return new BsmResult(false, "应用发布失败！文件中rc，namespace[" + resource.getMetadata().getNamespace()
								+ "],name[" + resource.getMetadata().getName() + "]资源已经存在，不允许发布！");
					}
					ReplicationController replicationController = (ReplicationController) resource;
					// 判断编排文件中是否包含已经存在的资源，如果存在则不允许创建
					Map<String, String> podLabels = replicationController.getSpec().getTemplate().getMetadata()
							.getLabels();
					if (null == podLabels) {
						podLabels = new HashMap<>();
					}
					// 追加资源标签
					podLabels.put(UNIFIED_LABEL_KEY, appName);
					podLabels.put(DIFF_LABEL_KEY, appName + diffLabelValue);
					// TODO 暂不支持Deployment资源的发布，请不要删除
					// } else if (resource.getKind().equals("Deployment")) {
					// // 如果Deployment资源已经存在则直接返回不允许创建这个文件
					// if (null != getDeployment(proxyUrl,
					// resource.getMetadata().getNamespace(),
					// resource.getMetadata().getName()).getData()) {
					// return new BsmResult(false,
					// "应用发布失败！文件中deployment，namespace[" +
					// resource.getMetadata().getNamespace() + "],name["
					// + resource.getMetadata().getName() + "]资源已经存在，不允许发布！");
					// }
					// Deployment deployment = (Deployment) resource;
					// Map<String, String> podLabels =
					// deployment.getSpec().getTemplate().getMetadata().getLabels();
					// if (null == podLabels) {
					// podLabels = new HashMap<>();
					// }
					// // 追加资源标签
					// podLabels.put(UNIFIED_LABEL_KEY, appName);
					// podLabels.put(DIFF_LABEL_KEY, appName + diffLabelValue);
				} else if ("Service".equals(resource.getKind())) {
					// 如果Service资源已经存在则直接返回不允许创建这个文件
					if (null != getService(proxyUrl, resource.getMetadata().getNamespace(),
							resource.getMetadata().getName()).getData()) {
						return new BsmResult(false,
								"应用发布失败！文件中Service，namespace[" + resource.getMetadata().getNamespace() + "],name["
										+ resource.getMetadata().getName() + "]资源已经存在，不允许发布！");
					}
					Service service = (Service) resource;
					Map<String, String> selectors = service.getSpec().getSelector();
					selectors.put(DIFF_LABEL_KEY, appName + diffLabelValue);
				} else if ("HorizontalPodAutoscaler".equals(resource.getKind())) {
					// 如果HPA资源已经存在则直接返回不允许创建这个文件
					if (null != getHPA(proxyUrl, resource.getMetadata().getNamespace(),
							resource.getMetadata().getName()).getData()) {
						return new BsmResult(false, "应用发布失败！文件中HPA，namespace[" + resource.getMetadata().getNamespace()
								+ "],name[" + resource.getMetadata().getName() + "]资源已经存在，不允许发布！");
					}
				} else {
					return new BsmResult(false, "应用发布失败！暂不支持[" + resource.getKind() + "]该资源类型的发布！");
				}
				hasMetadatas.add(resource);
			}
			resourceList = client.resourceList(hasMetadatas).createOrReplace();
			return new BsmResult(true, resourceList, "文件[" + filePath + "]部署成功！");
		} catch (FileNotFoundException e) {
			logger.error("文件 [" + filePath + "]未找到: ", e);
			// 报异常后需要删除发布的资源
			return new BsmResult(false, "文件 [" + filePath + "]未找到: " + e);
		} catch (KubernetesClientException e) {
			logger.error("发布应用失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "发布应用失败: " + error);
		} catch (Exception e) {
			logger.error("发布应用失败: ", e);
			return new BsmResult(false, "发布应用失败: " + e.getMessage());
		}
	}

	/**
	 * 删除k8s service集合
	 *
	 * @param services
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult deleteServices(String proxyUrl, List<Service> services) {
		if (ListTool.isEmpty(services)) {
			return new BsmResult(false, "需要删除service资源为空！");
		}
		boolean result = false;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			result = client.services().delete(services);
		} catch (KubernetesClientException e) {
			logger.error("service资源删除失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "service资源删除失败: " + error);
		} catch (Exception e) {
			logger.error("service资源删除失败: ", e);
			return new BsmResult(false, "service资源删除失败: " + e.getMessage());
		}
		if (result) {
			return new BsmResult(true, "service资源删除成功！");
		}
		return new BsmResult(false, "service资源删除失败！");
	}

	/**
	 * 删除k8s deployment集合
	 *
	 * @param deployments
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult deleteDeployments(String proxyUrl, List<Deployment> deployments) {
		if (ListTool.isEmpty(deployments)) {
			return new BsmResult(false, "需要删除deployments资源为空！");
		}
		boolean result = false;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			result = client.extensions().deployments().delete(deployments);
		} catch (KubernetesClientException e) {
			logger.error("deployment资源删除失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "deployment资源删除失败: " + error);
		} catch (Exception e) {
			logger.error("deployment资源删除失败: ", e);
			return new BsmResult(false, "deployment资源删除失败: " + e.getMessage());
		}
		if (result) {
			return new BsmResult(true, "replicationController资源删除成功！");
		}
		return new BsmResult(false, "deployment资源删除失败！");
	}

	/**
	 * 删除k8s rc集合
	 *
	 * @param replicationControllers
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult deleteReplicationControllers(String proxyUrl, List<ReplicationController> replicationControllers) {
		if (ListTool.isEmpty(replicationControllers)) {
			return new BsmResult(false, "需要删除replicationControllers资源为空！");
		}
		boolean result = false;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			result = client.replicationControllers().delete(replicationControllers);
		} catch (KubernetesClientException e) {
			logger.error("replicationController资源删除失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "replicationController资源删除失败: " + error);
		} catch (Exception e) {
			logger.error("replicationController资源删除失败: ", e);
			return new BsmResult(false, "replicationController资源删除失败: " + e.getMessage());
		}
		if (result) {
			return new BsmResult(true, "replicationController资源删除成功！");
		}
		return new BsmResult(false, "replicationController资源删除失败！");
	}

	/**
	 * 删除k8s pod集合
	 * 
	 * @author zjm
	 * @date 2017年4月1日
	 *
	 * @param pods
	 * @return
	 */
	public BsmResult deletePods(String proxyUrl, List<Pod> pods) {
		if (ListTool.isEmpty(pods)) {
			return new BsmResult(false, "需要删除pods资源为空！");
		}
		boolean result = false;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			result = client.pods().delete(pods);
		} catch (KubernetesClientException e) {
			logger.error("pod资源删除失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "pod资源删除失败: " + error);
		} catch (Exception e) {
			logger.error("pod资源删除失败: ", e);
			return new BsmResult(false, "pod资源删除失败: " + e.getMessage());
		}
		if (result) {
			return new BsmResult(true, "pod资源删除成功！");
		}
		return new BsmResult(false, "pod资源删除失败！");
	}

	/**
	 * 删除k8s hpa集合
	 * 
	 * @author zjm
	 * @date 2017年4月9日
	 *
	 * @param proxyUrl
	 * @param hpas
	 * @return
	 */
	public BsmResult deleteHPAs(String proxyUrl, JSONArray items) {
		if (ListTool.isEmpty(items)) {
			return new BsmResult(false, "需要删除hpas资源为空！");
		}
		try {
			for (Object object : items) {
				JSONObject item = JSONObject.parseObject(object.toString());
				Object metaObject = item.get("metadata");
				JSONObject metadata = JSONObject.parseObject(metaObject.toString());
				String name = metadata.get("name").toString();
				String namespace = metadata.get("namespace").toString();
				BsmResult result = deleteWithName(namespace, name, proxyUrl);
				if (result.isFailed()) {
					logger.info("删除弹性伸缩name=[" + name + "]失败");
					return new BsmResult(false, "删除弹性伸缩name=[" + name + "]失败");
				}
			}
			return new BsmResult(true, "删除弹性伸缩成功");
		} catch (KubernetesClientException e) {
			logger.error("弹性伸缩删除失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "弹性伸缩删除失败: " + error);
		} catch (Exception e) {
			logger.error("弹性伸缩删除失败: ", e);
			return new BsmResult(false, "弹性伸缩删除失败: " + e.getMessage());
		}
	}

	/**
	 * 按照k8s资源文件删除资源
	 *
	 * @param filePath
	 *            资源路径
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult deleteLayoutDeploy(String proxyUrl, String filePath) {
		if (StringUtils.isBlank(filePath)) {
			return new BsmResult(false, "文件[" + filePath + "]未找到，删除失败！");
		}
		boolean result = false;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			result = client.load(new FileInputStream(filePath)).delete();
		} catch (FileNotFoundException e) {
			logger.error("文件 [" + filePath + "]未找到，删除失败: ", e);
			return new BsmResult(false, "文件 [" + filePath + "]未找到，删除失败: " + e);
		} catch (KubernetesClientException e) {
			logger.error("删除文件 [" + filePath + "]资源失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "删除文件 [" + filePath + "]资源失败: " + error);
		} catch (Exception e) {
			logger.error("删除文件 [" + filePath + "]资源失败: ", e);
			return new BsmResult(false, "删除文件 [" + filePath + "]资源失败: " + e.getMessage());
		}
		if (!result) {
			return new BsmResult(false, "文件[" + filePath + "]未找到，删除失败！");
		}
		return new BsmResult(true, "文件[" + filePath + "]删除成功！");
	}

	/**
	 * 变更k8s rc实例数
	 *
	 * @param name
	 *            rc的名字
	 * @param count
	 *            变更的数量
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult scaleRc(String proxyUrl, String namespace, String name, int count) {
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			client.replicationControllers().inNamespace(namespace).withName(name).scale(count);
		} catch (KubernetesClientException e) {
			logger.error("[" + name + "]实例变更为[" + count + "]失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "[" + name + "]实例变更为[" + count + "]失败: " + error);
		} catch (Exception e) {
			logger.error("[" + name + "]实例变更为[" + count + "]失败: ", e);
			return new BsmResult(false, "实例变更[" + name + "] 为[" + count + "]失败: " + e.getMessage());
		}
		return new BsmResult(true, "[" + name + "]实例变更为[" + count + "]成功！");
	}

	/**
	 * 按照名称获取rc
	 * 
	 * @author zjm
	 * @date 2017年4月1日
	 *
	 * @param name
	 * @return
	 */
	public BsmResult getRc(String proxyUrl, String namespace, String name) {
		ReplicationController rc = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			rc = client.replicationControllers().inNamespace(namespace).withName(name).get();
		} catch (KubernetesClientException e) {
			logger.error("获取namespace[" + namespace + "], name[" + name + "]的rc失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取namespace[" + namespace + "], name[" + name + "]的rc失败: " + error);
		} catch (Exception e) {
			logger.error("获取namespace[" + namespace + "], name[" + name + "]的rc失败: ", e);
			return new BsmResult(false, "获取namespace[" + namespace + "], name[" + name + "]的rc失败: " + e.getMessage());
		}
		if (null == rc) {
			return new BsmResult(true, "获取namespace[" + namespace + "], name[" + name + "]的rc为空！");
		}
		return new BsmResult(true, rc, "获取namespace[" + namespace + "], name[" + name + "]的rc成功！");
	}

	/**
	 * 获取k8s rc集合
	 * 
	 * @author zjm
	 * @date 2017年3月22日
	 *
	 * @param labels
	 * @return
	 */
	public BsmResult getRcs(String proxyUrl, Map<String, String> labels) {
		List<Deployment> rcs = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			rcs = client.extensions().deployments().inAnyNamespace().withLabels(labels).list().getItems();
		} catch (KubernetesClientException e) {
			logger.error("获取label[" + labels.toString() + "]的rc集合失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取label[" + labels.toString() + "]的rc集合失败: " + error);
		} catch (Exception e) {
			logger.error("获取label[" + labels.toString() + "]的rc集合失败: ", e);
			return new BsmResult(false, "获取label[" + labels.toString() + "]的rc集合失败: " + e.getMessage());
		}
		if (ListTool.isEmpty(rcs)) {
			return new BsmResult(true, "获取label[" + labels.toString() + "]的rc集合为空！");
		}
		return new BsmResult(true, rcs, "获取label[" + labels.toString() + "]的rc集合成功！");
	}

	/**
	 * 按照名称获取service
	 * 
	 * @author zjm
	 * @date 2017年4月1日
	 *
	 * @param name
	 * @return
	 */
	public BsmResult getService(String proxyUrl, String namespace, String name) {
		Service service = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			service = client.services().inNamespace(namespace).withName(name).get();
		} catch (KubernetesClientException e) {
			logger.error("获取namespace[" + namespace + "], name[" + name + "]的service失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取namespace[" + namespace + "], name[" + name + "]的service失败: " + error);
		} catch (Exception e) {
			logger.error("获取namespace[" + namespace + "], name[" + name + "]的service失败: ", e);
			return new BsmResult(false,
					"获取namespace[" + namespace + "], name[" + name + "]的service失败: " + e.getMessage());
		}
		if (null == service) {
			return new BsmResult(true, "获取namespace[" + namespace + "], name[" + name + "]的service为空！");
		}
		return new BsmResult(true, service, "获取namespace[" + namespace + "], name[" + name + "]的service成功！");
	}

	/**
	 * 获取k8s service集合
	 * 
	 * @author zjm
	 * @date 2017年3月22日
	 *
	 * @param labels
	 * @return
	 */
	public BsmResult getServices(String proxyUrl, Map<String, String> labels) {
		List<Service> services = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			services = client.services().inAnyNamespace().withLabels(labels).list().getItems();
		} catch (KubernetesClientException e) {
			logger.error("获取label[" + labels.toString() + "]的service失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取label[" + labels.toString() + "]的service失败: " + error);
		} catch (Exception e) {
			logger.error("获取label[" + labels.toString() + "]的service失败: ", e);
			return new BsmResult(false, "获取label[" + labels.toString() + "]的service失败: " + e.getMessage());
		}
		if (ListTool.isEmpty(services)) {
			return new BsmResult(true, "获取label[" + labels.toString() + "]的service为空！");
		}
		return new BsmResult(true, services, "获取label[" + labels.toString() + "]的service成功！");
	}

	/**
	 * 获取k8s deployment
	 * 
	 * @author zjm
	 * @date 2017年4月7日
	 *
	 * @param name
	 * @return
	 */
	public BsmResult getDeployment(String proxyUrl, String namespace, String name) {
		Deployment deployment = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			deployment = client.extensions().deployments().inNamespace(namespace).withName(name).get();
		} catch (KubernetesClientException e) {
			logger.error("获取namespace[" + namespace + "]中, name[" + name + "]的deployment失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取namespace[" + namespace + "]中, name[" + name + "]的deployment失败: " + error);
		} catch (Exception e) {
			logger.error("获取namespace[" + namespace + "]中, name[" + name + "]的deployment失败: ", e);
			return new BsmResult(false,
					"获取namespace[" + namespace + "]中, name[" + name + "]的deployment失败: " + e.getMessage());
		}
		if (null == deployment) {
			return new BsmResult(true, "获取namespace[" + namespace + "]中, name[" + name + "]的deployment为空！");
		}
		return new BsmResult(true, deployment, "获取namespace[" + namespace + "]中, name[" + name + "]的deployment成功！");
	}

	/**
	 * 获取k8s deployment集合
	 * 
	 * @author zjm
	 * @date 2017年3月22日
	 *
	 * @param labels
	 * @return
	 */
	public BsmResult getDeployments(String proxyUrl, Map<String, String> labels) {
		List<Deployment> deployments = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			deployments = client.extensions().deployments().inAnyNamespace().withLabels(labels).list().getItems();
		} catch (KubernetesClientException e) {
			logger.error("获取label[" + labels.toString() + "]的deployment集合失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取label[" + labels.toString() + "]的deployment集合失败: " + error);
		} catch (Exception e) {
			logger.error("获取label[" + labels.toString() + "]的deployment集合失败: ", e);
			return new BsmResult(false, "获取label[" + labels.toString() + "]的deployment集合失败: " + e.getMessage());
		}
		if (ListTool.isEmpty(deployments)) {
			return new BsmResult(true, "获取label[" + labels.toString() + "]的deployment集合为空！");
		}
		return new BsmResult(true, deployments, "获取label[" + labels.toString() + "]的deployment集合成功！");
	}

	/**
	 * 获取k8s pod
	 *
	 * @param name
	 * @return
	 * @author zjm
	 * @date 2017年3月17日
	 */
	public BsmResult getPod(String proxyUrl, String namespace, String name) {
		Pod pod = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			pod = client.pods().inNamespace(namespace).withName(name).get();
		} catch (KubernetesClientException e) {
			logger.error("获取namespace[" + namespace + "], name[" + name + "]的pod失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取namespace[" + namespace + "], name[" + name + "]的pod失败: " + error);
		} catch (Exception e) {
			logger.error("获取namespace[" + namespace + "], name[" + name + "]的pod失败: ", e);
			return new BsmResult(false, "获取namespace[" + namespace + "], name[" + name + "]的pod失败: " + e.getMessage());
		}
		if (null == pod) {
			return new BsmResult(true, "获取namespace[" + namespace + "], name[" + name + "]的pod为空！");
		}
		return new BsmResult(true, pod, "获取namespace[" + namespace + "], name[" + name + "]的pod成功！");
	}

	/**
	 * 获取k8s pod集合
	 * 
	 * @author zjm
	 * @date 2017年4月9日
	 *
	 * @param proxyUrl
	 * @param labels
	 * @return
	 */
	public BsmResult getPods(String proxyUrl, Map<String, String> labels) {
		List<Pod> pods = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			pods = client.pods().inAnyNamespace().withLabels(labels).list().getItems();
		} catch (KubernetesClientException e) {
			logger.error("获取label[" + labels.toString() + "]的pod集合失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取label[" + labels.toString() + "]的pod集合失败: " + error);
		} catch (Exception e) {
			logger.error("获取label[" + labels.toString() + "]的pod集合失败: ", e);
			return new BsmResult(false, "获取label[" + labels.toString() + "]的pod集合失败: " + e.getMessage());
		}
		if (ListTool.isEmpty(pods)) {
			return new BsmResult(true, "获取label[" + labels.toString() + "]的pod集合为空！");
		}
		return new BsmResult(true, pods, "获取label[" + labels.toString() + "]的pod集合成功！");
	}

	/**
	 * 获取k8s hpa
	 * 
	 * @author zjm
	 * @date 2017年4月9日
	 *
	 * @param proxyUrl
	 * @param namespace
	 * @param name
	 * @return
	 */
	public BsmResult getHPA(String proxyUrl, String namespace, String name) {
		try {
			BsmResult result = getHPAWithName(namespace, name, proxyUrl);
			if (result.isFailed()) {
				return new BsmResult(false, "获取 HPA失败");
			}
			return new BsmResult(true, result.getData(),
					"获取namespace[" + namespace + "], name[" + name + "]的hpas资源成功！");
		} catch (KubernetesClientException e) {
			logger.error("获取namespace[" + namespace + "], name[" + name + "]的弹性伸缩失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取namespace[" + namespace + "], name[" + name + "]的弹性伸缩失败: " + error);
		} catch (Exception e) {
			logger.error("获取namespace[" + namespace + "], name[" + name + "]的弹性伸缩失败: ", e);
			return new BsmResult(false, "获取namespace[" + namespace + "], name[" + name + "]的弹性伸缩失败: " + e.getMessage());
		}
	}

	/**
	 * 根据资源label／资源kind和资源名称获取hpa
	 * 
	 * @author zjm
	 * @date 2017年4月19日
	 *
	 * @param proxyUrl
	 * @param namespace
	 * @param labels
	 * @param resourceKind
	 * @param resourceName
	 * @return
	 */
	public BsmResult getHPA(String proxyUrl, String namespace, Map<String, String> labels, String resourceKind,
			String resourceName) {
		JSONArray hpas = null;
		try {
			BsmResult hpa = getHPAWithlabels(namespace, labels, proxyUrl);
			if (hpa.isFailed()) {
				return new BsmResult(false, "获取 HPA 失败");
			} else {
				hpas = JSONObject.parseArray(hpa.getData().toString());
			}
		} catch (KubernetesClientException e) {
			logger.error("获取namespace[" + namespace + "], labels[" + labels + "]的弹性伸缩失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取namespace[" + namespace + "], labels[" + labels + "]的弹性伸缩失败: " + error);
		} catch (Exception e) {
			logger.error("获取namespace[" + namespace + "], labels[" + labels + "]的弹性伸缩失败: ", e);
			return new BsmResult(false,
					"获取namespace[" + namespace + "], labels[" + labels + "]的弹性伸缩失败: " + e.getMessage());
		}
		if (!ListTool.isEmpty(hpas)) {
			for (Object object : hpas) {
				JSONObject hpa = JSONObject.parseObject(object.toString());
				JSONObject spec = JSONObject.parseObject(hpa.get("spec").toString());
				JSONObject scaleTargetRef = JSONObject.parseObject(spec.get("scaleTargetRef").toString());
				if (scaleTargetRef.get("kind").equals(resourceKind)
						&& scaleTargetRef.get("name").equals(resourceName)) {
					return new BsmResult(true, spec,
							"获取resourceKind[" + resourceKind + "], resourceName[" + resourceName + "]的弹性伸缩成功！");
				}
			}
		}
		return new BsmResult(false, "获取resourceKind[" + resourceKind + "], resourceName[" + resourceName + "]的弹性伸缩成功！");
	}

	/**
	 * 获取k8s hpa集合
	 * 
	 * @author zjm
	 * @date 2017年4月9日
	 * @param proxyUrl
	 * @param labels
	 * @return
	 */
	public BsmResult getHPAs(String proxyUrl, Map<String, String> labels) {
		try {
			StringBuffer labelSelector = new StringBuffer();
			for (Map.Entry<String, String> entry : labels.entrySet()) {
				labelSelector.append(entry.getKey());
				labelSelector.append("=");
				labelSelector.append(entry.getValue());
			}
			BsmResult result = getHPAWithlabels(labels, proxyUrl);
			if (result.isFailed()) {
				return new BsmResult(false, "获取 HPA 失败");
			}
			return new BsmResult(true, result.getData(), "获取label[" + labels.toString() + "]的hpas集合成功！");
		} catch (KubernetesClientException e) {
			logger.error("获取label[" + labels.toString() + "]的hpa集合失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取label[" + labels.toString() + "]的hpa集合失败: " + error);
		} catch (Exception e) {
			logger.error("获取label[" + labels.toString() + "]的hpa集合失败: ", e);
			return new BsmResult(false, "获取label[" + labels.toString() + "]的hpa集合失败: " + e.getMessage());
		}
	}

	public BsmResult getNamespace(String proxyUrl, String name) {
		Namespace namespace = null;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			namespace = client.namespaces().withName(name).get();
		} catch (KubernetesClientException e) {
			logger.error("获取name[" + name + "]的namespace资源失败: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "获取name[" + name + "]的namespace资源失败: " + error);
		} catch (Exception e) {
			logger.error("获取name[" + name + "]的namespace资源失败: ", e);
			return new BsmResult(false, "获取name[" + name + "]的namespace资源失败: " + e.getMessage());
		}
		if (null == namespace) {
			return new BsmResult(false, "获取name[" + name + "]的namespace资源失败！");
		}
		return new BsmResult(true, namespace, "获取name[" + name + "]的namespace资源成功！");
	}

	/**
	 * 获取符合条件的deployment总实例数
	 * 
	 * @author zjm
	 * @date 2017年3月21日
	 *
	 * @param labels
	 * @return
	 */
	public BsmResult getDeploymentsReplicas(String proxyUrl, Map<String, String> labels) {
		int countReplicas = 0;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		List<Deployment> deployments = null;
		try {
			deployments = client.extensions().deployments().inAnyNamespace().withLabels(labels).list().getItems();
		} catch (KubernetesClientException e) {
			logger.error("获取label[" + labels.toString() + "]的deployment集合为空: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(true, "获取label[" + labels.toString() + "]的deployment集合为空: " + error);
		} catch (Exception e) {
			logger.error("获取label[" + labels.toString() + "]的deployment集合为空: ", e);
			return new BsmResult(true, "获取label[" + labels.toString() + "]的deployment集合为空: " + e.getMessage());
		}
		if (ListTool.isEmpty(deployments)) {
			return new BsmResult(true, "获取label[" + labels.toString() + "]的deployment集合为空！");
		}
		for (Deployment deployment : deployments) {
			countReplicas += deployment.getSpec().getReplicas();
		}
		return new BsmResult(true, countReplicas,
				"获取label[" + labels.toString() + "]的deployment的实例数为：" + countReplicas);
	}

	/**
	 * 获取符合条件的rc总实例数
	 * 
	 * @author zjm
	 * @date 2017年4月6日
	 *
	 * @param labels
	 * @return
	 */
	public BsmResult getRcsReplicas(String proxyUrl, Map<String, String> labels) {
		int countReplicas = 0;
		KubernetesClient client = getKubernetesClient(proxyUrl);
		List<ReplicationController> rcs = null;
		try {
			rcs = client.replicationControllers().inAnyNamespace().withLabels(labels).list().getItems();
		} catch (KubernetesClientException e) {
			logger.error("获取label[" + labels.toString() + "]rc集合为空: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(true, "获取label[" + labels.toString() + "]rc集合为空: " + error);
		} catch (Exception e) {
			logger.error("获取label[" + labels.toString() + "]rc集合为空: ", e);
			return new BsmResult(true, "获取label[" + labels.toString() + "]rc集合为空: " + e.getMessage());
		}
		if (ListTool.isEmpty(rcs)) {
			return new BsmResult(true, "获取label[" + labels.toString() + "]rc集合为空！");
		}
		for (ReplicationController rc : rcs) {
			countReplicas += rc.getSpec().getReplicas();
		}
		return new BsmResult(true, countReplicas, "获取label[" + labels.toString() + "]的rc的实例数为：" + countReplicas);
	}

	/**
	 * replicaSets滚动升级
	 * 
	 * @author zjm
	 * @date 2017年3月27日
	 *
	 * @param labels
	 * @param image
	 *            升级后的镜像（镜像名:镜像标签）
	 * @return
	 */
	public BsmResult rollingReplicaSets(String proxyUrl, String namespace, String rsName, String image) {

		KubernetesClient client = getKubernetesClient(proxyUrl);
		ReplicaSet replicaSets = null;
		try {
			replicaSets = client.extensions().replicaSets().inNamespace(namespace).withName(rsName).rolling()
					.updateImage(image);
		} catch (KubernetesClientException e) {
			logger.error("资源replicaSets[" + rsName + "]升级到[" + image + "]成功: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "资源replicaSets[" + rsName + "]升级到[" + image + "]成功: " + error);
		} catch (Exception e) {
			logger.error("资源replicaSets[" + rsName + "]升级到[" + image + "]成功: ", e);
			return new BsmResult(false, "资源replicaSets[" + rsName + "]升级到[" + image + "]成功: " + e.getMessage());
		}
		if (null != replicaSets) {
			return new BsmResult(true, replicaSets, "资源replicaSets[" + rsName + "]升级到[" + image + "]成功！");
		}
		return new BsmResult(false, "资源replicaSets[" + rsName + "]升级到[" + image + "]失败！");
	}

	/**
	 * rc滚动升级
	 * 
	 * @author zjm
	 * @date 2017年3月31日
	 *
	 * @param rcName
	 * @param image
	 *            升级后的镜像（镜像名:镜像标签）
	 * @return
	 */
	public BsmResult rollingRc(String proxyUrl, String namespace, String rcName, String image) {
		KubernetesClient client = getKubernetesClient(proxyUrl);
		ReplicationController rollingRc = null;
		try {
			rollingRc = client.replicationControllers().inNamespace(namespace).withName(rcName).rolling()
					.updateImage(image);
		} catch (Exception e) {
			logger.error("资源rc, namespace[" + namespace + "], name[" + rcName + "]升级到[" + image + "]失败！", e);
			return new BsmResult(false,
					"资源rc, namespace[" + namespace + "], name[" + rcName + "]升级到[" + image + "]失败！");
		}
		if (null != rollingRc) {
			return new BsmResult(true, rollingRc,
					"资源rc, namespace[" + namespace + "], name[" + rcName + "]升级到[" + image + "]成功！");
		}

		return new BsmResult(false, "资源rc, namespace[" + namespace + "], name[" + rcName + "]升级到[" + image + "]失败！");
	}

	/**
	 * 修改deployment的资源（目前只支持cpu和memory）
	 * 
	 * @author zjm
	 * @date 2017年3月28日
	 *
	 * @param deploymentName
	 * @param limits
	 *            值必须大于等于request
	 * @param requests
	 *            必填，数据采集使用
	 * @return
	 */
	public BsmResult editDeploymentResource(String proxyUrl, String namespace, String deploymentName,
			Map<String, Quantity> limits, Map<String, Quantity> requests) {
		KubernetesClient client = getKubernetesClient(proxyUrl);
		List<Container> containers = null;
		try {
			containers = client.extensions().deployments().inNamespace(namespace).withName(deploymentName).get()
					.getSpec().getTemplate().getSpec().getContainers();
		} catch (KubernetesClientException e) {
			logger.error("获取namespace[" + namespace + "], name[" + deploymentName + "]的deployment信息为空: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(true,
					"获取namespace[" + namespace + "], name[" + deploymentName + "]的deployment信息为空: " + error);
		} catch (Exception e) {
			logger.error("获取namespace[" + namespace + "], name[" + deploymentName + "]的deployment信息为空: ", e);
			return new BsmResult(true,
					"获取namespace[" + namespace + "], name[" + deploymentName + "]的deployment信息为空: " + e.getMessage());
		}
		if (ListTool.isEmpty(containers)) {
			return new BsmResult(true, "获取namespace[" + namespace + "], name[" + deploymentName + "]的deployment信息为空！");
		}
		Deployment editDeployment = null;
		for (int i = 0; i < containers.size(); i++) {
			editDeployment = client.extensions().deployments().inNamespace(namespace).withName(deploymentName).edit()
					.editSpec().editTemplate().editSpec().editContainer(i).editOrNewResources().addToLimits(limits)
					.addToRequests(requests).endResources().endContainer().endSpec().endTemplate().endSpec().done();
		}
		if (null != editDeployment) {
			return new BsmResult(true, editDeployment, "deployment[" + deploymentName + "]更新资源成功！");
		}
		return new BsmResult(false, "deployment[" + deploymentName + "]未找到！");
	}

	/**
	 * 修改rc的资源（目前只支持cpu和memory）
	 * 
	 * @author zjm
	 * @date 2017年3月31日
	 *
	 * @param rc
	 * @param limits
	 *            值必须大于等于request
	 * @param requests
	 *            必填，数据采集使用
	 * @return
	 */
	public BsmResult editRcResource(String proxyUrl, String namespace, String rcName, Map<String, Quantity> limits,
			Map<String, Quantity> requests) {
		KubernetesClient client = getKubernetesClient(proxyUrl);
		List<Container> containers = null;
		try {
			containers = client.replicationControllers().inNamespace(namespace).withName(rcName).get().getSpec()
					.getTemplate().getSpec().getContainers();
		} catch (KubernetesClientException e) {
			logger.error("获取namespace[" + namespace + "], name[" + rcName + "]的rc信息为空: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(true, "获取namespace[" + namespace + "], name[" + rcName + "]的rc信息为空: " + error);
		} catch (Exception e) {
			logger.error("获取namespace[" + namespace + "], name[" + rcName + "]的rc信息为空: ", e);
			return new BsmResult(true,
					"获取namespace[" + namespace + "], name[" + rcName + "]的rc信息为空: " + e.getMessage());
		}
		if (ListTool.isEmpty(containers)) {
			return new BsmResult(true, "获取namespace[" + namespace + "], name[" + rcName + "]的rc信息为空！");
		}
		ReplicationController editRc = null;
		for (int i = 0; i < containers.size(); i++) {
			editRc = client.replicationControllers().inNamespace(namespace).withName(rcName).edit().editSpec()
					.editTemplate().editSpec().editContainer(i).editOrNewResources().addToLimits(limits)
					.addToRequests(requests).endResources().endContainer().endSpec().endTemplate().endSpec().done();
		}
		if (null != editRc) {
			return new BsmResult(true, editRc, "deployment[" + rcName + "]更新资源成功！");
		}
		return new BsmResult(false, "deployment[" + rcName + "]未找到！");
	}

	/**
	 * 修改rc资源名称
	 * 
	 * @author zjm
	 * @date 2017年4月4日
	 *
	 * @param rcName
	 * @param newRcName
	 * @return
	 */
	public BsmResult editRcName(String proxyUrl, String namespace, String rcName, String newRcName) {
		KubernetesClient client = getKubernetesClient(proxyUrl);
		try {
			ReplicationController editRc = client.replicationControllers().inNamespace(namespace).withName(rcName)
					.edit().editMetadata().withName(newRcName).endMetadata().done();
			return new BsmResult(true, editRc, "资源rc[" + rcName + "]升级到[" + newRcName + "]成功！");
		} catch (KubernetesClientException e) {
			logger.error("资源rc[" + rcName + "]升级到[" + newRcName + "]成功: ", e);
			String error = "";
			if (null != e.getStatus()) {
				error = e.getStatus().getMessage();
			} else if (null != e.getCause()) {
				error = e.getCause().getMessage();
			} else {
				error = e.getMessage();
			}
			return new BsmResult(false, "资源rc[" + rcName + "]升级到[" + newRcName + "]成功: " + error);
		} catch (Exception e) {
			logger.error("资源rc[" + rcName + "]升级到[" + newRcName + "]成功: ", e);
			return new BsmResult(false, "资源rc[" + rcName + "]升级到[" + newRcName + "]成功: " + e.getMessage());
		}
	}

	/**
	 * 根据namespace lable获取HPA
	 * 
	 * @param proxyUrl
	 * @return
	 */
	public static BsmResult getHPAWithlabels(String namespace, Map<String, String> label, String proxyUrl) {
		Result result = null;
		try {
			StringBuffer labelSelector = new StringBuffer();
			for (Map.Entry<String, String> entry : label.entrySet()) {
				labelSelector.append(entry.getKey());
				labelSelector.append("=");
				labelSelector.append(entry.getValue());
			}
			HttpClient httpClient = new HttpClient(1000 * 10);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Keep-Alive", "10");
			String url = "http://" + proxyUrl + ":8080/apis/autoscaling/v1/namespaces/" + namespace
					+ "/horizontalpodautoscalers?labelSelector=" + labelSelector;
			result = httpClient.get(map, null, url);
			if (result.isFailed()) {
				logger.error("get horizontalpodautoscalers is failed!");
				return new BsmResult(false, "获取HPA失败");
			} else {
				JSONObject object = JSONObject.parseObject(result.getMessage().toString());
				JSONArray items = JSONObject.parseArray(object.getString("items"));
				return new BsmResult(true, items, "获取HPA成功");
			}
		} catch (Exception e) {
			logger.error("get horizontalpodautoscalers is Exception!");
			return new BsmResult(false, "获取HPA异常");
		}
	}

	/**
	 * 根据 lables 获取 HPA
	 * 
	 * @param labelSelector
	 * @param proxyUrl
	 * @return
	 */
	public static BsmResult getHPAWithlabels(Map<String, String> label, String proxyUrl) {
		Result result = null;
		try {
			StringBuffer labelSelector = new StringBuffer();
			for (Map.Entry<String, String> entry : label.entrySet()) {
				labelSelector.append(entry.getKey());
				labelSelector.append("=");
				labelSelector.append(entry.getValue());
			}
			HttpClient httpClient = new HttpClient(1000 * 10);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Keep-Alive", "10");
			String url = "http://" + proxyUrl + ":8080/apis/autoscaling/v1/horizontalpodautoscalers?labelSelector="
					+ labelSelector;
			result = httpClient.get(map, null, url);
			if (result.isFailed()) {
				logger.error("get horizontalpodautoscalers is failed!");
				return new BsmResult(false, "获取HPA失败");
			} else {
				return new BsmResult(true, result.getMessage(), "获取HPA成功");
			}
		} catch (Exception e) {
			logger.error("get horizontalpodautoscalers is Exception!");
			return new BsmResult(false, "获取HPA异常");
		}
	}

	/**
	 * 获取所有namespace下的HPA
	 * 
	 * @param proxyUrl
	 * @return
	 */
	public static BsmResult getHPAs(String proxyUrl, String namespace) {
		Result result = null;
		try {
			HttpClient httpClient = new HttpClient(1000 * 10);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Keep-Alive", "10");
			String url = "";
			if (null != namespace && !"".equals(namespace)) {
				url = "http://" + proxyUrl + ":8080/apis/autoscaling/v1/namespaces/"+namespace+"/horizontalpodautoscalers";
			} else {
				url = "http://" + proxyUrl + ":8080/apis/autoscaling/v1/horizontalpodautoscalers";
			}
			result = httpClient.get(map, null, url);
			if (result.isFailed()) {
				logger.error("get horizontalpodautoscalers is failed!");
				return new BsmResult(false, "获取HPA失败");
			} else {
				JSONObject object = JSONObject.parseObject(result.getMessage().toString());
				JSONArray items = JSONObject.parseArray(object.getString("items"));
				return new BsmResult(true, items, "获取HPA成功");
			}
		} catch (Exception e) {
			logger.error("get horizontalpodautoscalers is Exception!");
			return new BsmResult(false, "获取HPA异常");
		}
	}

	/**
	 * 根据name namespace 获取HPA
	 * 
	 * @param namespace
	 * @param name
	 * @param proxyUrl
	 * @return
	 */
	public static BsmResult getHPAWithName(String namespace, String name, String proxyUrl) {
		try {
			HttpClient httpClient = new HttpClient();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Keep-Alive", "10");
			String Url = "http://" + proxyUrl + ":8080/apis/autoscaling/v1/namespaces/" + namespace
					+ "/horizontalpodautoscalers/" + name;
			Result result = httpClient.get(map, null, Url);
			if (result.isFailed()) {
				logger.error("get [" + namespace + "]-->[" + name + "]fail!");
				return new BsmResult(false, "获取[" + namespace + "]下的[" + name + "]失败");
			}
			return new BsmResult(true, "创建HPA成功", result.getMessage());
		} catch (Exception e) {
			logger.error("get [" + namespace + "]-->[" + name + "]fail!,  please check network exception!");
			return new BsmResult(false, "获取[" + namespace + "]下的[" + name + "]失败,请检查网络异常");
		}
	}

	/**
	 * 创建 HPA
	 * 
	 * @param paramMap
	 * @param proxyUrl
	 * @param namespace
	 * @return
	 */
	public static BsmResult createHPA(StringEntity se, String proxyUrl, String namespace) {
		Result result = null;
		try {
			ExtendHttpClient httpClient = new ExtendHttpClient();
			String url = "http://" + proxyUrl + ":8080/apis/autoscaling/v1/namespaces/" + namespace
					+ "/horizontalpodautoscalers";
			Map<String, Object> headers = MapTools.simpleMap("Content-Type", "application/json");
			headers.put("Keep-Alive", "10");
			result = httpClient.post(headers, null, url, se);
			if (result.isFailed()) {
				logger.error("get horizontalpodautoscalers is failed!");
				return new BsmResult(false, "创建HPA失败");
			} else {
				return new BsmResult(true, "创建HPA成功");
			}
		} catch (Exception e) {
			logger.error("create horizontalpodautoscalers is Exception!");
			return new BsmResult(false, "创建HPA异常");
		}
	}

	/**
	 * 按HPA名称删除
	 * 
	 * @param namespace
	 * @param name
	 * @return
	 */
	public BsmResult deleteWithName(String namespace, String name, String proxyUrl) {
		try {
			HttpClient httpClient = new HttpClient();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Keep-Alive", "10");
			String url = "http://" + proxyUrl + ":8080/apis/autoscaling/v1/namespaces/" + namespace
					+ "/horizontalpodautoscalers/" + name;
			Result result = httpClient.delete(map, null, url);
			if (result.isFailed()) {
				logger.info("删除HPA[" + name + "]失败[网络连接失败]");
				return new BsmResult(false, "删除HPA[" + name + "]失败!");
			}
			return new BsmResult(true, "删除HPA[" + name + "]成功!");
		} catch (Exception e) {
			logger.info("删除HPA[" + name + "]失败[网络连接异常]");
			return new BsmResult(false, "删除HPA[" + name + "]异常!");
		}
	}

	@SuppressWarnings("deprecation")
	public BsmResult createServerIngress(String proxyUrl, String serviceName, IntOrString servicePort, String path,
			String nameSpace) {

		Ingress ingress = new Ingress();
		ObjectMeta objectMeta = new ObjectMeta();
		// objectMeta.setName(serviceName.replace("-", "."));
		if (null == path) {
			path = "/" + serviceName.split("-")[0];
		} else {
			path = "/" + path;
		}
		objectMeta.setName(serviceName);
		if (null == nameSpace) {
			objectMeta.setNamespace("application-" + serviceName.split("-")[0]);
		} else {
			objectMeta.setNamespace(nameSpace);
		}
		Map<String, String> annotations = new HashMap<>();
		annotations.put("ingress.kubernetes.io/rewrite-target", "/");
		objectMeta.setAnnotations(annotations);
		ingress.setMetadata(objectMeta);

		List<IngressRule> ingressRules = new ArrayList<>();
		IngressBackend backend = new IngressBackend(serviceName, servicePort);
		List<HTTPIngressPath> paths = new ArrayList<>();
		paths.add(new HTTPIngressPath(backend, path));

		// ingressRules.add(new IngressRule(INGRESS_HOST_PRE +
		// serviceName.replace("-", "") + INGRESS_HOST_AFT,
		// new HTTPIngressRuleValue(paths)));

		ingressRules.add(new IngressRule(null, new HTTPIngressRuleValue(paths)));
		ingress.setSpec(new IngressSpec(null, ingressRules, null));

		KubernetesClient client = getKubernetesClient(proxyUrl);
		client.extensions().ingress().create(ingress);
		return new BsmResult(true, "create ingress success.");
	}

}
