package com.bocloud.paas.service.application;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.LayoutTemplate;

import java.util.List;
import java.util.Map;

public interface LayoutTemplateService {

	/**
	 * 编排模板创建
	 * 
	 * @param layoutTemplate
	 * @param user
	 * @return
	 */
	public BsmResult create(LayoutTemplate layoutTemplate, RequestUser user);

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
	public BsmResult list(int page, int rows, List<Param> params,
                          Map<String, String> sorter, Boolean simple);

	/**
	 * 更新
	 * 
	 * @param layoutTemplate
	 *            基本信息
	 * @param userId
	 *            操作用户ID
	 * @return 操作结果
	 */
	public BsmResult modify(LayoutTemplate layoutTemplate, Long userId);

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
	
	/**
	 * 无分页列表查询
	 * 
	 * @return
	 */
	public BsmResult getList();

}
