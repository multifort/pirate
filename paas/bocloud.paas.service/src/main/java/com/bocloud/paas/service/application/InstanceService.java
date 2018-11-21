package com.bocloud.paas.service.application;

import com.bocloud.common.model.BsmResult;

public interface InstanceService {

	/**
	 * 获取pod状态
	 * 
	 * @param name
	 * @return
	 */
	public BsmResult getPodStatus(Long appId, String namespace, String name);
	
	/**
	 * 获取Pod的资源
	 * 
	 * @param appId
	 * @param namespace
	 * @param resourceName
	 * @param resourceType
	 * @return
	 */
	public BsmResult getPodResource(Long appId, String namespace, String resourceName, String resourceType);

	/**
	 * 获取日志信息
	 * 
	 * @param namespace
	 * @param resourceName
	 * @return
	 */
	public BsmResult getLog(String namespace, String resourceName, String containerName, String status, Long envId);
}
