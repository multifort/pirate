package com.bocloud.paas.service.resource;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.LoadBalance;

public interface LoadBalanceService {

	/**
	 * 创建负载
	 * 
	 * @param params
	 * @param userId
	 * @return
	 */
	public BsmResult create(LoadBalance loadBalance, Long userId);

	/**
	 * 删除负载
	 * 
	 * @param params
	 * @param userId
	 * @return
	 */
	public BsmResult remove(List<Long> ids, Long userId);

	/**
	 * 查询负载
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple);

	/**
	 * 更改负载
	 * 
	 * @param params
	 * @param userId
	 * @return
	 */
	public BsmResult modify(LoadBalance loadBalance, Long userId);

	/**
	 * 查询负载详情
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public BsmResult detail(Long id);

	/**
	 * 根据负载获取应用
	 * 
	 * @date 2017年4月26日
	 *
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult listApps(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple,
			RequestUser requestUser);

	/**
	 * 查询是否存在该名称的负载
	 * 
	 * @param envName
	 * @param userId
	 * @return
	 */
	public BsmResult checkName(String envName, Long userId);

}
