package com.bocloud.paas.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.bocloud.paas.server.interceptor.Log;
import com.bocloud.paas.service.process.TaskService;
/**
 * @Describe: 流程編排任务server控制层
 * @author Zaney
 * @Date 2017年6月16日
 */
@RestController
@RequestMapping("/task")
public class TaskController {
	@Autowired
	private TaskService taskService;
	
	/**
	 * 获取所有的taskDefs
	 * @author Zaney
	 */
	@RequestMapping(value = "/taskDefs", method = { RequestMethod.GET })
	@Log(name="获取所有的任务列表")
	public BsmResult getTaskDefs(){
		return taskService.taskDefs();
	}
	/**
	 * 获取任务模版
	 * @author Zaney
	 */
	@RequestMapping(value = "/template", method = { RequestMethod.GET })
	@Log(name="获取任务模版")
	public BsmResult template(@RequestParam(value = Common.PARAMS) String params,
			@Value(Common.REQ_USER) RequestUser user){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return taskService.template(object, user);
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 获取任务参数模板
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/param", method = { RequestMethod.GET })
	@Log(name="获取任务参数模板")
	public BsmResult paramTemplate(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return taskService.getTaskParam(object.getString("taskName"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 获取某个task
	 * @author Zaney
	 */
	@RequestMapping(value = "/task", method = { RequestMethod.GET })
	@Log(name="获取编排流程里某个任务信息")
	public BsmResult getTask(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return taskService.task(object.getString("taskId"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 获取任务输出参数属性
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/ouput/param", method = { RequestMethod.GET })
	@Log(name="获取任务输出参数属性")
	public BsmResult getTaskOuputParam(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return taskService.getTaskOuputParam(object.getString("taskName"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 获取插件中job的构建次数列表
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/build/count", method = { RequestMethod.GET })
	@Log(name="job构建次数")
	public BsmResult jobBuildCount(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return taskService.buildRecord(object.getString("workflowId"), object.getString("taskId"));
		} else {
			return ResultTools.formatErrResult();
		}
	}
	/**
	 * 获取插件中job的构建输出日志信息
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/build/output", method = { RequestMethod.GET })
	@Log(name="job输出日志信息")
	public BsmResult getJobBuildOutput(@RequestParam(value = Common.PARAMS) String params){
		JSONObject object = JSONTools.isJSONObj(params);
		if (null != object) {
			return taskService.getJobBuildOutput(object.getString("jobName"), Integer.valueOf(object.getString("buildNum")));
		} else {
			return ResultTools.formatErrResult();
		}
	}
}
