package com.bocloud.paas.service.application;

import java.util.List;
import java.util.Map;
import com.bocloud.paas.entity.Layout;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;

public interface LayoutService {

	/**
	 * 编排文件创建
	 * 
	 * @param name
	 * @param remark
	 * @param mess
	 * @return
	 */
	public BsmResult create(Layout layout, RequestUser user);

	/**
	 * 列表
	 * 
	 * @param page
	 *            当前页
	 * @param rows
	 *            页码
	 * @param params
	 *            查询参数
	 * @param sorter
	 *            拍寻参数
	 * @return 满足条件的列表
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, 
			RequestUser requestUser);

	/**
	 * 获取编排文件被哪些应用使用
	 * 
	 * @author zjm
	 * @date 2017年3月23日
	 *
	 * @param appId
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult listUsed(int page, int rows, List<Param> params, Map<String, String> sorter,
			Boolean simple);

	/**
	 * 更新
	 * 
	 * @param bean
	 *            基本信息
	 * @param userId
	 *            操作用户ID
	 * @return 操作结果
	 */
	public BsmResult modify(Layout layout, Long userId);

	/**
	 * 移除
	 * 
	 * @param id
	 *            ID
	 * @param userId
	 *            操作者ID
	 * @return 操作结果
	 */
	public BsmResult remove(Long id, Long userId);

	/**
	 * 详情
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult detail(Long id);
	
	public BsmResult getVariablesById(Long id);

}
