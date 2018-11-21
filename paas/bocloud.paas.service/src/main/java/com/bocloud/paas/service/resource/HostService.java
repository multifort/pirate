package com.bocloud.paas.service.resource;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.paas.entity.Host;

public interface HostService {

	/**
	 * 
	 * 向集群中添加主机
	 * 
	 * @param host
	 * @param userId
	 * @return
	 */
	public BsmResult addHost(Host host, Long userId);

	/**
	 * 删除集群中的主机
	 * 
	 * @param ids
	 * @param userId
	 * @return
	 */
	public BsmResult removeHost(List<Long> ids, Long userId);

	/**
	 * 数据库中添加主机
	 * 
	 * @param host
	 * @param userId
	 * @return
	 */
	public BsmResult create(Host host, Long userId);

	/**
	 * 删除主机
	 * 
	 * @param ids
	 * @param userId
	 * @return
	 */
	public BsmResult remove(List<Long> ids, Long userId);

	/**
	 * 查询主机
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
	 * 更改主机
	 * 
	 * @param host
	 * @param userId
	 * @return
	 */
	public BsmResult modify(Host host, Long userId);

	/**
	 * 查询主机详情
	 * 
	 * @param hostId
	 * @return
	 */
	public BsmResult detail(Long hostId);

	/**
	 * 查询状态正常且不在环境中的主机
	 * 
	 * @param host
	 * @param userId
	 * @return
	 */
	public BsmResult queryNormalHost(Host host, Long userId);

	/**
	 * 环境中节点调度
	 * 
	 * @param hostId
	 * @param userId
	 * @return
	 */
	public BsmResult scheduleNode(Long hostId, Long userId);

	/**
	 * 查询处于某一环境中的主机
	 * 
	 * @param envId
	 * @param userId
	 * @return
	 */
	public BsmResult queryHostInEnv(Long envId, Long userId);

	/**
	 * 查询不在环境中的主机
	 * 
	 * @return
	 */
	public BsmResult queryHostNotInEnv(Long envId, Long userId);

	/**
	 * 集群中主机监控
	 */
	public void monitor();
	/**
	 * 主机拓扑图
	 * @param hostId
	 * @return
	 */
	public BsmResult hostTopology(Long hostId);
	
	/**
	 * 集群中主机监控
	 */
	public BsmResult monitorGpu(Long id, String num, String timeUnit);
}
