package com.bocloud.paas.web.controller.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.MapTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

/**
 * 字典信息处理API
 * 
 * @author dmw
 *
 */
@RestController
@RequestMapping("/dict")
public class DictController {
	private static final String BASE_SERVICE = "/dict";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;

	/**
	 * 字典树
	 * 
	 * @param value
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST })
	public BsmResult list(@RequestParam(value = Common.VALUE, required = true) String value,
			HttpServletRequest request) {
		Map<String, Object> param = new HashMap<>();
		if (null != value) {
			param.put(Common.VALUE, value);
		}
		String url = BASE_SERVICE + "/list";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.LIST, null, param, request);
		return service.invoke();
	}

	/**
	 * 字典列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/children", method = { RequestMethod.GET })
	public BsmResult children(@RequestParam(value = Common.PARAMS, required = false) String params,
			HttpServletRequest request) {
		JSONObject object = JSONObject.parseObject(params);
		Map<String, Object> param = null;
		if (null != object) {
			param = MapTools.simpleMap(Common.VALUE, object.get(Common.VALUE));
		}
		String url = BASE_SERVICE + "/children";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, param, request);
		return service.invoke();
	}

}
