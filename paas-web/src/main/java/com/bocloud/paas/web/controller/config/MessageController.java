package com.bocloud.paas.web.controller.config;

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
 * 机房相关接口
 * 
 * @author dmw
 *
 */
@RestController
@RequestMapping("/message")
public class MessageController {
	private static final String BASE_SERVICE = "/message";
	@Autowired
	private BasicController basicController;

	/**
	 * 获取列表
	 * 
	 * @param page
	 *            当前页码
	 * @param rows
	 *            页面数据大小
	 * @param params
	 *            查询参数，例如：[{"param":{"name":"aaa","password":"1245"},"sign":
	 *            "EQ|UEQ"},{"param":{"name":"aaa","password":"1245"},"sign":
	 *            "EQ|UEQ"}]
	 * @param sorter
	 *            排序参数，例如：{"name":0|1,"password":0|1},0表示增序，1表示降序
	 * @param simple
	 *            简单查询标记，只有true和false,为false时返回机房的详细信息，为true时只返回id和name值。
	 * @return 数据结果集
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request) {
		return basicController.list(page, rows, params, sorter, simple, BASE_SERVICE, request,
				MessageController.class.getSimpleName());
	}

	/**
	 * 移除
	 * 
	 * @param params
	 *            机房属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @return 操作结果
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.remove(params, BASE_SERVICE, request, MessageController.class.getSimpleName());
	}

	/**
	 * 查看详细信息
	 * 
	 * @param params
	 *            机房属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @return 操作结果
	 */
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request) {
		return basicController.detail(params, BASE_SERVICE, request, MessageController.class.getSimpleName());
	}

}
