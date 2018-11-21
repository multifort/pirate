package com.bocloud.paas.service.application;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.ConfigManage;

/**
 * describe:配置管理业务逻辑层接口
 * @author Zaney
 * @data 2017年10月17日
 */
public interface ConfigManageService {
	/**
	 * 创建configMap
	 * @param envId
	 * @param applicationId
	 * @param name  名称
	 * @param dataMap  手动设置的key、value值
	 * @param fileDir  上传的文件目录路径
	 * @return
	 */
	public BsmResult create(ConfigManage configManage, Map<String, String> dataMap, RequestUser user);
	/**
	 * 获取详情 
	 * @param id 配置实例ID
	 * @return
	 */
	public BsmResult detail(Long id);
	/**
	 * 获取列表
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param user
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, 
			 RequestUser user);
	/**
	 * 删除
	 * @param id
	 * @param nameMap
	 * @return
	 */
	public BsmResult remove(List<Long> ids);
	/**
	 * 修改配置实例
	 * @param id
	 * @param dataMap
	 * @param user
	 * @return
	 */
	public BsmResult modify(Long id, String remark, Map<String, String> dataMap, RequestUser user);
}
