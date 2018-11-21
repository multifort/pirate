package com.bocloud.paas.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.LayoutTemplateVersion;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.application.LayoutTemplateVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/layout/template/version")
public class LayoutTemplateVersionController {
	@Autowired
	private LayoutTemplateVersionService layoutTemplateVersionService;

	/**
	 * 编排模板版本创建
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "编排模板版本创建")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = jsonToObject(params);
		if (null != object) {
			LayoutTemplateVersion layoutTemplateVersion = objectToLayoutTemplateVersion(object);
			layoutTemplateVersion.setCreaterId(user.getId());
			layoutTemplateVersion.setMenderId(user.getId());
			return layoutTemplateVersionService.create(layoutTemplateVersion, user);
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	
	/**
	 * 列表
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name = "列表展示所有编排模板版本")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return layoutTemplateVersionService.list(page, rows, paramList, sorterMap, simple);
	}
	

	/**
	 * 修改
	 * 
	 * @param params
	 *            应用属性信息的json字符串，在数据转换时会发生异常，对外抛出400异常【Bad Request】
	 * @param id
	 *            应用ID
	 * @param user
	 *            操作者信息
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "编排模板版本信息修改")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = jsonToObject(params);
		if (null != object) {
			LayoutTemplateVersion layoutTemplateVersion = objectToLayoutTemplateVersion(object);
			return layoutTemplateVersionService.modify(layoutTemplateVersion, user.getId());
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
	@Log(name = "编排模板版本删除")
	public BsmResult remove(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return layoutTemplateVersionService.remove(id, user.getId());
	}

	/**
	 * 详细信息
	 * 
	 * @param id
	 *            任务ID
	 * @return 操作结果
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	@Log(name = "编排模板版本详情")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return layoutTemplateVersionService.detail(id);
	}

	// 获取编排模板版本的参数
	@RequestMapping(value = "/getVarsById", method = { RequestMethod.GET })
	@Log(name = "获取编排模版版本参数")
	public BsmResult getVariablesById(@RequestParam(value = Common.PARAMS, required = false) String params) {
		 JSONObject jsonObject = JSONTools.isJSONObj(params);
		 Long id = Long.valueOf(jsonObject.get("id").toString());
		return layoutTemplateVersionService.getVarsById(id);
	}
	
	// 获取编排模板版本的内容
	@RequestMapping(value = "/getTemplate", method = { RequestMethod.GET })
	@Log(name = "获取编排模版版本内容")
	public BsmResult getTemplate(@RequestParam(value = Common.PARAMS, required = false) String params) {
		 JSONObject jsonObject = JSONTools.isJSONObj(params);
		 Long id = Long.valueOf(jsonObject.get("id").toString());
		return layoutTemplateVersionService.getTemplate(id);
	}
		
	// 升级
	@RequestMapping(value = "/upgrade", method = { RequestMethod.POST })
	@Log(name = "编排模版版本升级")
	public BsmResult upgrade(@RequestParam(value = Common.PARAMS, required = false) String params, @Value(Common.REQ_USER) RequestUser user) {
		 JSONObject jsonObject = JSONTools.isJSONObj(params);
		 Long id = Long.valueOf(jsonObject.get("id").toString());
		return layoutTemplateVersionService.upgrade(id,user.getId());
	}
	
	// 实例化
	@RequestMapping(value = "/instantiation", method = { RequestMethod.POST })
	@Log(name = "编排模版版本实例化")
	public BsmResult instantiation(@RequestParam(value = Common.PARAMS, required = false) String params,@RequestParam(value = Common.ID, required = true) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return layoutTemplateVersionService.instantiation(id,params,user.getId());
	}
	
	/*// 应用实例模板化，暂时放在这儿，以后移动到应用实例Controller
	@RequestMapping(value = "/templatable", method = { RequestMethod.POST })
	@Log(name = "应用实例转换成模板")
	public BsmResult templatable(@RequestParam(value = Common.PARAMS, required = false) String params, @Value(Common.REQ_USER) RequestUser user) {
		 JSONObject jsonObject = JSONTools.isJSONObj(params);
		 Long id = null;
		 if(null != jsonObject){
			 id = Long.valueOf(jsonObject.get("id").toString());
		 }
		return layoutTemplateVersionService.templatable(id,user.getId());
	}*/
		
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
	private final LayoutTemplateVersion objectToLayoutTemplateVersion(JSONObject obj) {
		return JSONObject.parseObject(obj.toJSONString(), LayoutTemplateVersion.class);
	}
}
