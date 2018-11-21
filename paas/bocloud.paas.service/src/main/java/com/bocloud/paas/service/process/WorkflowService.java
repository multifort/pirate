package com.bocloud.paas.service.process;

import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.paas.entity.Workflow;

/**
 * @Describe: 流程编排业务层
 * @author Zaney
 * @2017年6月14日
 */
public interface WorkflowService {
	
	/**
	 * 创建WorkowDef
	 * @param id
	 * @return
	 */
	public BsmResult create(JSONObject data, JSONArray tasks, RequestUser requestUser);
	/**
	 * 修改WorkowDef
	 * @param object
	 * @return
	 */
	public BsmResult modify(JSONObject data, JSONArray tasks, Workflow workflow, RequestUser requestUser);
	/**
	 * 获取所有的workflowDef
	 * @return
	 */
	public BsmResult listWorkflowDef(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, 
			RequestUser requestUser);
	
	/**
	 * 从数据库获取workflowDefJSON,用于流程图
	 * @param workflowDefName
	 * @param version
	 * @return
	 */
	public BsmResult workflowDefJSON(String workflowDefName, Integer version);
	/**
	 * 获取流程编排模板详情
	 * @param workflowDefName
	 * @param version
	 * @return
	 */
	public BsmResult workflowDef(String workflowDefName, Integer version);
	
	
	//======================== 以下是 workflow 功能=================//
	
	/**
	 * 统计workflow有状态的数据
	 * @return
	 */
	public BsmResult total(RequestUser requestUser);
	/**
	 * 启动workflowDef
	 * @param workflowDefName
	 * @param version
	 * @param correlationId
	 * @param input
	 * @return
	 */
	public BsmResult start(String workflowDefName, Integer version, String correlationId, Map<String, Object> input);

	/**
	 * 获取某个workflow
	 * @return
	 */
	public BsmResult workflow(String workflowId);
	/**
	 *   解析所有任务中，需要workflow输入参数的参数名  
	 *   如：${workflow.input.param},则解析出param这个参数
	 *   目前平台没用到这个功能，代码保留
	 * @param tasks
	 * @return
	 */
	public BsmResult getWorkflowInputParam(JSONArray tasks);
	/**
	 * 暂停
	 * @param workflowId
	 * @return
	 */
	public BsmResult pauseWorkflow(String workflowId);
	/**
	 * 继续
	 * @param workflowId
	 * @return
	 */
	public BsmResult resumeWorkflow(String workflowId);
	/**
	 * 
	 * @param workflowId
	 * @return
	 */
	public BsmResult restart(String workflowId);
	/**
	 * 在最后一个失败的任务上重新执行
	 * @param workflowId
	 * @return
	 */
	public BsmResult retryLastFailedTask(String workflowId);
	/**
	 * 终止
	 * @param workflowId
	 * @return
	 */
	public BsmResult terminateWorkflow(String workflowId);
	/**
	 * 获取workflow详情
	 * @param workflowId
	 * @return
	 */
	public com.netflix.conductor.common.run.Workflow getWorkflow(String workflowId);
	/**
	 * 运行工作流
	 * @param id
	 * @param user
	 * @return
	 */
	public BsmResult running(Long id, RequestUser user);

}
