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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.JSONTools;
import com.bocloud.common.utils.ResultTools;
import com.bocloud.paas.entity.Workflow;
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.process.WorkflowService;
/**
 * @Describe: 流程编排 页面纵览 server 控制层
 * @author Zaney
 * @2017年6月14日
 */
@RestController
@RequestMapping("/workflow")
public class WorkflowController {
	
	@Autowired
	private WorkflowService workflowService;
	
	/**
	 * 页面数据纵览 统计
	 * @author Zaney
	 */
	@RequestMapping(value = "/total", method = { RequestMethod.GET })
	@Log(name="数据统计")
	public BsmResult total(@Value(Common.REQ_USER) RequestUser user){
		return workflowService.total(user);
	}
	
	/**
	 * 运行工作流
	 * @param params
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/running", method = { RequestMethod.POST })
	@Log(name="运行工作流")
	public BsmResult search(@RequestParam(value = Common.PARAMS) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			Long id = object.getLong("id");
			return workflowService.running(id, user);
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 创建workflowDef
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflowDef", method = { RequestMethod.POST })
	@Log(name="创建流程编排模版")
	public BsmResult createWorkflow(@RequestParam(value = Common.PARAMS) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			JSONObject data = JSONObject.parseObject(object.getString("data"));
			JSONArray tasks = JSONObject.parseArray(object.getString("tasks"));
			return workflowService.create(data, tasks, user);
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 修改workflowDef
	 * @author Zaney
	 */
	@RequestMapping(value = "/update/workflowDef", method = { RequestMethod.POST })
	@Log(name="修改流程编排模版")
	public BsmResult updateWorkflow(@RequestParam(value = Common.PARAMS) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			JSONObject data = JSONObject.parseObject(object.getString("data"));
			JSONArray tasks = JSONObject.parseArray(object.getString("tasks"));
			Workflow workflow = JSONObject.parseObject(object.getString("workflow"), Workflow.class);
			return workflowService.modify(data, tasks, workflow, user);
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 从数据库获取页面模板排版信息
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflowDefJSON", method = { RequestMethod.GET })
	@Log(name="修改流程编排模版")
	public BsmResult getWorkflowDefJSON(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			String workflowDefName = object.getString("name");
			Integer version = Integer.valueOf(object.getString("version"));
			return workflowService.workflowDefJSON(workflowDefName, version);
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 启动 workflowDef
	 * @author Zaney
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/start", method = { RequestMethod.POST })
	@Log(name="启动流程编排")
	public BsmResult startWorkflow(@RequestParam(value = Common.PARAMS) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			String workflowDefName = object.getString("name");
			Integer version = Integer.valueOf(object.getString("version"));
			String correlationId = user.getId().toString();
			Map<String, Object> input = JSONObject.parseObject(object.getString("input"), Map.class);
			return workflowService.start(workflowDefName, version, correlationId, input);
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 获取workflow
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflow", method = { RequestMethod.GET })
	@Log(name="获取某个流程编排")
	public BsmResult getWorkflow(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return workflowService.workflow(object.getString("workflowId"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
//	/**
//	 * 删除workflow
//	 * @author Zaney
//	 */
//	@RequestMapping(value = "/workflow", method = { RequestMethod.DELETE })
//	@Log(name="获取某个流程编排")
//	public BsmResult deleteWorkflow(@RequestParam(value = Common.PARAMS) String params){
//		JSONObject object = JSONTools.isJSONObj(params);
//		if (null != object) {
//			return workflowService.removeWorkflow(object.getJSONArray("ids"), object.getString("name"), Integer.valueOf(object.getString("version")));
//		} else {
//			return ResultTools.formatErrResult();
//		}
//	}
	/**
	 * 获取所有的workflowDefs
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflowDefs", method = { RequestMethod.GET })
	@Log(name="获取全部流程编排模版")
	public BsmResult getWorkflowDefs(@RequestParam(value = Common.PAGE, required = false) Integer page,
			@RequestParam(value = Common.ROWS, required = false) Integer rows,
			@RequestParam(value = Common.PARAMS, required = false) String params,
			@RequestParam(value = Common.SORTER, required = false) String sorter,
			@RequestParam(value = Common.SIMPLE, required = false) Boolean simple,
			@Value(Common.REQ_USER) RequestUser user){
		
			List<Param> paramList = JSONObject.parseArray(params, Param.class);
			@SuppressWarnings("unchecked")
			Map<String, String> sorterMap = JSONObject.parseObject(sorter, HashMap.class);
			return workflowService.listWorkflowDef(page, rows, paramList, sorterMap, simple, user);
	}
	/**
	 * 获取workflowDef详情
	 * @author Zaney
	 */
	@RequestMapping(value = "/workflowDef", method = { RequestMethod.GET })
	@Log(name="获取流程编排模版详情")
	public BsmResult workflowDef(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return workflowService.workflowDef(object.getString("name"), object.getInteger("version"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 暂停某个工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/pause", method = { RequestMethod.POST })
	@Log(name="暂停某个工作流")
	public BsmResult pauseWorkflow(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return workflowService.pauseWorkflow(object.getString("workflowId"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 继续执行工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/resume", method = { RequestMethod.POST })
	@Log(name="继续某个工作流")
	public BsmResult resumeWorkflow(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return workflowService.resumeWorkflow(object.getString("workflowId"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 重启某个工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/restart", method = { RequestMethod.POST })
	@Log(name="重启某个工作流")
	public BsmResult restartWorkflow(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return workflowService.restart(object.getString("workflowId"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 重试某个工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/retry", method = { RequestMethod.POST })
	@Log(name="重试某个工作流")
	public BsmResult retryLastFailedTask(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return workflowService.retryLastFailedTask(object.getString("workflowId"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 终止某个工作流
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/terminate", method = { RequestMethod.POST })
	@Log(name="终止某个工作流")
	public BsmResult terminateWorkflow(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return workflowService.terminateWorkflow(object.getString("workflowId"));
		} else {
			return ResultTools.formatErrResult();
		}
	}

	/**
	 * 获取任务中需要工作流的输入参数作为输入参数的属性
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/input/param", method = { RequestMethod.GET })
	@Log(name="获取任务中需要工作流的输入参数作为输入参数的属性")
	public BsmResult getWorkflowInputParam(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return workflowService.getWorkflowInputParam(object.getJSONArray("tasks"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	
}
