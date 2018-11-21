package com.bocloud.paas.service.application;

import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.paas.entity.ServiceAlarm;

import io.fabric8.kubernetes.api.model.Quantity;

public interface Service {
	
	/**
	 * 服务列表信息
	 * @param applicationId
	 * @return
	 */
	public BsmResult list(Long applicationId);
	
	/**
	 * 单个服务的详情信息
	 * @param applicationId
	 * @param name
	 * @return
	 */
	public BsmResult detail(Long applicationId, String name);
	
	/**
	 * 名称唯一性校验
	 * @param applicationId
	 * @param name
	 * @return
	 */
	public boolean existed(Long applicationId, String name);
	
	/**
	 * 统计服务中资源情况
	 * @param applicationId
	 * @param name
	 */
	//TODO
	public void statistic(Long applicationId, String name);
	
	/**
	 * 根据编排文件部署服务
	 * @param applicationId
	 * @param layoutId
	 */
	//TODO
	public BsmResult deploy(Long applicationId, Long layoutId);
	
	/**
	 * 根据镜像模板部署服务
	 * @param applicationId
	 * @param layoutId
	 */
	public BsmResult deploy(Long applicationId, Object object);
	
	/**
	 * 调整服务实例数
	 * @param applicationId
	 * @param name
	 * @param number
	 * @return
	 */
	public BsmResult scale(Long applicationId, String name, Integer number, Long userId);
	
	
	/**
	 * 调整服务实例的资源限制情况
	 * @param applicationId
	 * @param name
	 * @param limits
	 * @param request
	 * @return
	 */
	public BsmResult scaleResource(Long applicationId, String name, Map<String, Quantity> limits, Map<String, Quantity> request,
			Long userId);
	
	/**
	 * 获取部署服务的限制资源
	 * @param id
	 * @param name
	 * @return
	 */
	public BsmResult resource(Long id, String name); 
	
	/**
	 * 根据提供的镜像对服务实例进行变更
	 * @param applicationId
	 * @param name
	 * @param image
	 * @return
	 */
	public BsmResult rolling(Long applicationId, String name, String image, Long userId);
	/**
	 * 回滚操作
	 * @param applicationId
	 * @param paramInfo
	 * @param userId
	 * @return
	 */
	public BsmResult rollBack(Long applicationId, String name, String paramInfo,Long userId);
	
	//弹性伸缩
	//TODO
	public void autoScale(Long applicationId, String name, Map<String, Object> policy);
	
	/**
	 * 根据名称删除服务资源
	 * @param applicationId
	 * @param name
	 * @return
	 */
	public boolean remove(Long applicationId, String name);
	
	/**
	 * 根据服务名称批量删除资源
	 * @param applicationId
	 * @param names
	 * @return
	 */
	public BsmResult remove(Long applicationId, List<String> names);
	/**
	 * 获取服务之间的依赖关系
	 * @param currentName
	 * @param currentNamespace
	 * @return
	 */
	public BsmResult gplot(String currentName, String currentNamespace);
	/**
	 * 创建hps
	 * @param applicationId
	 * @param object
	 * @return
	 */
	public BsmResult createHpa(Long applicationId, String name, JSONObject hpa, Long userId);
	/**
	 * 获取hpa信息
	 * @param applicationId
	 * @param name  hpa名称
	 * @return
	 */
	public BsmResult hpa(Long applicationId, String name);
	/**
	 * 
	 * @param applicationId
	 * @param name 服务名称
	 * @return
	 */
	public BsmResult removeHpa(Long applicationId, String name);
	/**
	 * 获取服务使用的配置项信息
	 * @param applicationId
	 * @param cmNames
	 * @return
	 */
	public BsmResult configMap(Long applicationId, String name, String resourceType);
	/**
	 * 服务告警详情
	 * @param applicationId
	 * @param name
	 * @return
	 */
	public BsmResult serviceAlarmDetail(Long applicationId, String name);
	/**
	 * 服务告警-策略保存
	 * @param serviceAlarm
	 * @return
	 */
	public BsmResult serviceAlarmSave(ServiceAlarm serviceAlarm);
	/**
	 * 服务告警-策略修改
	 * @param saId
	 * @return
	 */
	public BsmResult serviceAlarmModify(ServiceAlarm serviceAlarm);
	/**
	 * 服务告警监控
	 */
	public void serviceAlarmMonitor();
	
	public BsmResult listJob(Long applicationId);
	
	public BsmResult deleteJob(Long applicationId, String jobName);
	
}
