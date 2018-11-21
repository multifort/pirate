package com.bocloud.paas.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.bocloud.paas.entity.Dictionary;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.system.DictService;

@RestController
@RequestMapping("/dict")
public class DictionaryController {

	// private static Logger logger =
	// LoggerFactory.getLogger(DictionaryController.class);

	@Autowired
	private DictService dictService;

	/**
	 * 创建数据字典
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/create", method = { RequestMethod.POST })
	@Log(name = "创建系统参数")
	public BsmResult create(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Dictionary dictionary = JSONObject.parseObject(
					object.toJSONString(), Dictionary.class);
			BsmResult result = dictService.create(dictionary, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 修改数据字典
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/{id}/modify", method = { RequestMethod.POST })
	@Log(name = "修改系统参数")
	public BsmResult modify(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Dictionary dictionary = JSONObject.parseObject(
					object.toJSONString(), Dictionary.class);
			BsmResult result = dictService.modify(dictionary, user.getId());
			return result;
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 删除数据字典
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/remove", method = { RequestMethod.POST })
	public BsmResult remove(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		List<Long> ids = JSON.parseArray(jsonObject.get("id").toString(),
				Long.class);
		return dictService.remove(ids, user.getId());
	}

	/**
	 * 查询数据字典
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
	@Log(name = "系统参数列表查询")
	public BsmResult list(
			@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			@Value(Common.REQ_USER) RequestUser user) {
		List<Param> paramList = JSONObject.parseArray(params, Param.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sorterMap = JSONObject.parseObject(sorter,
				HashMap.class);
		BsmResult result = dictService.list(page, rows, paramList, sorterMap,
				simple);
		return result;
	}

	/**
	 * 校验数据字典key是否存在
	 * 
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/checkKey", method = { RequestMethod.GET })
	public BsmResult checkKey(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String dictKey = jsonObject.getString("key");
			return dictService.checkKey(dictKey, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}

	@RequestMapping(value = "/statistic", method = { RequestMethod.GET })
	@Log(name = "系统参数统计")
	public BsmResult statistic(
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		return dictService.statistic();
	}

	@RequestMapping(value = "/batchModify", method = { RequestMethod.POST })
	@Log(name = "修改系统参数")
	public BsmResult batchModify(
			@RequestParam(value = Common.PARAMS, required = true) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			List<Dictionary> dictionarys = JSON.parseArray(jsonObject
					.getString("dicts").toString(), Dictionary.class);
			return dictService.batchModify(dictionarys, user.getId());
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	@RequestMapping(value = "/detail", method = { RequestMethod.GET })
	@Log(name="系统参数详情")
	public BsmResult detail(@RequestParam(value = Common.PARAMS, required = false) String params,
			@Value(Common.REQ_USER) RequestUser user) {
		
		JSONObject jsonObject = JSONTools.isJSONObj(params);

		String dictKey = null;
		if (null != jsonObject) {
			if (null != jsonObject.get("dictKey")) {
				dictKey = jsonObject.get("dictKey").toString();
			}
		}
		return dictService.detail(dictKey);
	}
	
	@RequestMapping(value = "/template", method = { RequestMethod.GET })
	@Log(name="获取参数新增模板")
	public BsmResult template() {
		return dictService.template();
	}

}
