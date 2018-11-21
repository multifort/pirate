package com.bocloud.paas.web.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.MapTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;
/**
 * @Describe: 流程編排任务web控制层
 * @author Zaney
 * @Date 2017年6月16日
 */
@RestController
@RequestMapping("/task")
public class TaskController {
	private final String BASE_SERVICE = "/task";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;
	
	/**
	 * 获取所有的taskDefs
	 * @author Zaney
	 */
	@RequestMapping(value = "/taskDefs", method = { RequestMethod.GET })
	public BsmResult getTaskDefs(HttpServletRequest request){
		String url = BASE_SERVICE + "/taskDefs";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null, request);
		return service.invoke();
	}
	/**
	 * 获取模版
	 * @author Zaney
	 */
	@RequestMapping(value = "/template", method = { RequestMethod.GET })
	public BsmResult template(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/template";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 获取任务参数模板
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/param", method = { RequestMethod.GET })
	public BsmResult paramTemplate(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/param";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 获取某个task
	 * @author Zaney
	 */
	@RequestMapping(value = "/task", method = { RequestMethod.GET })
	public BsmResult getTask(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/task";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 获取任务输出参数属性
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/ouput/param", method = { RequestMethod.GET })
	public BsmResult getTaskOuputParam(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/ouput/param";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 获取插件中job的构建次数列表
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/build/count", method = { RequestMethod.GET })
	public BsmResult jobBuildCount(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/build/count";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 获取插件中job的构建输出日志信息
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/build/output", method = { RequestMethod.GET })
	public BsmResult getJobBuildOutput(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/build/output";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}

}
