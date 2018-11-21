package com.bocloud.paas.service.application;

import java.util.List;
import java.util.Map;

import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.Monitor;

import io.fabric8.kubernetes.api.model.Quantity;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;

/**
 * 应用service接口
 * 
 * @author zjm
 * @date 2017年3月17日
 * @describe
 */
public interface ApplicationService {

	/**
	 * 列表查询
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser user);

	/**
	 * 添加应用信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param user
	 * @param application
	 * @return
	 */
	public BsmResult create(RequestUser user, Application application);

	/**
	 * 获取应用详情
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);


	/**
	 * @param ids
	 * @return 删除应用
	 */
	public BsmResult remove(List<Long> ids);

	/**
	 * 修改应用信息
	 * 
	 * @author zjm
	 * @date 2017年3月17日
	 * 
	 * @param application
	 * @param userId
	 * @return
	 */
	public BsmResult modify(Application applicationBean, Long userId);
	/**
	 * 获取监控信息
	 * @param monitor
	 * @return
	 */
	public BsmResult monitor(Monitor monitor);
	/**
	 * 创建ingress
	 * @param applicationId
	 * @param serviceName
	 * @param servicePort
	 * @return
	 */
	public BsmResult createIngress(Long applicationId, String serviceName, int servicePort);
	/**
	 * 获取已暴露服务的服务列表
	 * @param applicationId
	 * @return
	 */
	public BsmResult getIngress(Long applicationId);
	/**
	 * 获取服务下的port
	 * @param applicationId
	 * @param serviceName
	 * @return
	 */
	public BsmResult getServicePort(Long applicationId, String type, String serviceName);
	/**
	 * 获取应用资源信息
	 * @param application
	 * @return
	 */
	public BsmResult getResource(Long applicationId);
	/**
	 * 应用拓扑
	 * @param applicationId
	 * @return
	 */
	public BsmResult applicationTopology(Long applicationId);
	/**
	 * 删除该应用下某个暴露的服务
	 * @param applicationId
	 * @param svcName 服务名称
	 * @return
	 */
	public BsmResult deleteServiceIngress(Long applicationId, String svcName);
	
	//应用实例模板化
	public BsmResult templatable(Long id,String namespace,String name,Long userId);
	
	//======================= 资源配额 ==============================//
	public BsmResult createResourceQuota(Long applicationId, JSONObject object, JSONObject limitRangeObject);
	public BsmResult detailResourceQuota(Long applicationId);
	public BsmResult modifyResourceQuota(Long applicationId, Map<String, Quantity> hardMap, JSONObject limitRangeObject);
	public BsmResult getUsedResourceQuota(Long applicationId);
	public BsmResult deleteResourceQuota(Long applicationId);
	
	
}
