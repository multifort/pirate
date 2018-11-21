package com.bocloud.paas.service.resource;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.paas.entity.Volume;

public interface PersistentVolumeService {

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
	 * 创建PersistentVolume
	 * 
	 * @param pv
	 * @param userId
	 * @return
	 */
	public BsmResult create(Volume pv, Long userId);

	/**
	 * 获取存储模板
	 * 
	 * @param pv
	 * @param userId
	 * @return
	 */
	public BsmResult queryPVTemplate(Volume pv, Long userId);

	/**
	 * 删除存储，支持批量删除
	 * 
	 * @param ids
	 * @param userId
	 * @return
	 */
	public BsmResult remove(List<Long> ids, Long userId);

	/**
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);

	/**
	 * 编辑存储卷
	 * 
	 * @param pv
	 * @param userId
	 * @return
	 */
	public BsmResult modify(Volume pv, Long userId);

	/**
	 * 获取存储卷Json数据
	 * 
	 * @param pv
	 * @param userId
	 * @return
	 */
	public BsmResult queryPVData(Volume pv, Long userId);
	
	/**
	 * 环境中pv监控
	 */
	public void pvMonitor();
}
