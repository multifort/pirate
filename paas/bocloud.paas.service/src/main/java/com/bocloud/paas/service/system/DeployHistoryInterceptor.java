package com.bocloud.paas.service.system;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.paas.dao.application.ApplicationDao;
import com.bocloud.paas.dao.application.DeployHistoryDao;
import com.bocloud.paas.dao.environment.EnvironmentDao;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.DeployHistory;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.common.enums.ApplicationEnum;
import com.bocloud.paas.service.application.util.ApplicationClient;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

@Aspect
@Component("deployHistoryInterceptor")
public class DeployHistoryInterceptor {
	private static Logger logger = LoggerFactory.getLogger(DeployHistoryInterceptor.class);

	@Autowired
	private DeployHistoryDao deployHistoryDao;
	@Autowired
	private ApplicationDao applicationDao;
	@Autowired
	private EnvironmentDao environmentDao;
	
	private DeployHistory deployHistory ;
	private JSONObject dataMap;
	
	@Pointcut("execution (* com.bocloud.paas.service.application.Impl.ServiceImpl.rolling(..)) "
			+ "|| execution (* com.bocloud.paas.service.application.Impl.ServiceImpl.scale(..)) "
			+ "|| execution (* com.bocloud.paas.service.application.Impl.ServiceImpl.scaleResource(..))"
			+ "|| execution (* com.bocloud.paas.service.application.Impl.ServiceImpl.createHpa(..))"
			+ "|| execution (* com.bocloud.paas.service.application.Impl.ServiceImpl.rollBack(..))")
    private void anyMethod() {} // 声明一个切入点，anyMethod为切入点名称
	
	@Before(value="anyMethod()")
    public void before(JoinPoint joinPoint) {
		logger.info("服务资源信息变更前，参数信息获取...");
        ApplicationClient client = null;
        this.deployHistory = new DeployHistory();
	    
        //获取方法参数信息
        Map<String, Object> paramMap = getParams(joinPoint);
        if (null == paramMap || paramMap.isEmpty()) {
			return;
		}
        
        Long applicationId = Long.valueOf(paramMap.get("applicationId").toString());
        String name = paramMap.get("name").toString();
        Long userId = (Long) paramMap.get("userId");
        
        //根据请求的参数不同，做不同的逻辑处理
        switchCase(paramMap);
        
        // 获取namespace信息
 		Application application = getApplication(applicationId);
 		if (null == application) {
 			return;
 		}
 		//获取资源环境
 		client = getClient(application.getEnvId());
	     
        //获取原资源信息
 		StringBuffer buffer = getResources(application.getNamespace(), name, client);
	 	
	 	//获取hpa信息
	 	Map<String, Object> hpa = getHpa(application.getNamespace(), name, client);
	 	
	 	Iterator<Entry<String, Object>> it = hpa.entrySet().iterator();
	 	buffer.append(", 弹性伸缩值[");
	 	while (it.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
			
			buffer.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
		}
	 	buffer.append("]");
		
        //部署历史记录信息记录
		deployHistory.setServiceName(name);
		deployHistory.setApplicationId(applicationId);
		deployHistory.setCreateId(userId);
		deployHistory.setGmtCreate(new Date());
		deployHistory.setDataInfo(buffer.toString());
		deployHistory.setParamInfo(dataMap.toString());
		deployHistory.setVersion(new Date());
		
		client.close();
        
    }
	
