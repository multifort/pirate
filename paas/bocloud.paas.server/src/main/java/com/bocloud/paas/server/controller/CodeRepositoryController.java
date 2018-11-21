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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.paas.entity.CodeRepository;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.process.CodeRepositoryService;

/**
 * describe: 代码仓库server层控制层
 * @author Zaney
 * @data 2017年10月27日
 */
@RestController
@RequestMapping("/code/repository")
public class CodeRepositoryController {
	@Autowired
	private CodeRepositoryService codeRepositoryService;
	
	@RequestMapping(value = "/code", method = { RequestMethod.POST })
	@Log(name="关联代码仓库")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user){
		CodeRepository codeRepository = JSONObject.parseObject(
				params, CodeRepository.class);
		return codeRepositoryService.create(codeRepository, user);
	}
	
	@RequestMapping(value = "/code", method = {RequestMethod.GET})
	@Log(name = "代码仓库列表查询")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user){
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		return codeRepositoryService.list(page, rows, paramList, sorterMap, simple, user);
	}
	
	@RequestMapping(value = "/{id}/code", method = {RequestMethod.GET})
	@Log(name = "获取代码仓库详情")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id){
		return codeRepositoryService.detail(id);
	}
	
	@RequestMapping(value = "/code", method = {RequestMethod.DELETE})
	@Log(name = "删除代码仓库对象信息")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Long> ids = JSON.parseArray(jsonObject.get("ids").toString(), Long.class);
		return codeRepositoryService.remove(ids);
	}
	
	@RequestMapping(value = "/update/code", method = {RequestMethod.POST})
	@Log(name = "修改代码仓库对象信息")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user){
		CodeRepository codeRepository = JSONObject.parseObject(
				params, CodeRepository.class);
		return codeRepositoryService.modify(codeRepository, user);
	}
	
	@RequestMapping(value = "/update/{id}/status", method = {RequestMethod.POST})
	@Log(name = "修改代码仓库对象状态信息")
	public BsmResult modifyStatus(@PathVariable(value = Common.ID) Long id){
		return codeRepositoryService.status(id);
	}
}
