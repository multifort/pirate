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
import com.bocloud.paas.entity.Repository;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.repository.RepositoryService;

@RestController
@RequestMapping("/registry")
public class RepositoryController {

	@Autowired
	private RepositoryService repositoryService;

	/**
	 * 创建仓库
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "创建仓库")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(value = Common.REQ_USER) RequestUser user) {
		JSONObject obj = JSONTools.isJSONObj(params);
		if (null != obj) {
			Repository registry = JSONObject.parseObject(obj.toJSONString(), Repository.class);
			return repositoryService.create(user, registry);
		}
		return ResultTools.formatErrResult();
	}

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
	@Log(name = "仓库列表展示")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return repositoryService.list(page, rows, paramList, sorterMap, simple, user);
	}
	
	@RequestMapping(value = "/list", method = { RequestMethod.GET })
	@Log(name = "获取流程管控模块推送镜像获取仓库地址信息")
	public BsmResult listAddress(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return repositoryService.listAddress(paramList, sorterMap, user);
	}
	
	/**
	 * 获取仓库详情
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	@Log(name = "仓库详情信息")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return repositoryService.detail(id, user.getId());
	}

	/**
	 * 删除仓库
	 * 
	 * @param id
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/remove", method = { RequestMethod.DELETE })
	@Log(name = "删除仓库")
	public BsmResult remove(@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		return repositoryService.remove(id, user.getId());
	}

	/**
	 * 修改仓库信息
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "修改仓库信息")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@PathVariable(value = Common.ID) Long id, @Value(Common.REQ_USER) RequestUser user) {
		JSONObject obj = JSONTools.isJSONObj(params);
		if (null != obj) {
			Repository registry = JSONObject.parseObject(obj.toJSONString(), Repository.class);
			return repositoryService.modify(registry, user.getId());
		}
		return ResultTools.formatErrResult();
	}

	/**
	 * 统计
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/count", method = { RequestMethod.GET })
	@Log(name = "统计仓库数量")
	public BsmResult count(@Value(Common.REQ_USER) RequestUser user) {
		return repositoryService.countInfo(user.getId());
	}

	/**
	 * 获取仓库的镜像信息
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/images", method = { RequestMethod.POST })
	@Log(name = "查询仓库镜像")
	public BsmResult images(@RequestParam(value = "page", required = true) Integer page,
			@RequestParam(value = "rows", required = true) Integer rows,
			@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(value = Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		if (!paramList.isEmpty()) {
			return repositoryService.getImagesInRegistry(page, rows, paramList, user.getId());
		}
		return ResultTools.formatErrResult();
	}

	/**
	 * 仓库镜像同步
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/sycn/image", method = { RequestMethod.GET })
	@Log(name = "仓库镜像同步")
	public BsmResult sycnImage(@RequestParam(value = Common.PARAMS, required = false) String params) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Long id = Long.valueOf(object.getString("id"));
			return repositoryService.sycn(id);
		} else {
			return ResultTools.formatErrResult();
		}
	}


}
