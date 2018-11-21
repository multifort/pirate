package com.bocloud.paas.service.application;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.ApplicationStore;

public interface ApplicationStoreService {
	
	/**
	 * @return
	 */
	public BsmResult list(String name);
	
	/**
	 * 根据模板文件，部署应用商店的内容
	 * @param envId
	 * @param fileName
	 * @return
	 */
	public BsmResult deploy(Long envId, Long applicationId, Long storeId, JSONObject paramJson, 
			String deployType, RequestUser user);
	
	//获取详细信息
	public BsmResult detail(String name);
	
	/**
	 * 获取应用商店参数模板的内容
	 * @param storeId
	 * @return
	 */
	public BsmResult content(Long storeId, String deployType);
	/**
	 * 商品模板上传
	 * @param applicationStore
	 * @return
	 */
	public BsmResult upload(ApplicationStore applicationStore);
	/**
	 * 版本升级
	 * @param applicationStoreId
	 * @param image
	 * @return
	 */
	public BsmResult versionUpgrade(Long applicationStoreId, Long imageId, String deployType);
	/**
	 * 获取模板中的镜像名称
	 * @param applicationStoreId
	 * @return
	 */
	public BsmResult getTemplateImage(Long applicationStoreId, String deployType);
	/**
	 * 商品下架
	 * @param applicationStoreId
	 * @return
	 */
	public BsmResult down(Long applicationStoreId);
	/**
	 * 读取模板
	 * @param applicationStoreId
	 * @return
	 */
	public BsmResult readTemplate(Long applicationStoreId, String deployType);
	/**
	 * 根据镜像获取基础模板
	 * @param imageId
	 * @param jsonObject
	 * @return
	 */
	public BsmResult makeTemplate(Long imageId, JSONObject jsonObject);
	/**
	 * 获取一定条件下的zk服务
	 * @param envId
	 * @param appId
	 * @return
	 */
	public BsmResult getZookeeper(Long envId, Long applicationId, String type, RequestUser user);
	/**
	 * 从镜像模板里获取镜像端口号
	 * @param storeId
	 * @return
	 */
	public BsmResult getImagePort(Long storeId, String deployType);
	
}
