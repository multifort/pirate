package com.bocloud.paas.service.application.util;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.enums.ApplicationEnum;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class ApplicationClient {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationClient.class);

    private static final String HTTPS_PROXY = "http://";
    private KubernetesClient kubeClient = null;
    private String proxy;
    private String port;

    /**
     * @Author: langzi
     * @Description: kubernetes client builder
     * @Date: 12:03 2017/11/15
     */
    public void build() {
        try {
            String url = HTTPS_PROXY + proxy + ":" + port;
            Config config = new ConfigBuilder().withMasterUrl(url).build();
            kubeClient = new DefaultKubernetesClient(config);
        } catch (Exception e) {
            logger.error("获取服务端链接失败", e);
        }
    }
    //Openshift client builder
    /*public void build() {
        try {
            Config config = new ConfigBuilder().withMasterUrl("https://master.example.com:8443/").build();
            config.setTrustCerts(true);
            config.setOauthToken("zRrVz5DmrL_SxY5cPPJXJibUbPS6zbyNEHo83gro43Y");
            kubeClient = new DefaultKubernetesClient(config);

        } catch (Exception e) {
            logger.error("获取服务端链接失败", e);
        }
    }*/

    /**
     * 关闭和kubernetes的链接
     */
    public void close() {
        kubeClient.close();
    }

    /**
     * 获取资源的列表信息
     *
     * @param resource 获取应用类型的名称
     * @return
     */
    public Object list(ApplicationEnum.RESOURCE resource) {
        switch (resource) {
            case NODE:
                return kubeClient.nodes().list();
            case DEPLOYMENT:
                return kubeClient.extensions().deployments().inAnyNamespace().list();
            case REPLICATIONCONTROLLER:
                return kubeClient.replicationControllers().inAnyNamespace().list();
            case SERVICE:
                return kubeClient.services().inAnyNamespace().list();
            case INGRESS:
                return kubeClient.extensions().ingresses().inAnyNamespace().list();
            case PERSISTENTVOLUME:
                return kubeClient.persistentVolumes().list();
            case POD:
                return kubeClient.pods().inAnyNamespace().list();
            case HORIZONTALPODAUTOSCALER:
                return kubeClient.autoscaling().horizontalPodAutoscalers().inAnyNamespace().list();

            case STATEFULSETS:
                return kubeClient.apps().statefulSets().inAnyNamespace().list();
                
            case JOB:
            	return kubeClient.extensions().jobs().inAnyNamespace().list();
            default:
                return null;
        }
    }

    /**
     * 获取资源的列表信息
     *
     * @param namespace
     * @param resource
     * @return
     */
    public Object list(String namespace, ApplicationEnum.RESOURCE resource) {
        try {
            switch (resource) {
                case NODE:
                    return kubeClient.nodes().list();
                case DEPLOYMENT:
                    return kubeClient.extensions().deployments().inNamespace(namespace).list();
                case REPLICATIONCONTROLLER:
                    return kubeClient.replicationControllers().inNamespace(namespace).list();
                case SERVICE:
                    return kubeClient.services().inNamespace(namespace).list();
                case INGRESS:
                    return kubeClient.extensions().ingresses().inNamespace(namespace).list();
                case PERSISTENTVOLUME:
                    return kubeClient.persistentVolumes().list();
                case PERSISTENTVOLUMECLAIMS:
                    return kubeClient.persistentVolumeClaims().list();
                case POD:
                    return kubeClient.pods().inNamespace(namespace).list();
                case EVENT:
                    return kubeClient.events().inNamespace(namespace).list();
                case CONFIGMAP:
                    return kubeClient.configMaps().inNamespace(namespace).list();
                case DAEMONSETS:
                    return kubeClient.extensions().daemonSets().inNamespace(namespace).list();
                case STATEFULSETS:
                    return kubeClient.apps().statefulSets().inNamespace(namespace).list();
                case JOB:
                	return kubeClient.extensions().jobs().inNamespace(namespace).list();
                default:
                    return null;
            }
        } catch (Exception e) {
            logger.error("连接不上环境资源，获取不到数据信息, 请检查环境连接是否顺畅，或主机是否处于down机或者关闭状态", e);
            return null;
        }
    }

    /**
     * 获取资源的详细信息
     *
     * @param namespace
     * @param name
     * @param resource
     * @return
     */
    public Object detail(String namespace, String name, ApplicationEnum.RESOURCE resource) {
        try {
            switch (resource) {
                case DEPLOYMENT:
                    return kubeClient.extensions().deployments().inNamespace(namespace).withName(name).get();
                case REPLICATIONCONTROLLER:
                    return kubeClient.replicationControllers().inNamespace(namespace).withName(name).get();
                case SERVICE:
                    return kubeClient.services().inNamespace(namespace).withName(name).get();
                case INGRESS:
                    return kubeClient.extensions().ingresses().inNamespace(namespace).withName(name).get();
                case PERSISTENTVOLUME:
                    return kubeClient.persistentVolumes().withName(name).get();
                case POD:
                    return kubeClient.pods().inNamespace(namespace).withName(name).get();
                case NODE:
                    return kubeClient.nodes().withName(name).get();
                case HORIZONTALPODAUTOSCALER:
                    return kubeClient.autoscaling().horizontalPodAutoscalers().inNamespace(namespace).withName(name).get();
                case CONFIGMAP:
                    return kubeClient.configMaps().inNamespace(namespace).withName(name).get();
                case STATEFULSETS:
                    return kubeClient.apps().statefulSets().inNamespace(namespace).withName(name).get();
                case RESOURCEQUOTA:
                    return kubeClient.resourceQuotas().inNamespace(namespace).withName(name).get();
                case LIMITRANGE:
                	return kubeClient.limitRanges().inNamespace(namespace).withName(name).get();
                case JOB:
                	return kubeClient.extensions().jobs().inNamespace(namespace).withName(name).get();
                default:
                    return null;
            }
        } catch (Exception e) {
            logger.error("连接不上环境资源，获取不到数据信息, 请检查环境是否处于down机或者关闭状态", e);
            return null;
        }

    }

    /**
     * 根据lable获取pod
     *
     * @param namespace 命名空间
     * @param labels 过滤标签
     * @param resource  资源类型
     * @return 返回获取的对象列表
     */
    public Object list(String namespace, Map<String, String> labels, ApplicationEnum.RESOURCE resource) {
        try {
            switch (resource) {
                case STATEFULSETS:
                    return kubeClient.apps().statefulSets().inNamespace(namespace).withLabels(labels).list();
                case POD:
                    return kubeClient.pods().inNamespace(namespace).withLabels(labels).list();
                case DEPLOYMENT:
                    return kubeClient.extensions().deployments().inNamespace(namespace).withLabels(labels).list();
                default:
                    return null;
            }
        } catch (Exception e) {
            logger.error("连接不上环境资源，获取不到数据信息, 请检查环境是否处于down机或者关闭状态", e);
            return null;
        }

    }

    /**
     * 获取容器日志
     *
     * @param namespace
     * @param name
     * @param containerName
     * @return 返回容器的日志信息
     */
    public String log(String namespace, String name, String containerName, Integer line) {
        try {
            return kubeClient.pods().inNamespace(namespace).withName(name).inContainer(containerName)
                    .tailingLines(line).getLog();
        } catch (Exception e) {
            logger.error("连接不上环境资源，获取不到数据信息, 请检查环境是否处于down机或者关闭状态", e);
            return null;
        }
    }
    
    /**
     * 修改资源配置
     * @param namespace
     * @param name
     * @param hardMap
     * @return
     */
    public ResourceQuota editQuota(String namespace, String name, Map<String, Quantity> hardMap){
        try {
			return kubeClient.resourceQuotas().inNamespace(namespace).withName(name).edit().editSpec().addToHard(hardMap).endSpec().done();
		} catch (Exception e) {
			logger.error("修改资源配额异常", e);
			return null;
		}
   }

    /**
     * 校验资源名称的唯一性
     *
     * @param namespace
     * @param name
     * @param resource
     * @return
     */
    public boolean existed(String namespace, String name, ApplicationEnum.RESOURCE resource) {
        switch (resource) {
            case DEPLOYMENT:
                return null == kubeClient.extensions().deployments().inNamespace(namespace).withName(name).get() ? false
                        : true;
            case REPLICATIONCONTROLLER:
                return null == kubeClient.replicationControllers().inNamespace(namespace).withName(name).get() ? false
                        : true;
            case SERVICE:
                return null == kubeClient.services().inNamespace(namespace).withName(name).get() ? false : true;
            case PERSISTENTVOLUME:
                return null == kubeClient.persistentVolumes().withName(name).get() ? false : true;
            default:
                return false;
        }
    }

    // 根据deployment部署应用
    public Object deploy(String namespace, Object object, ApplicationEnum.RESOURCE resource) {
        switch (resource) {
            case DEPLOYMENT:
                return kubeClient.extensions().deployments().inNamespace(namespace).createOrReplace((Deployment) object);
            case REPLICATIONCONTROLLER:
                return kubeClient.replicationControllers().inNamespace(namespace)
                        .createOrReplace((ReplicationController) object);
            case SERVICE:
                return kubeClient.services().inNamespace(namespace).createOrReplace((Service) object);
            case PERSISTENTVOLUME:
                return kubeClient.persistentVolumes().createOrReplace((PersistentVolume) object);
            default:
                return null;
        }
    }

    /**
     * @param file 根据模板文件进行应用部署
     */
    public List<HasMetadata> load(File file) {
        try {
            if (!file.exists()) {
                logger.warn("模板文件不存在");
                return null;
            }
            return kubeClient.load(new FileInputStream(file)).createOrReplace();
        } catch (FileNotFoundException e) {
            logger.error("部署的模板文件不存在", e);
            return new ArrayList<>();
        }
    }

    /**
     * @param file 根据模板文件内容转换成对象
     */
    public List<HasMetadata> load(String file) {
        try {
            if (!StringUtils.hasText(file)) {
                logger.warn("模板文件路径不存在");
                return new ArrayList<>();
            }
            return kubeClient.load(new FileInputStream(file)).get();
        } catch (FileNotFoundException e) {
            logger.error("部署的模板文件内容转换成对象异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * @param metadatas 加载元数据
     */
    public List<HasMetadata> load(List<HasMetadata> metadatas) {
        try {
            return kubeClient.resourceList(metadatas).createOrReplace();
        } catch (Exception e) {
            logger.error("部署的模板文件内容或格式不对", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 运行pod的实例数变更
     *
     * @param namespace
     * @param name
     * @param number
     * @return
     */
    public Boolean scale(String namespace, String name, Integer number) {
        int actualNum = 0;
        if (null == kubeClient.extensions().deployments().inNamespace(namespace).withName(name).get()) {
            ReplicationController rc = kubeClient.replicationControllers().inNamespace(namespace).withName(name)
                    .scale(number, true);
            if (null == rc) {
                logger.warn("scale pod instances failed, please check your kubernetes environment is ready");
                return false;
            }
            actualNum = rc.getSpec().getReplicas();
        } else {
            Deployment deployment = kubeClient.extensions().deployments().inNamespace(namespace).withName(name)
                    .scale(number, true);
            if (null == deployment) {
                logger.warn("scale pod instances failed, please check your kubernetes environment is ready");
                return false;
            }
            actualNum = deployment.getSpec().getReplicas();
        }
        if (actualNum == 0) {
            return false;
        }
        return actualNum == number ? true : false;
    }

    /**
     * 变更运行的pod的资源使用情况
     *
     * @param namespace
     * @param name
     * @param limits
     * @param request
     * @return
     */
    public boolean scale(String namespace, String name, Map<String, Quantity> limits, Map<String, Quantity> request) {
        String type = "";
        List<Container> containers = new ArrayList<>();
        // 获取一个rc/deploy下所有的container列表
        Deployment deployment = kubeClient.extensions().deployments().inNamespace(namespace).withName(name).get();
        if (null != deployment) {
            containers = deployment.getSpec().getTemplate().getSpec().getContainers();
            type = "DEPLOY";
        } else {
            containers = kubeClient.replicationControllers().inNamespace(namespace).withName(name).get()
                    .getSpec().getTemplate().getSpec().getContainers();
            type = "RC";
        }

        if (ListTool.isEmpty(containers)) {
            logger.warn("There is no container found");
            return false;
        }

        // 变更container的资源信息
        switch (type) {
            case "DEPLOY":
                for (int i = 0; i < containers.size(); i++) {
                    kubeClient.extensions().deployments().inNamespace(namespace).withName(name).edit().editSpec().editTemplate()
                            .editSpec().editContainer(i).editOrNewResources().addToLimits(limits).addToRequests(request)
                            .endResources().endContainer().endSpec().endTemplate().endSpec().done();
                }
                break;
            case "RC":
                for (int i = 0; i < containers.size(); i++) {
                    kubeClient.replicationControllers().inNamespace(namespace).withName(name).edit().editSpec().editTemplate()
                            .editSpec().editContainer(i).editOrNewResources().addToLimits(limits).addToRequests(request)
                            .endResources().endContainer().endSpec().endTemplate().endSpec().done();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 根据镜像信息进行版本变更
     *
     * @param namespace
     * @param name      资源名称
     * @param newImage
     * @param oldImage
     * @return
     */
    public boolean rolling(String namespace, String name, String newImage, String oldImage,
                           ApplicationEnum.RESOURCE resource) {
        switch (resource) {
            case REPLICATIONCONTROLLER:
                ReplicationController rc = kubeClient.replicationControllers().inNamespace(namespace).withName(name).rolling()
                        .updateImage(newImage);
                if (!oldImage.equals(rc.getSpec().getTemplate().getSpec().getContainers().get(0).getImage())) {
                    return true;
                }
                logger.warn("rc rolling fail");
                return false;
            default:
                Deployment deployment = kubeClient.extensions().deployments().inNamespace(namespace).withName(name)
                        .edit().editSpec().editTemplate().editSpec().editFirstContainer().withImage(newImage)
                        .editOrNewResources().endResources().endContainer().endSpec().endTemplate().endSpec().done();
                if (!oldImage.equals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage())) {
                    return true;
                }
                logger.warn("deployment rolling fail");
                return false;
        }
    }

    /**
     * 回滚操作
     *
     * @param namespace
     * @param name
     * @param paramInfo
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean rollBack(String namespace, String name, String paramInfo) {
        JSONObject object = JSONObject.parseObject(paramInfo);
        Map<String, Quantity> limits = (Map<String, Quantity>) JSONObject.parseObject(object.getString("limits"), Map.class);
        Map<String, Quantity> request = (Map<String, Quantity>) JSONObject.parseObject(object.getString("requests"), Map.class);
        Integer replicas = object.getInteger("replicas") == null ? 0 : object.getInteger("replicas");
        String newImage = object.getString("image");
        Integer maxReplicas = object.getInteger("maxReplicas") == null ? 0 : object.getInteger("maxReplicas");
        Integer minReplicas = object.getInteger("minReplicas") == null ? 0 : object.getInteger("minReplicas");
        Integer targetCPUUtilizationPercentage = object.getInteger("targetCPUUtilizationPercentage") == null ? 0 : object.getInteger("targetCPUUtilizationPercentage");

        try {
            Deployment deployment = kubeClient.extensions().deployments().inNamespace(namespace).withName(name).get();
            if (null != deployment) {
                kubeClient.extensions().deployments().inNamespace(namespace).withName(name).edit().editSpec().withReplicas(replicas).editTemplate()
                        .editSpec().editContainer(0).withImage(newImage).editOrNewResources().addToLimits(limits).addToRequests(request)
                        .endResources().endContainer().endSpec().endTemplate().endSpec().done();

            } else {
                kubeClient.replicationControllers().inNamespace(namespace).withName(name).edit().editSpec().withReplicas(replicas).editTemplate()
                        .editSpec().editContainer(0).withImage(newImage).editOrNewResources().addToLimits(limits).addToRequests(request)
                        .endResources().endContainer().endSpec().endTemplate().endSpec().done();
            }

            //修改hpa
            if (0 != maxReplicas || 0 != minReplicas || 0 != targetCPUUtilizationPercentage) {
                kubeClient.autoscaling().horizontalPodAutoscalers().inNamespace(namespace).withName(name).edit().editSpec().withMaxReplicas(maxReplicas)
                        .withMinReplicas(minReplicas).withTargetCPUUtilizationPercentage(targetCPUUtilizationPercentage)
                        .endSpec().done();
            }
        } catch (Exception e) {
            logger.error("回滚操作失败", e);
            return false;
        }
        return true;
    }

    /**
     * 根据名称删除资源
     *
     * @param namespace
     * @param name
     * @return
     */
    public boolean remove(String namespace, String name, ApplicationEnum.RESOURCE resource) {

        switch (resource) {
            case NAMESPACE:
                return kubeClient.namespaces().withName(name).delete();
            case DEPLOYMENT:
                return kubeClient.extensions().deployments().inNamespace(namespace).withName(name).delete();
            case REPLICATIONCONTROLLER:
                return kubeClient.replicationControllers().inNamespace(namespace).withName(name).delete();
            case SERVICE:
                return kubeClient.services().inNamespace(namespace).withName(name).delete();
            case INGRESS:
                return kubeClient.extensions().ingresses().inNamespace(namespace).withName(name).delete();
            case PERSISTENTVOLUME:
                return kubeClient.persistentVolumes().withName(name).delete();
            case PERSISTENTVOLUMECLAIMS:
                return kubeClient.persistentVolumeClaims().inNamespace(namespace).withName(name).delete();
            case HORIZONTALPODAUTOSCALER:
                return kubeClient.autoscaling().horizontalPodAutoscalers().inNamespace(namespace).withName(name).delete();
            case CONFIGMAP:
                return kubeClient.configMaps().inNamespace(namespace).withName(name).delete();
            case STATEFULSETS:
                return kubeClient.apps().statefulSets().inNamespace(namespace).withName(name).delete();
            case RESOURCEQUOTA:
                return kubeClient.resourceQuotas().inNamespace(namespace).withName(name).delete();
            case LIMITRANGE:
                return kubeClient.limitRanges().inNamespace(namespace).withName(name).delete();
            case JOB:
                return kubeClient.extensions().jobs().inNamespace(namespace).withName(name).delete();
            default:
                logger.warn("delete resource type is supported");
                return false;
        }

    }

    /**
     * 将服务名称和端口加入到ingress
     *
     * @param namespace
     * @param serviceName
     * @param servicePort
     * @return
     */
    @SuppressWarnings("deprecation")
    public boolean loadServicePath(String appName, String namespace, String serviceName, IntOrString servicePort) {

        Ingress ingress = kubeClient.extensions().ingress().inNamespace(namespace).withName(appName).get();
        if (null == ingress) {
            logger.warn("Get ingress of namespace[" + namespace + "] is null");
            return false;
        }

        IngressBackend backend = new IngressBackend(serviceName, servicePort);
        List<HTTPIngressPath> paths = new ArrayList<>();

        paths.add(new HTTPIngressPath(backend, "/" + appName + "/" + serviceName));

        Ingress editIngress = kubeClient.extensions().ingresses().inNamespace(namespace)
                .withName(ingress.getMetadata().getName()).edit().editSpec()
                .addToRules(new IngressRule(null, new HTTPIngressRuleValue(paths))).endSpec().done();

        if (editIngress.getSpec().getRules().size() == ingress.getSpec().getRules().size() + 1) {
            return true;
        }
        logger.warn("load serviceIngress path fail");
        return false;
    }

    /**
     * 删除Ingress里的某服务暴露出来的路径
     *
     * @param namespace
     * @param appName
     * @param svcName
     * @return
     */
    @SuppressWarnings("deprecation")
    public boolean removeServicePath(String namespace, String appName, String svcName) {
        List<IngressRule> nodeleteRules = new ArrayList<>();//存储不需要删除的服务暴露路径
        Ingress ingress = kubeClient.extensions().ingress().inNamespace(namespace).withName(appName).get();
        if (null == ingress) {
            logger.warn("Get ingress of appName/namespace[" + appName + "/" + namespace + "] is null");
            return false;
        }

        List<IngressRule> rules = ingress.getSpec().getRules();
        if (rules.size() == 1) { //若ingress里只有一条信息，则需要删除整个ingress资源，因为ingress不允许rules为空
            Boolean deleted = kubeClient.extensions().ingress().inNamespace(namespace).withName(appName).delete();
            if (null == deleted || deleted == false) {
                return false;
            }
            return true;
        }

        for (IngressRule ingressRule : rules) {
            List<HTTPIngressPath> paths = ingressRule.getHttp().getPaths();
            for (HTTPIngressPath httpIngressPath : paths) {
                if (!svcName.equals(httpIngressPath.getBackend().getServiceName())) {
                    nodeleteRules.add(ingressRule);
                    continue;
                }
                Ingress deleteIngress = kubeClient.extensions().ingresses().inNamespace(namespace)
                        .withName(appName).edit().editSpec().withRules(nodeleteRules)
                        .removeFromRules(ingressRule).endSpec().done();
                if (deleteIngress.getSpec().getRules().size() == ingress.getSpec().getRules().size() - 1) {
                    return true;
                }
                logger.warn("delete serviceIngress path fail");
                return false;
            }
        }
        logger.warn("服务端Ingress资源对象中不存在暴露的服务路径，或该服务暴露的路径不存在");
        return false;
    }

    /**
     * 在应用第一次部署的时候，没有ingress存在。创建ingress
     *
     * @param appName
     * @param serviceName
     * @param servicePort
     * @param nameSpace
     * @return
     */
    @SuppressWarnings("deprecation")
    public Ingress createServerIngress(String appName, String serviceName, IntOrString servicePort, String nameSpace) {

        Ingress ingress = new Ingress();
        ObjectMeta objectMeta = new ObjectMeta();

        objectMeta.setName(appName);
        objectMeta.setNamespace(nameSpace);

        Map<String, String> annotations = new HashMap<>();
        annotations.put("ingress.kubernetes.io/rewrite-target", "/");
        objectMeta.setAnnotations(annotations);
        ingress.setMetadata(objectMeta);

        List<IngressRule> ingressRules = new ArrayList<>();
        IngressBackend backend = new IngressBackend(serviceName, servicePort);
        List<HTTPIngressPath> paths = new ArrayList<>();

        paths.add(new HTTPIngressPath(backend, "/" + appName + "/" + serviceName));

        ingressRules.add(new IngressRule(null, new HTTPIngressRuleValue(paths)));
        ingress.setSpec(new IngressSpec(null, ingressRules, null));

        return kubeClient.extensions().ingress().create(ingress);
    }

    /**
     * 删除服务ingress
     *
     * @param namespace
     * @param names
     * @return
     */
    @SuppressWarnings("deprecation")
    public boolean remove(String namespace, List<String> names) {

        //获取 该namespace下的ingress
        List<Ingress> items = kubeClient.extensions().ingress().inNamespace(namespace).list().getItems();
        if (ListTool.isEmpty(items)) {
            return true;
        }
        Ingress ingress = items.get(0);
        if (null == ingress) {
            logger.info("namespace[" + namespace + "] not have ingress");
            return true;
        }

        //获取需要删除的服务rule,并且删除
        int count = 0;
        List<IngressRule> rules = ingress.getSpec().getRules();
        Iterator<IngressRule> iterator = rules.iterator();
        while (iterator.hasNext()) {
            IngressRule ingressRule = (IngressRule) iterator.next();
            String serviceName = ingressRule.getHttp().getPaths().get(0).getBackend().getServiceName();
            if (names.contains(serviceName)) {
                count++;
                iterator.remove();
            }
        }

        //如果被删除的服务都没有被暴露，则不执行ingress删除操作
        if (count == 0) {
            logger.info("service not exposed, not ingress need to deleted");
            return true;
        }

        //若全删除该ingress里的所有被暴露的服务信息，则直接删除整个ingress,因为ingress不允许rules为空
        if (ListTool.isEmpty(rules)) {
            boolean deleted = remove(namespace, ingress.getMetadata().getName(), ApplicationEnum.RESOURCE.INGRESS);
            if (!deleted) {
                logger.warn("delete ingress fail, please check resource connection");
                return false;
            }
            return true;
        }

        //移除被删除服务的暴露访问信息
        Ingress editIngress = kubeClient.extensions().ingresses().inNamespace(namespace)
                .withName(ingress.getMetadata().getName()).edit().editSpec().withRules(rules)
                .removeAllFromRules(rules).endSpec().done();

        if (editIngress.getSpec().getRules().size() == ingress.getSpec().getRules().size()) {
            return true;
        }
        logger.warn("remove service ingress fail");
        return false;
    }

    public boolean createPvcByPv(String pvName, String pvcName, String namespace) {

        PersistentVolume persistentVolume = kubeClient.persistentVolumes().withName(pvName).get();
        PersistentVolumeClaim pvc = new PersistentVolumeClaim();
        pvc.setApiVersion("v1");
        pvc.setKind("PersistentVolumeClaim");
        // 设置metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(pvcName);
        metadata.setNamespace(namespace);
        pvc.setMetadata(metadata);

        // 创建spec
        PersistentVolumeClaimSpec spec = new PersistentVolumeClaimSpec();
        // 创建accessMode
        spec.setAccessModes(persistentVolume.getSpec().getAccessModes());

        // 创建ResourceRequirement
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Quantity quantity = persistentVolume.getSpec().getCapacity().get("storage");

        Map<String, Quantity> quantityMap = new HashMap<>();
        quantityMap.put("storage", quantity);
        resourceRequirements.setRequests(quantityMap);
        spec.setResources(resourceRequirements);

        pvc.setSpec(spec);
        PersistentVolumeClaim pvcResult = kubeClient.persistentVolumeClaims().create(pvc);
        if (null != pvcResult) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param namespace
     * @param names
     * @param resource
     * @return
     */
    public boolean remove(String namespace, List<String> names, ApplicationEnum.RESOURCE resource) {

        int deleted = 0;
        for (String name : names) {
            boolean singleDeleted = remove(namespace, name, resource);
            if (singleDeleted) {
                deleted++;
            }
        }

        if (names.size() != deleted) {
            logger.warn("delete some resources from kubernetes server failed, please check kubernetes environment");
            return false;
        }
        return true;
    }

    /**
     * @param file 根据文件删除资源主题对象
     */
    public boolean remove(File file) {
        try {
            return kubeClient.load(new FileInputStream(file)).delete();
        } catch (FileNotFoundException e) {
            logger.error("file not found", e);
            return false;
        }
    }
    
    /**
	 * 创建LimitRange资源对象
	 * @param temObject
	 * @param limitRangeObject
	 * @return
	 */
	public boolean createLimitRange(String name, String namespace, JSONObject limitRangeObject){
		LimitRange limitRange = new LimitRange();
		limitRange.setApiVersion("v1");
	    limitRange.setKind("LimitRange");
		//创建ObjectMeta对象
		ObjectMeta objectMeta = new ObjectMeta();
	    objectMeta.setName(name);
	    objectMeta.setNamespace(namespace);
	    limitRange.setMetadata(objectMeta);
	    
	    //创建LimitRangeSpec对象
	    LimitRangeSpec limitRangeSpec = new LimitRangeSpec();
	    
	    //获取LimitRangeSpec的集合
	    List<LimitRangeItem> items = createLimitRangeItem(limitRangeObject);
		
	    limitRangeSpec.setLimits(items);
	    limitRange.setSpec(limitRangeSpec);
	    
	    LimitRange newLimitRange = kubeClient.limitRanges().createOrReplace(limitRange);
	    if (null == newLimitRange) {
	    	logger.warn("创建LimitRange失败");
			return false;
		}
		return true;
	}
	
	public boolean editLimitRange(String name, String namespace, JSONObject limitRangeObject){
	   //获取LimitRangeSpec的集合
		List<LimitRangeItem> newItems = createLimitRangeItem(limitRangeObject);
		//修改
	    LimitRange editLimitRange = kubeClient.limitRanges().inNamespace(namespace).withName(name)
	    		.edit().editSpec()
	    		.withLimits(new ArrayList<LimitRangeItem>())
	    		.removeAllFromLimits(new ArrayList<LimitRangeItem>())
	    		.addAllToLimits(newItems)
	    		.endSpec().done();
	    
	    if (null == editLimitRange) {
	    	logger.warn("修改LimitRange失败");
			return false;
		}
		return true;
	
	}
	/**
	 * LimitRangeSpec的集合
	 * @param limitRangeObject
	 * @return
	 */
	private List<LimitRangeItem> createLimitRangeItem(JSONObject limitRangeObject){
		 //创建LimitRangeSpec对象的集合
	    List<LimitRangeItem> list = new ArrayList<>();
		
		/**
		 * 对pod类型的资源极限范围的设置
		 */
	    LimitRangeItem podItem = null;
		JSONObject podObject = limitRangeObject.getJSONObject("pod");
		if (!podObject.isEmpty()) {
			//创建LimitRangeItem对象
			podItem = new LimitRangeItem();
			list.add(podItem);
		    podItem.setType("Pod");
		    
		    HashMap<String, Quantity> maxMap = new HashMap<>();
		    Quantity cpuQua = new Quantity();
		    cpuQua.setAmount(podObject.getString("maxPodCpu"));
		    maxMap.put("cpu", cpuQua);
		    Quantity memoryQua = new Quantity();
		    memoryQua.setAmount(podObject.getString("maxPodMemory"));
		    maxMap.put("memory", memoryQua);
		    podItem.setMax(maxMap);
			
			HashMap<String, Quantity> minMap = new HashMap<>();
		    Quantity minMemQua = new Quantity();
		    minMemQua.setAmount(podObject.getString("minPodMemory"));
		    minMap.put("memory", minMemQua);
		    Quantity minCpuQua = new Quantity();
		    minCpuQua.setAmount(podObject.getString("minPodCpu"));
		    minMap.put("cpu", minCpuQua);
		    podItem.setMin(minMap);;
		}
		
		/**
		 * 对container类型的资源极限范围的设置
		 */
		JSONObject containerObject = limitRangeObject.getJSONObject("container");
		JSONObject defaultContainerObject = limitRangeObject.getJSONObject("defaultContainer");
		LimitRangeItem containerItem = null;
		if (!containerObject.isEmpty() || !defaultContainerObject.isEmpty()) {
			//创建LimitRangeItem对象
			containerItem = new LimitRangeItem();
			list.add(containerItem);
			containerItem.setType("Container");
		}
		
		if (!containerObject.isEmpty()) {
			HashMap<String, Quantity> maxMap = new HashMap<>();
		    Quantity cpuQua = new Quantity();
		    cpuQua.setAmount(containerObject.getString("maxContainerCpu"));
		    maxMap.put("cpu", cpuQua);
		    Quantity memoryQua = new Quantity();
		    memoryQua.setAmount(containerObject.getString("maxContainerMemory"));
		    maxMap.put("memory", memoryQua);
		    containerItem.setMax(maxMap);
			
			HashMap<String, Quantity> minMap = new HashMap<>();
		    Quantity minMemQua = new Quantity();
		    minMemQua.setAmount(containerObject.getString("minContainerCpu"));
		    minMap.put("cpu", minMemQua);
		    Quantity minCpuQua = new Quantity();
		    minCpuQua.setAmount(containerObject.getString("minContainerMemory"));
		    minMap.put("memory", minCpuQua);
		    containerItem.setMin(minMap);
		}
		
		if (!defaultContainerObject.isEmpty()) {
			HashMap<String, Quantity> defaultMap = new HashMap<>();
		    Quantity cpuQua = new Quantity();
		    cpuQua.setAmount(defaultContainerObject.getString("defaultCpu"));
		    defaultMap.put("cpu", cpuQua);
		    Quantity memoryQua = new Quantity();
		    memoryQua.setAmount(defaultContainerObject.getString("defaultMemory"));
		    defaultMap.put("memory", memoryQua);
		    containerItem.setDefault(defaultMap);
			
			HashMap<String, Quantity> reqMap = new HashMap<>();
		    Quantity reqMemQua = new Quantity();
		    reqMemQua.setAmount(defaultContainerObject.getString("defaultReqMemory"));
		    reqMap.put("memory", reqMemQua);
		    Quantity reqCpuQua = new Quantity();
		    reqCpuQua.setAmount(defaultContainerObject.getString("defaultReqCpu"));
		    reqMap.put("cpu", reqCpuQua);
		    containerItem.setDefaultRequest(reqMap);
		}
		
		return list;
	}

    public String getProxy() {
        return proxy;
    }

    public void setProxyUrl(String proxy) {
        this.proxy = proxy;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }


    /**
     * @return the kubeClient
     */
    public KubernetesClient getKubeClient() {
        return kubeClient;
    }


    public ApplicationClient(String proxy, String port) {
        super();
        this.proxy = proxy;
        this.port = port;
        this.build();
    }

    public ApplicationClient() {
    }

}