	@AfterReturning(pointcut="anyMethod()",returning="result")
	public void afterReturning(JoinPoint joinPoint, BsmResult result) {
       logger.info("服务资源信息变更后，数据返回...");
        //保存部署记录
  		if (!result.isSuccess()) {
  			this.deployHistory.setResult("失败");
  		} else {
  			this.deployHistory.setResult("成功");
  		}
  		if (!save(deployHistory)) {
  			if (!result.isSuccess()) {
  				logger.warn("rolling up fail, save deploy history fail");
  			} else {
  				logger.warn("rolling up success, save deploy history fail");
  			}
  		}
    }
	/**
	 * 根据请求的参数不同，做不同的逻辑处理
	 * @param paramMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void switchCase(Map<String, Object> paramMap){
		if (paramMap.containsKey("number")) {
        	Integer number = Integer.valueOf(paramMap.get("number").toString());
        	this.deployHistory.setObject("弹性伸缩-手动方式");
    		this.deployHistory.setRemark("变更参数: "+number);
    		
		} else if (paramMap.containsKey("hpa")) {
			JSONObject object = (JSONObject) paramMap.get("hpa");
			this.deployHistory.setObject("弹性伸缩-自动方式");
			this.deployHistory.setRemark("变更参数: 最大实例数="+object.get("maxReplicas")
					+", 最小实例数="+object.get("minReplicas")
					+", 触发阀值="+object.get("cpuPercent"));
			
		} else if (paramMap.containsKey("limits") && paramMap.containsKey("request")) {
			Map<String, String> limits = (Map<String, String>) paramMap.get("limits");
			Map<String, String> requests = (Map<String, String>) paramMap.get("request");
			
			this.deployHistory.setObject("资源限制");
			this.deployHistory.setRemark("变更参数: "
    				+ "cpu资源限制="+limits.get("cpu")+", 内存资源限制="+limits.get("memory")
    				+ "cpu资源请求="+requests.get("cpu")+", 内存资源请求="+requests.get("memory"));
    		
		} else if (paramMap.containsKey("image")) {
			String image = (String) paramMap.get("image");
			this.deployHistory.setObject("滚动升级");
			this.deployHistory.setRemark("变更参数: "+image);
    		
		} else if (paramMap.containsKey("paramInfo")) {
			String paramInfo = (String) paramMap.get("paramInfo");
			JSONObject object = JSONObject.parseObject(paramInfo);
			String param = "实例数= "+object.get("replicas")
			+ ", 镜像版本= "+object.get("image")
			+ ", 弹性伸缩值[最大实例数="+object.get("maxReplicas")+",最小实例数="+object.get("minReplicas")+",触发阀值="+object.get("targetCPUUtilizationPercentage")+"]";
			
			param += ", 资源限制[ ";
			if (null != object.get("limits")) {
				JSONObject limits = JSONObject.parseObject(object.get("limits").toString());
				param += "cpu资源限制= "+limits.getString("cpu")+", 内存资源限制= "+limits.getString("memory");
			}
			if (null != object.get("requests")) {
				JSONObject requests = JSONObject.parseObject(object.get("requests").toString());
				param += ", cpu资源请求= "+requests.getString("cpu")+", 内存资源请求= "+requests.getString("memory");
			}
			param += " ]";
			this.deployHistory.setObject("版本回滚");
			this.deployHistory.setRemark("变更参数: "+param);
    		
		}
	}
      		
	/**
	 * 保存部署信息
	 * @param deployHistory
	 * @return
	 */
	private boolean save(DeployHistory deployHistory) {
		boolean result = false;
		try {
			result = deployHistoryDao.insert(deployHistory);
		} catch (Exception e) {
			logger.error("保存部署历史数据失败！", e);
		}
		if (!result) {
			logger.error("保存部署历史数据失败！");
		}
		return result;
	}
	/**
	 * 获取实例数
	 * @param namespace
	 * @param name
	 * @param client
	 * @return
	 */
	private StringBuffer getResources(String namespace, String name, ApplicationClient client){
		StringBuffer buffer = new StringBuffer();
		dataMap = new JSONObject();
		ResourceRequirements resources = null;
		Integer replicas = 0;
		String image = null;
		try {
			Deployment deploy = (Deployment) client
					.detail(namespace, name,  ApplicationEnum.RESOURCE.DEPLOYMENT);
			if (null == deploy) {
				ReplicationController rc = (ReplicationController) client
				    .detail(namespace, name,  ApplicationEnum.RESOURCE.REPLICATIONCONTROLLER);
				resources = rc.getSpec().getTemplate().getSpec().getContainers().get(0).getResources();
				replicas = rc.getSpec().getReplicas();
				image = rc.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
			} else {
				resources = deploy.getSpec().getTemplate().getSpec().getContainers().get(0).getResources();
				replicas = deploy.getSpec().getReplicas();
				image = deploy.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
			}
			
			buffer.append("实例数= ").append(replicas).append(",");
			buffer.append("镜像版本= ").append(image).append(",");
			
			dataMap.put("replicas", replicas);
			dataMap.put("image", image);
			
			buffer.append(", 资源限制[ ");
			if (null != resources.getLimits()) {
				if (null != resources.getLimits().get("cpu")) {
					buffer.append("cpu资源限制= ").append(resources.getLimits().get("cpu").getAmount()).append(",");
				}
				if (null != resources.getLimits().get("memory")) {
					buffer.append("内存资源限制= ").append(resources.getLimits().get("memory").getAmount()).append(",");
				}
				HashMap<String, String> limMap = new HashMap<>();
				limMap.put("cpu", resources.getLimits().get("cpu").getAmount());
				limMap.put("memory", resources.getLimits().get("memory").getAmount());
				dataMap.put("limits", limMap);
			} else {
				dataMap.put("limits", null);
			}
			if (null != resources.getRequests()) {
				if (null != resources.getRequests().get("cpu")) {
					buffer.append("cpu资源请求= ").append(resources.getRequests().get("cpu").getAmount()).append(",");
				}
				if (null != resources.getRequests().get("memory")) {
					buffer.append("内存资源请求= ").append(resources.getRequests().get("memory").getAmount());
				}
				HashMap<String, String> reqMap = new HashMap<>();
				reqMap.put("cpu", resources.getRequests().get("cpu").getAmount());
				reqMap.put("memory", resources.getRequests().get("memory").getAmount());
				dataMap.put("requests", reqMap);
			} else {
				dataMap.put("requests", null);
			}
			buffer.append(" ], ");
			
		} catch (Exception e) {
			logger.error("获取资源信息异常，请检查网络连接是否正常", e);
		}finally {
			client.close();
		}
		this.deployHistory.setParamInfo(dataMap.toJSONString());
		return buffer;
	}
	/**
	 * 获取hpa信息
	 * @param namespace
	 * @param name
	 * @param client
	 * @return
	 */
	private Map<String, Object> getHpa(String namespace, String name, ApplicationClient client){
		Map<String, Object> hpaMap = new HashMap<>();
		try {
			 HorizontalPodAutoscaler hpa = (HorizontalPodAutoscaler) client
					.detail(namespace, name,  ApplicationEnum.RESOURCE.HORIZONTALPODAUTOSCALER);
			 
			 if (null == hpa) {
				 hpaMap.put("最大实例数", 0);
				 hpaMap.put("最小实例数", 0);
				 hpaMap.put("触发阀值", 0);
				 
				 dataMap.put("maxReplicas", 0);
				 dataMap.put("minReplicas", 0);
				 dataMap.put("targetCPUUtilizationPercentage", 0);
			} else {
				hpaMap.put("最大实例数", hpa.getSpec().getMaxReplicas());
				hpaMap.put("最小实例数", hpa.getSpec().getMinReplicas());
				hpaMap.put("触发阀值", hpa.getSpec().getTargetCPUUtilizationPercentage());
				
				dataMap.put("maxReplicas", hpa.getSpec().getMaxReplicas());
				dataMap.put("minReplicas", hpa.getSpec().getMinReplicas());
				dataMap.put("targetCPUUtilizationPercentage", hpa.getSpec().getTargetCPUUtilizationPercentage());
			}
			 
		} catch (Exception e) {
			logger.error("获取资源信息异常，请检查网络连接是否正常", e);
		}finally {
			client.close();
		}
		return hpaMap;
	}
	/**
	 * 获取环境资源连接
	 * @param EnvId
	 * @return
	 */
	private ApplicationClient getClient(Long EnvId){
		Environment env = getEnv(EnvId);
		if (null == env) {
			return null;
		}
		// 获取详细的资源信息，包括service和deployment
		return new ApplicationClient(env.getProxy(), String.valueOf(env.getPort()));
	}
	/**
	 * 获取方法请求参数
	 * @param joinPoint
	 * @return
	 */
	private Map<String, Object> getParams(JoinPoint joinPoint){
		Map<String, Object> paramMap = null;
        // 获取目标对象类的类名称
        String className = joinPoint.getTarget().getClass().getName();
		try {  
	        Class<?> clazz = Class.forName(className);    
	        String clazzName = clazz.getName();
			// 获取目标方法
	        String methodName = joinPoint.getSignature().getName();
	        Object[] args = joinPoint.getArgs();//参数  
	         //获取参数名称匹配对应的参数值  
			try {
				paramMap = getFieldsName(this.getClass(), clazzName, methodName,args);
			} catch (NotFoundException e) {
				logger.error("get param with value exception", e);
			}
		} catch (ClassNotFoundException e) {
			logger.error("reflect exception", e);
		} 
		return paramMap;
	}

	@SuppressWarnings("rawtypes")
	private Map<String,Object> getFieldsName(Class cls, String clazzName, String methodName, Object[] args) throws NotFoundException {   
        Map<String,Object > map=new HashMap<String,Object>();  
          
        ClassPool pool = ClassPool.getDefault();    
        ClassClassPath classPath = new ClassClassPath(cls);    
        pool.insertClassPath(classPath);    
            
        CtClass cc = pool.get(clazzName);    
        CtMethod cm = cc.getDeclaredMethod(methodName);    
        MethodInfo methodInfo = cm.getMethodInfo();  
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();    
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);    
        if (attr == null) {    
            logger.warn("LocalVariableAttribute is null");
            return map;
        }    
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;    
        for (int i = 0; i < cm.getParameterTypes().length; i++){    
            map.put( attr.variableName(i + pos),args[i]);//paramNames即参数名    
        }    
          
        //Map<>  
        return map;    
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
}
