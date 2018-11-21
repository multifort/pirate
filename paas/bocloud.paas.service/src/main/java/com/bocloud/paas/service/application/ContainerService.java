package com.bocloud.paas.service.application;

import java.util.Map;

import com.bocloud.common.model.BsmResult;

/**
 * @author Zaney
 * @param <LogWatch>
 * @data:2017年3月9日
 * @describe:页面数据统计业务逻辑层
 */
public interface ContainerService {
	/**
	 * 获取页面数据统计
	 * 
	 * @param clusterProxyIp
	 * @param clusterUsername
	 * @param clusterPassword
	 * @return
	 */
	public BsmResult total(Long envId);

	/**
	 * 获取容器平台资源列表
	 * 
	 * @param clusterIp
	 * @param clusterProxyIp
	 * @param clusterUsername
	 * @param clusterPassword
	 * @param resourceType
	 * @return
	 */
	public BsmResult list(String resourceType, String resourceName, Long envId, String namespace);
	/**
	 * 获取应用平台资源列表
	 * 
	 * @param clusterIp
	 * @param clusterProxyIp
	 * @param clusterUsername
	 * @param clusterPassword
	 * @param resourceType
	 * @return
	 */
	public BsmResult list(Map<String, String> labels, Long appId);

	/**
	 * 容器平台获取资源详情
	 * @param resourceType
	 * @param namespace
	 * @param resourceName
	 * @param envId
	 * @return
	 */
	public BsmResult detail(String resourceType, String namespace, String resourceName, Long envId);
	/**
	 * 应用平台获取资源详情
	 * @param resourceName
	 * @param appId
	 * @return
	 */
	public BsmResult detail(String resourceName, Long appId, String resourceType);

	/**
	 * 容器平台事件列表
	 * @param namespace
	 * @param resourceType
	 * @param resourceName
	 * @param envId
	 * @return
	 */
	public BsmResult event(String namespace, String resourceName, Long envId);
	/**
	 * 应用平台事件列表
	 * @param resourceName
	 * @param appId
	 * @return
	 */
	public BsmResult event(String resourceName, Long appId);

	/**
	 * 容器平台日志获取
	 * 
	 * @param namespace
	 * @param resourceName
	 * @return
	 */
	public BsmResult getLog(String namespace, String resourceName, String containerName, String status, Long envId, Integer line);
	/**
	 * 应用平台日志获取
	 * @param resourceName
	 * @param containerName
	 * @param status
	 * @param appId
	 * @return
	 */
	public BsmResult getLog(String resourceName, String containerName, String status, Long appId, Integer line);

	/**
	 * 获取节点下的pod
	 * 
	 * @param proxyId
	 * @param nodeName
	 * @return
	 */
	public BsmResult getNodePod(Long envId, String nodeIP);
	/**
	 * 获取pod模板信息
	 * @param application
	 * @param name  pod名称
	 * @return
	 */
	public BsmResult template(Long applicationId, String name);
	/**
	 * 获取服务所在主机列表
	 * @param applicationId
	 * @param name  
	 * @return
	 */
	public BsmResult hostList(Long applicationId, Map<String, String> labels);
}
