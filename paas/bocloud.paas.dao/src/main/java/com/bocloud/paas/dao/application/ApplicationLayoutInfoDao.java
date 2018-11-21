package com.bocloud.paas.dao.application;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.Param;
import com.bocloud.paas.entity.Application;
import com.bocloud.paas.entity.ApplicationLayoutInfo;

/**
 * 
 * @author zjm
 * @date 2017年3月18日
 */
public interface ApplicationLayoutInfoDao {

	/**
	 * 保存
	 * 
	 * @param applicationLayoutInfo
	 * @return
	 * @throws Exception
	 */
	public boolean insert(ApplicationLayoutInfo applicationLayoutInfo) throws Exception;

	/**
	 * 删除
	 * 
	 * @param applicationLayoutInfo
	 * @return
	 * @throws Exception
	 */
	public boolean delete(ApplicationLayoutInfo applicationLayoutInfo) throws Exception;

	/**
	 * 根据应用和编排文件获取应用编排文件
	 * 
	 * @author zjm
	 * @date 2017年4月9日
	 *
	 * @param appId
	 * @param layoutId
	 * @return
	 * @throws Exception
	 */
	public ApplicationLayoutInfo query(Long appId, Long layoutId) throws Exception;
	/**
	 * 查找应用和编排文件关系表
	 * @param appId
	 * @return
	 * @throws Exception
	 */
	public ApplicationLayoutInfo query(Long appId) throws Exception;
	/**
	 * 获取数量
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int count(List<Param> params) throws Exception;
	/**
	 * 列表查询
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @return
	 * @throws Exception
	 */
	public List<Application> list(int page, int rows, List<Param> params, Map<String, String> sorter) throws Exception;
}
