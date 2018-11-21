package com.bocloud.paas.service.statistic;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;

public interface StatisticService {

	public BsmResult statisticTotal(RequestUser requestUser);
	/**
	 * 获取应用cpu/memory使用量
	 * @param page
	 * @param rows
	 * @return
	 */
	public BsmResult getAppResource(RequestUser user);
	
}
