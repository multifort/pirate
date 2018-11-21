package com.bocloud.paas.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.Layout;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.application.LayoutService;

@RestController
@RequestMapping("/layout")
public class LayoutController {
	@Autowired
	private LayoutService layoutService;

	/**
	 * 列表
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
	 *            简单查询标记，只有true和false,为false时返回用户的详细信息，为true时只返回id和name值。
	 * @param user
	 *            操作者信息
	 * @return 数据结果集
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name = "列表展示所有编排")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return layoutService.list(page, rows, paramList, sorterMap, simple, user);
	}

	/**
	 * 按照应用获取编排
	 * 
	 * @author zjm
	 * @date 2017年3月23日
	 *
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @return
	 */
	@RequestMapping(value = "/used", method = { RequestMethod.GET })
	@Log(name = "获取被使用的应用信息")
	public BsmResult listByAppId(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return layoutService.listUsed(page, rows, paramList, sorterMap, simple);
	}

	/**
	 * 编排文件创建
	 * 
	 * @param page
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "编排文件创建")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = jsonToObject(params);
		if (null != object) {
			Layout layout = objectToLayout(object);
			layout.setCreaterId(user.getId());
			layout.setMenderId(user.getId());
			return layoutService.create(layout, user);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 修改
	 * 
	 * @param params
	 *            应用属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param id
	 *            应用ID
	 * @param user
	 *            操作者信息O
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "编排文件信息修改")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = jsonToObject(params);
		if (null != object) {
			Layout layout = objectToLayout(object);
			return layoutService.modify(layout, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 移除
	 * 
	 * @param id
	 *            应用ID
	 * @param user
	 *            操作者信息
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/remove", method = { RequestMethod.DELETE })
	@Log(name = "编排文件移除")
	public BsmResult remove(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return layoutService.remove(id, user.getId());
	}

	/**
	 * 详细信息
	 * 
	 * @param id
	 *            任务ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	@Log(name = "编排文件详情")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return layoutService.detail(id);
	}

	@RequestMapping(value = "/{id}/getVariablesById", method = { RequestMethod.GET })
	@Log(name = "获取编排模版动态属性")
	public BsmResult getVariablesById(@RequestParam(value = Common.PARAMS, required = false) String params,
			@PathVariable(value = Common.ID) Long id) {
		return layoutService.getVariablesById(id);
	}

	/**
	 * json字符串转换为json对象
	 * 
	 * @param json
	 * @return
	 */
	private final JSONObject jsonToObject(String json) {
		return JSONTools.isJSONObj(json);
	}

	/**
	 * json对象转换为仓库对象
	 * 
	 * @param obj
	 * @return
	 */
	private final Layout objectToLayout(JSONObject obj) {
		return JSONObject.parseObject(obj.toJSONString(), Layout.class);
	}
}
