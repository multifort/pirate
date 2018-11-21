package com.bocloud.paas.service.environment;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.paas.entity.Environment;
import com.bocloud.paas.entity.Host;
import com.bocloud.paas.model.Cluster;

public interface EnvironmentService {

	/**
	 * 创建环境
	 * 
	 * @param userId
	 * @return
	 */
	public BsmResult create(Environment environment, Long userId);

	/**
	 * 删除环境
	 * 
	 * @param userId
	 * @return
	 */
	public BsmResult remove(List<Long> ids, Long userId);

	/**
	 * 查询环境
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, Long userId);

	/**
	 * 更改环境
	 * 
	 * @param userId
	 * @return
	 */
	public BsmResult modify(Environment environment, Long userId);

	/**
	 * 查询环境详情
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public BsmResult detail(Long id);

	/**
	 * 环境操作
	 * 
	 * @param environment
	 * @param userId
	 * @return
	 */
	public BsmResult operate(Environment environment, Long userId);

	/**
	 * 环境接管集群
	 * 
	 * @param host
	 * @param userId
	 * @return
	 */
	public BsmResult receiveCluster(Host host, Long userId);

	/**
	 * 添加节点
	 * 
	 * @param host
	 * @param userId
	 * @return
	 */
	public BsmResult addNode(Host host, Long userId);

	/**
	 * 删除节点
	 * 
	 * @param host
	 * @param userId
	 * @return
	 */
	public BsmResult deleteNode(Host host, Long userId);

	/**
	 * 查询状态正常的环境
	 * 
	 * @param userId
	 * @return
	 */
	public BsmResult queryNormalEnv(Long userId);

	/**
	 * 在裸机上部署kubernetes集群
	 * 
	 * @param userId
	 * @return
	 */
	public BsmResult createKubernetesCluser(Cluster cluster, Long userId);

	/**
	 * 获取环境下的命名空间
	 * 
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult nameSpace(Long id, Long userId);

	/**
	 * 获取环境下监控的url
	 * 
	 * @param userId
	 * @return
	 */
	public BsmResult monitorUrl(Long id, Long userId);

	/**
	 * 环境状态监控
	 */
	public void envMonitor();
	/**
	 * 环境拓扑
	 * @param envId
	 * @return
	 */
	public BsmResult envTopology(Long envId);
}
