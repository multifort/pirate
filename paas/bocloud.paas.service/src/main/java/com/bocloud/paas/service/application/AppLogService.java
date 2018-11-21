package com.bocloud.paas.service.application;

import java.util.List;
import java.util.Map;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;

/**
 * @author Zaney
 * @data  2017年5月4日
 * describe: 历史记录业务层接口
 */

public interface AppLogService {
	/**
	 * 历史记录列表展示
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param userId
	 * @return
	 */
	public BsmResult list(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, Long userId); 
}
