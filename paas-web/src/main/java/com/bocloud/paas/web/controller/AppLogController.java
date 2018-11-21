package com.bocloud.paas.web.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bocloud.paas.web.controller.BasicController;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
/**
 * @author Zaney
 * @data  2017年5月5日
 * describe: 容器平台历史记录
 */
@RestController
@RequestMapping("/app/log")
public class AppLogController {
	private final String BASE_SERVICE = "/app/log";
	
	@Autowired
	private BasicController basicController;
	/**
	 * 列表查询
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult auditLog(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				AppLogController.class.getSimpleName());
	}
}
