package com.bocloud.paas.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.statistic.StatisticService;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

	@Autowired
	private StatisticService statisticService;

	@RequestMapping(value = "/statistic", method = { RequestMethod.GET })
	@Log(name = "首页统计信息")
	public BsmResult statistic(@RequestParam(value = Common.PARAMS, required = false) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		return statisticService.statisticTotal(user);
	}
	
	/**
	 * 应用cpu/memory使用量排行
	 * @return
	 */
	@RequestMapping(value = "/application", method = { RequestMethod.GET })
	@Log(name = "首页统计信息")
	public BsmResult getAppResource(@Value(Common.REQ_USER) RequestUser user){
		return statisticService.getAppResource(user);
	}
}
