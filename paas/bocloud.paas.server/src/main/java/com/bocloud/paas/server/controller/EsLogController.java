package com.bocloud.paas.server.controller;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.paas.service.system.EsLogService;

@RestController
@RequestMapping("/eslog")
public class EsLogController {
	
	private static Logger logger = LoggerFactory.getLogger(EsLogController.class);

	@Autowired
	private EsLogService esLogService;

	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(
			@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@Value(Common.REQ_USER) RequestUser user) {

		JSONObject jsonObject = JSONTools.isJSONObj(params);

		String startTime = null;
		String endTime = null;
		String index = null;
		String type = null;
		String order = null;
		String port = null;
		if (null != jsonObject) {
			if (null != jsonObject.get("startTime")) {
				startTime = jsonObject.get("startTime").toString();
			}
			if (null != jsonObject.get("endTime")) {
				endTime = jsonObject.get("endTime").toString();
			}
			if (null != jsonObject.get("index")) {
				index = jsonObject.get("index").toString();
			}
			if (null != jsonObject.get("type")) {
				type = jsonObject.get("type").toString();
			}
			if (null != jsonObject.get("order")) {
				order = jsonObject.get("order").toString();
			}
			if (null != jsonObject.get("port")) {
				port = jsonObject.get("port").toString();
			}
		}

		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (null != startTime && null != endTime) {
			queryBuilder.must(QueryBuilders.rangeQuery("@timestamp")
					.from(startTime).to(endTime).includeLower(true)
					.includeUpper(true));
		}

		if (null != port) {
			queryBuilder.must(QueryBuilders.termQuery("port", port));
		}

		BsmResult result = null;
		try {
			result = esLogService.list(queryBuilder, index, type, sorter,
					order, page, rows);
		} catch (Exception e) {
			logger.error("查询失败",e);
		}
		return result;
	}
	/**
	 * 获取server端系统日志
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/system", method = { RequestMethod.POST })
	public BsmResult systemLog(@RequestParam(value = Common.PARAMS, required = false) String params){
		return esLogService.getSystemLog();
	}

}
