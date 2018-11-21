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
import com.bocloud.common.utils.ListHelper;
import com.bocloud.common.utils.MapTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

/**
 * @Describe: 流程编排 页面纵览 web 控制层
 * @author Zaney
 * @2017年6月14日
 */
@RestController
@RequestMapping("/workflow")
public class WorkflowController {
	private final String BASE_SERVICE = "/workflow";
	private static final BoCloudService SERVICE = BoCloudService.Cmp;
	@Autowired
	private ServiceFactory serviceFactory;
	
	/**
	 * 页面数据纵览 统计
	 * @author Zaney
	 */
	@RequestMapping(value = "/total", method = { RequestMethod.GET })
	public BsmResult total(HttpServletRequest request){
		String url = BASE_SERVICE + "/total";
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, null, request);
		return service.invoke();
	}
	
//	/**
//	 * 条件查询流程编排
//	 * @author Zaney
//	 */
//	@RequestMapping(value = "/search", method = { RequestMethod.GET })
//	public BsmResult search(@RequestParam(value = Common.PARAMS) String params,
//			HttpServletRequest request){
//		String url = BASE_SERVICE + "/search";
//		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
//		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
//		return service.invoke();
//	}
	
	/**
	 * 启动 workflowDef
	 * @author Zaney
	 */
	@RequestMapping(value = "/start", method = { RequestMethod.POST })
	public BsmResult startWorkflow(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/start";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OTHERS, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 创建workflowDef 
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflowDef", method = { RequestMethod.POST })
	public BsmResult createWorkflow(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/workflowDef";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 修改workflowDef
	 * @author Zaney
	 */
	@RequestMapping(value = "/update/workflowDef", method = { RequestMethod.POST })
	public BsmResult updateWorkflow(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/update/workflowDef";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 从数据库获取页面模板排版信息
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflowDefJSON", method = { RequestMethod.GET })
	public BsmResult getWorkflowDefJSON(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/workflowDefJSON";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 运行 workflowDef
	 * @author Zaney
	 */
	@RequestMapping(value = "/running", method = { RequestMethod.POST })
	public BsmResult runningWorkflow(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/running";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.CREATE, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 获取workflow
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflow", method = { RequestMethod.GET })
	public BsmResult getWorkflow(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/workflow";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 删除workflow
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflow", method = { RequestMethod.DELETE })
	public BsmResult deleteWorkflow(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/workflow";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.REMOVE, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 获取所有的workflowDefs
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflowDefs", method = { RequestMethod.GET })
	public BsmResult getWorkflowDefs(@RequestParam(value = Common.PAGE, required = false, defaultValue = Common.ONE) Integer page,
			@RequestParam(value = Common.ROWS, required = false, defaultValue = Common.TEN) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) boolean simple, HttpServletRequest request){
		Map<String, Object> param = ListHelper.assembleParam(page, rows, params, sorter, simple);
        String url = BASE_SERVICE + "/workflowDefs";
        RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, param, request);
        return service.invoke();
	}
	/**
	 * 获取所有的workflowDef
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflowDef", method = { RequestMethod.GET })
	public BsmResult getWorkflowDef(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/workflowDef";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}

	/**
	 * 暂停某个工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/pause", method = { RequestMethod.POST })
	public BsmResult pauseWorkflow(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/pause";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
		return service.invoke();
	}
	/**
	 * 继续执行工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/resume", method = { RequestMethod.POST })
	public BsmResult resumeWorkflow(@RequestParam(value = Common.PARAMS) String params,
		HttpServletRequest request){
			String url = BASE_SERVICE + "/resume";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
			return service.invoke();
	}
	/**
	 * 重启某个工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/restart", method = { RequestMethod.POST })
	public BsmResult restartWorkflow(@RequestParam(value = Common.PARAMS) String params,
		HttpServletRequest request){
			String url = BASE_SERVICE + "/restart";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
			return service.invoke();
	}
	/**
	 * 重试某个工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/retry", method = { RequestMethod.POST })
	public BsmResult retryLastFailedTask(@RequestParam(value = Common.PARAMS) String params,
		HttpServletRequest request){
			String url = BASE_SERVICE + "/retry";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
			return service.invoke();
	}
	/**
	 * 终止某个工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/terminate", method = { RequestMethod.POST })
	public BsmResult terminateWorkflow(@RequestParam(value = Common.PARAMS) String params,
		HttpServletRequest request){
			String url = BASE_SERVICE + "/terminate";
			Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
			RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.OPERATE, null, paramMap, request);
			return service.invoke();
	}

	/**
	 * 获取任务中需要工作流的输入参数作为输入参数的属性
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/input/param", method = { RequestMethod.GET })
	public BsmResult getWorkflowInputParam(@RequestParam(value = Common.PARAMS) String params,
			HttpServletRequest request){
		String url = BASE_SERVICE + "/input/param";
		Map<String, Object> paramMap = MapTools.simpleMap(Common.PARAMS, params);
		RemoteService service = serviceFactory.safeBuild(SERVICE, url, BoCloudMethod.BASIC, null, paramMap, request);
		return service.invoke();
	}
}
