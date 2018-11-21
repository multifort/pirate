package com.bocloud.paas.service.application;

import java.util.List;
import java.util.Map;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;

/**
 * 应用部署历史service接口
 * 
 * @author zjm
 * @date 2017年3月17日
 * @describe
 */
public interface DeployHistoryService {

	/**
	 * 列表查询
	 * @author zjm
	 * @date 2017年4月7日
	 *
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple);

}
