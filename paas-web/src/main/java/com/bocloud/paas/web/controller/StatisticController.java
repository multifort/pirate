package com.bocloud.paas.web.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

@RestController
@RequestMapping(value = "/statistic")
public class StatisticController {
	private final String BASE_SERVICE = "/statistic";
	@Autowired
	private ServiceFactory serviceFactory;
	private static final BoCloudService SERVICE = BoCloudService.Cmp;

	@RequestMapping(value = "/statistic", method = { RequestMethod.GET})
	public BsmResult statisticTotal(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		String url = BASE_SERVICE + "/statistic";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null, request);
		return service.invoke();
	}
	/**
	 * 应用cpu/memory使用量排行
	 * @return
	 */
	@RequestMapping(value = "/application", method = { RequestMethod.GET })
	public BsmResult getAppResource(HttpServletRequest request){
		String url = BASE_SERVICE + "/application";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null, request);
		return service.invoke();
	}
}
