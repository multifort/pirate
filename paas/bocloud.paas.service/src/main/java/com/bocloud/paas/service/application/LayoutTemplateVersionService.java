package com.bocloud.paas.service.application;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.LayoutTemplateVersion;

import java.util.List;
import java.util.Map;

public interface LayoutTemplateVersionService {

	/**
	 * 编排模板版本创建
	 * 
	 * @param layoutTemplateVersion
	 * @param user
	 * @return
	 */
	public BsmResult create(LayoutTemplateVersion layoutTemplateVersion, RequestUser user);

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
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple);

	/**
	 * 更新
	 * 
	 * @param layoutTemplateVersion
	 *            基本信息
	 * @param userId
	 *            操作用户ID
	 * @return 操作结果
	 */
	public BsmResult modify(LayoutTemplateVersion layoutTemplateVersion, Long userId);

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
	 * 参数详情
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult getVarsById(Long id);
	
	/**
	 * 模板内容
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult getTemplate(Long id);
	
	/**
	 * 模板版本升级
	 * 
	 * @param id
	 * @return
	 */
	public BsmResult upgrade(Long id, Long userId);

	/**
	 * 模板实例化
	 *
	 * @param id
	 * @return
	 */
	public BsmResult instantiation(Long id, String params, Long userId);

	/*//应用实例模板化
	public BsmResult templatable(Long id, Long userId);*/

}
