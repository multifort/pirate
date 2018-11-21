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
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.Volume;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.resource.PersistentVolumeService;

@RestController
@RequestMapping("/pv")
public class PersistentVolumeController {

	@Autowired
	private PersistentVolumeService persistentVolumeService;

	/**
	 * 查询pv
	 * 
	 * @param page
	 * @param rows
	 * @param params
	 * @param sorter
	 * @param simple
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	@Log(name = "查询存储卷")
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
		BsmResult result = persistentVolumeService.list(page, rows, paramList, sorterMap, simple, user.getId());
		return result;
	}

	/**
	 * 创建pv
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "创建存储卷")
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Volume pv = JSONObject.parseObject(object.toJSONString(), Volume.class);
			BsmResult result = persistentVolumeService.create(pv, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询创建存储所需的参数
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/queryPVTemplate", method = { RequestMethod.POST })
	@Log(name = "查询存储卷模板")
	public BsmResult queryPVTemplate(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Volume pv = JSONObject.parseObject(object.toJSONString(), Volume.class);
			BsmResult result = persistentVolumeService.queryPVTemplate(pv, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除存储，支持批量删除
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	@Log(name = "删除存储")
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			List<Long> ids = JSON.parseArray(jsonObject.get("id").toString(), Long.class);
			return persistentVolumeService.remove(ids, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 查询存储详情
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/detail", method = { RequestMethod.GET })
	@Log(name = "查询存储详情")
	public BsmResult detail(@PathVariable(value = Common.ID) Long id) {
		return persistentVolumeService.detail(id);
	}

	/**
	 * 编辑存储卷
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "编辑存储卷")
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Volume volume = JSONObject.parseObject(object.toJSONString(), Volume.class);
			BsmResult result = persistentVolumeService.modify(volume, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	@RequestMapping(value = "/queryPVData", method = {RequestMethod.POST})
	@Log(name = "获取存储卷数据")
	public BsmResult queryPVData(@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Volume volume = JSONObject.parseObject(object.toJSONString(), Volume.class);
			BsmResult result = persistentVolumeService.queryPVData(volume, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}
}
