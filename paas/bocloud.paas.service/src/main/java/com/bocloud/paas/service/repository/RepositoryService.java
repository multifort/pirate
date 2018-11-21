package com.bocloud.paas.service.repository;

import java.util.List;
import java.util.Map;
import com.bocloud.paas.entity.Repository;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;

/**
 * 仓库实现类接口
 * @author Zaney
 *
 */
public interface RepositoryService {
	
	/**
	 * 列表查询
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, RequestUser requestUser);
	
	/**
	 * 获取带有http
	 * @param params
	 * @param sorter
	 * @param requestUser
	 * @return
	 */
	public BsmResult listAddress(List<Param> params, Map<String, String> sorter, RequestUser requestUser);

	/**
	 * 添加仓库信息
	 * @param registry
	 * @return
	 */
	public BsmResult create(RequestUser user , Repository registry);
	
	/**
	 * 获取仓库详情
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id, Long userId);
	
	/**
	 * 删除仓库信息
	 * @param id
	 * @param userId
	 * @return
	 */
	public BsmResult remove(Long id, Long userId);
	
	/**
	 * 修改仓库信息
	 * @param registry
	 * @param userId
	 * @return
	 */
	public BsmResult modify(Repository registry, Long userId);
	
	/**
	 * 统计
	 * @param userId
	 * @param userId
	 * @return
	 */
	public BsmResult countInfo(Long userId);
	
	/**
	 * 获取仓库中的镜像
	 * @param params
	 * @param userId
	 * @return
	 */
	public BsmResult getImagesInRegistry(int page, int rows, List<Param> params, Long userId);
	/**
	 * 查询所有未删除的仓库
	 * @return
	 */
	public List<Repository> listRepository();
	/**
	 * 修改仓库某些字段信息
	 * @param fields
	 */
	public void updateWithField(Repository registry, String[] fields);

	/**
	 * 仓库镜像同步
	 * @param id
	 * @return
	 */
	public BsmResult sycn(Long id);
}
