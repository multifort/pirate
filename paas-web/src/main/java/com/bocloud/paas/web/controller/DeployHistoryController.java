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
 * 
 * @author zjm
 * @date 2017年4月7日
 */
@RestController
@RequestMapping("/deployHistory")
public class DeployHistoryController {
	private final String BASE_SERVICE = "/deployHistory";
	@Autowired
	private BasicController basicController;

	/**
	 * 列表展示
	 * @author zjm
	 * @date 2017年4月7日
	 *
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROW, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				DeployHistoryController.class.getSimpleName());
	}
}
