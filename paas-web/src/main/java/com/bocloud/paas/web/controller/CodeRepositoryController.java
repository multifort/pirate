package com.bocloud.paas.web.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ListHelper;
import com.bocloud.common.utils.MapTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;
import com.bocloud.registry.utils.UrlTranslator;

/**
 * describe: 代码仓库web控制层
 * @author Zaney
 * @data 2017年10月17日
 */
@RestController
@RequestMapping("/code/repository")
public class CodeRepositoryController {
	private final String BASE_SERVICE = "/code/repository";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;
	
	/**
	 * 创建
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/code", method = { RequestMethod.POST })
	public BsmResult create(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/code";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap,
					request);
			return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 列表
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/code", method = {RequestMethod.GET})
	public BsmResult list(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROW, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple,
			HttpServletRequest request) {
		Map<String, Object> param = ListHelper.assembleParam(page, rows, params, sorter, simple);
        String url = BASE_SERVICE + "/code";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, param, request);
        return service.invoke();
	}
	
	/**
	 * 详情
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{id}/code", method = {RequestMethod.GET})
	public BsmResult detail(@PathVariable(value = Common.ID) Long id,
			HttpServletRequest request){
        String url = UrlTranslator.translate(BASE_SERVICE + "/code", id);
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null,
                request);
        return service.invoke();
	}
	
	/**
	 * 删除
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/code", method = {RequestMethod.DELETE})
	public BsmResult remove(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/code";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
            RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.REMOVE, null, paramMap,
                    request);
            return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 修改
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/update/code", method = {RequestMethod.POST})
	public BsmResult modify(@RequestParam(value = Common.PARAMS, required = true) String params,
			HttpServletRequest request){
		JSONObject jsonObject = JSONTools.isJSONObj(params);
		if (null != jsonObject) {
			String url = BASE_SERVICE + "/update/code";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
            RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap,
                    request);
            return service.invoke();
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
	/**
	 * 修改状态
	 * @param params
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/update/{id}/status", method = {RequestMethod.POST})
	public BsmResult modifyStatus(@PathVariable(value = Common.ID) Long id,
			HttpServletRequest request){
        String url = UrlTranslator.translate(BASE_SERVICE + "/update/status", id);
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, null,
                request);
        return service.invoke();
	}
	
}
