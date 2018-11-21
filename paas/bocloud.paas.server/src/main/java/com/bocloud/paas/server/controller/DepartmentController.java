package com.bocloud.paas.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.Department;
import com.bocloud.paas.model.DepartmentBean;
import com.bocloud.paas.service.user.DepartmentService;

@RestController
@RequestMapping("/department")
public class DepartmentController {

	@Autowired
	private DepartmentService departmentService;

	/**
	 * 查询组织机构
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.PARENTID, required = true) Long parentId,
			@Value(Common.REQ_USER) RequestUser user) {
		if (null != parentId) {
			return departmentService.list(parentId, user.getTenantId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 创建组织机构
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Department depart = JSONObject.parseObject(object.toJSONString(), Department.class);
			depart.setCreaterId(user.getId());
			depart.setTenantId(user.getTenantId());
			return departmentService.create(depart);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 修改组织机构
	 * 
	 * @param params
	 * @param id
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			DepartmentBean depart = JSONObject.parseObject(object.toJSONString(), DepartmentBean.class);
			return departmentService.modify(depart, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除组织机构
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/remove", method = { RequestMethod.DELETE })
	public BsmResult remove(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return departmentService.remove(id, user.getId());
	}

	/**
	 * 查询组织机构详情
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	public BsmResult detail(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return departmentService.detail(id);
	}
	
	/**
	 * 校验组织机构名称唯一性
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "/checkName", method = { RequestMethod.POST })
	public BsmResult checkName(@RequestParam(value = Common.NAME, required = true) String name) {
		return departmentService.exists(name, null);
	}
	

}
