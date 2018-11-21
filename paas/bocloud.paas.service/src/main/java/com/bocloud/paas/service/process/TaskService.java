package com.bocloud.paas.service.process;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
/**
 * @Describe: 流程编排 任务业务层
 * @author Zaney
 * @2017年6月14日
 */
public interface TaskService {
	/**
	 * 获取所有的taskDefs
	 * @return
	 */
	public BsmResult taskDefs();

	/**
	 * 根据taskId获取任务信息
	 * @param taskId
	 * @return
	 */
	public BsmResult task(String taskId);

	/**
	 * 获取插件的输出参数
	 * @param taskName
	 * @return
	 */
	public BsmResult getTaskOuputParam(String taskName);
	/**
	 * 获取某任务的job构建信息
	 * @param workflowId
	 * @param taskId
	 * @return
	 */
	public BsmResult buildRecord(String workflowId, String taskId);
	/**
	 * 获取某任务的job某次构建日志信息
	 * @param workflowId
	 * @param taskId
	 * @return
	 */
	public BsmResult getJobBuildOutput(String jobName, int buildNum);
	/**
	 * 根据任务类型，获取对应的模板信息
	 * @param type
	 * @return
	 */
	public BsmResult getTaskParam(String taskName);
	/**
	 * 获取task模版
	 * @param type
	 * @return
	 */
	public BsmResult template(JSONObject object, RequestUser requestUser);
}
