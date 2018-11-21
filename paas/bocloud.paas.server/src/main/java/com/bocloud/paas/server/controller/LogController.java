package com.bocloud.paas.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.log.service.LogService;
import com.bocloud.paas.server.interceptor.Log;

@RestController
@RequestMapping("/log")
public class LogController {
	
	@Autowired
	private LogService logService;

	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name = "获取日志信息")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) int page,
			@RequestParam(value = Common.ROWS, required = false) int rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		BsmResult result = logService.list(page, rows, paramList, sorterMap, simple, null);
		return result;
	}
	
}
