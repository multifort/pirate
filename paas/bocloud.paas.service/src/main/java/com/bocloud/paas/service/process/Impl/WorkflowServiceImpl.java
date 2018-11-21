package com.bocloud.paas.service.process.Impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.GridBean;
import com.bocloud.common.model.Param;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.model.Result;
import com.bocloud.common.model.SimpleBean;
import com.bocloud.common.utils.Common;
import com.bocloud.common.utils.GridHelper;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.paas.common.util.ExtendHttpClient;
import com.bocloud.paas.dao.process.WorkflowDao;
import com.bocloud.paas.dao.user.UserDao;
import com.bocloud.paas.entity.User;
import com.bocloud.paas.entity.Workflow;
import com.bocloud.paas.service.process.WorkflowService;
import com.bocloud.paas.service.process.config.JenkinsConfig;
import com.bocloud.paas.service.process.config.WorkflowConfig;
import com.bocloud.paas.service.process.util.JenkinsClient;
import com.bocloud.paas.service.user.UserService;
import com.google.common.collect.Maps;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.SearchResult;
import com.netflix.conductor.common.run.WorkflowSummary;

/**
 * @Describe: 流程编排 数据总览业务层实现
 * @author Zaney
 * @2017年6月14日
 */
@Service("workflowService")
public class WorkflowServiceImpl implements WorkflowService {
	private static Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);
	private static final String HTTPS = "http://";
	private static final String API = "/api/";	
	@Autowired
	private WorkflowConfig workflowConfig;
	@Autowired
	private JenkinsConfig jenkinsConfig;
	@Autowired
	private WorkflowDao workflowDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserService userService;
	
	/**
	 * 获取资源连接
	 * @return
	 */
	private WorkflowClient getClient(){
		WorkflowClient workflowClient = new WorkflowClient();
		workflowClient.setRootURI(HTTPS+workflowConfig.getWorkflowUrl()+":"+workflowConfig.getWorkflowPort()+API);
		return workflowClient;
	}
	
	/**
	 * 过滤掉插件自带的WorkflowDef
	 * @param name
	 * @return
	 */
	private boolean filter(String name, int version){
		String[] args = new String[]{"kitchensink:1", "sub_flow_1:1"};
		if (Arrays.asList(args).contains(name+":"+version)) {
			return true;
		}
		return false;
	}
	
	@Override
	public BsmResult create(JSONObject data, JSONArray tasks, RequestUser requestUser) {
		User user = null;
		if (null == (user = getUser(requestUser.getId()))) {
			return new BsmResult(false,"未获取到当前用户信息");
		}
		//名称校验
		boolean isExist  = checkName(data.getString("name"), Integer.valueOf(data.getString("version")));
		if (!isExist) {
			return new BsmResult(false, "名称["+data.getString("name")+"]/版本["+data.getString("version")+"] 该版本已存在");
		}
		
		//格式校验
		WorkflowDef workflowDef = checkFormate(data, tasks);
		if (workflowDef == null) {
			return new BsmResult(false,"格式不正确");
		}
		
		//创建
		WorkflowClient client = getClient();
		try {
			client.registerWorkflow(workflowDef);
		} catch (Exception ex) {
			logger.error("create workflow exception in conductor", ex);
			return new BsmResult(false, "在服务端conductor创建工作流 异常");
		}
		
		//修改数据库信息
		try {
			Workflow workflow = new Workflow();
			workflow.setVersion(Integer.valueOf(data.getString("version")));
			workflow.setName(data.getString("name"));
			workflow.setWorkflowJson(data.toJSONString());
			workflow.setWorkflowDef(JSON.toJSONString(workflowDef));
			workflow.setCreaterId(requestUser.getId());
			workflow.setMenderId(requestUser.getId());
			workflow.setOwnerId(requestUser.getId());
			workflow.setDeptId(user.getDepartId());
			if (!workflowDao.baseSave(workflow)) {
				return new BsmResult(false, "创建编排成功【数据库储存信息失败】");
			}
			return new BsmResult(true, "创建编排成功");
		} catch (Exception e) {
			logger.error("create workflow exception", e);
			return new BsmResult(false, "创建编排成功【数据库储存信息异常】");
		}
	}
	/**
	 * 获取用户信息
	 * @param id
	 * @return
	 */
	private User getUser(Long id) {
		User user = null;
		try {
			user = userDao.query(id);
			if (null == user) {
				logger.warn("该用户不存在");
			}
		} catch (Exception e) {
			logger.error("获取该用户信息异常", e);
		}
		return user;
	}
	@Override
	public BsmResult modify(JSONObject data, JSONArray tasks, Workflow workflow, RequestUser requestUser) {
		//格式校验
		WorkflowDef workflowDef = checkFormate(data, tasks);
		
		//修改工作流服务端的信息以及重新启动
		if (!"NOT_RUNNING".equals(workflow.getStatus())) {
			boolean isStart = update(workflowDef, workflow, requestUser.getId());
			if (!isStart) {
				return new BsmResult(false, "修改工作流失败");
			}
		} else {
			return new BsmResult(true, "请先执行【运行】工作流后，再编辑");
		}
		
		//数据库修改
		try {
			workflow.setVersion(Integer.valueOf(data.getString("version")));
			workflow.setName(data.getString("name"));
			workflow.setWorkflowJson(data.toJSONString());
			
			workflow.setWorkflowDef(JSON.toJSONString(workflowDef));
			workflow.setCreaterId(requestUser.getId());
			workflow.setMenderId(requestUser.getId());
			workflow.setOwnerId(requestUser.getId());
			if (!workflowDao.update(workflow)) {
				return new BsmResult(false, "修改编排成功【数据库修改信息失败】");
			}
			return new BsmResult(true, "修改编排成功");
		} catch (Exception e) {
			logger.error("update workflowDef exception", e);
			return new BsmResult(false, "修改编排成功【数据库修改信息异常】");
		}
	}
	/**
	 * 获取数据库工作流信息
	 * @param id
	 * @return
	 */
	private Workflow getWorkflow(Long id){
		try {
			return workflowDao.query(id);
		} catch (Exception e) {
			logger.error("Get workflow from JDBC exception", e);
			return null;
		}
	}
	
	/**
	 * 修改工作流，同时做了个判断，主要是防止conductor数据库挂了，数据消失的情况
	 * @param workflowDef
	 * @return
	 */
	private boolean update(WorkflowDef workflowDef, Workflow workflow, Long userId){
		//请求修改
		Result result = updateWorkflowDefClient(workflowDef);
		if (result.isFailed()) {
			return false;
		}
		//满足条件，重启restart工作流
		if ("TERMINATED".equals(workflow.getStatus()) || "TIMED_OUT".equals(workflow.getStatus()) 
				|| "COMPLETED".equals(workflow.getStatus()) || "FAILED".equals(workflow.getStatus())) {
			BsmResult restart = restart(workflow.getWorkflowId());
			if (restart.isFailed()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public BsmResult listWorkflowDef(int page, int rows, List<Param> params, Map<String, String> sorter, Boolean simple, 
			RequestUser requestUser) {
		BsmResult bsmResult = new BsmResult(false, "未获取到工作流信息");
		List<Workflow> workflows = null;
		List<SimpleBean> beans = null;
		int total = 0;
		GridBean gridBean = null;

		try {
			//获取当前用户所在的组织机构以及组织机构下的子机构ID
			String deptIds = userService.listDept(requestUser.getId());
			if (ListTool.isEmpty(params)) {
				params = new ArrayList<Param>();
			}
			
			if (null == sorter) {
				sorter = Maps.newHashMap();
			}
			sorter.put("gmtCreate", Common.ONE);
			total = workflowDao.count(params, deptIds);
			if (simple) {
				beans = workflowDao.list(params, sorter, deptIds);
				gridBean = new GridBean(1, 1, total, beans);
			} else {
				workflows = workflowDao.list(page, rows, params, sorter, deptIds);
				if (!ListTool.isEmpty(workflows)) {
					for (Workflow workflow : workflows) {
						//获取工作流非数据库里的信息
						workflow = getInfo(workflow);
					}
				}
				gridBean = GridHelper.getBean(page, rows, total, workflows);
			}
			bsmResult.setMessage("查询工作流模板信息成功！");
			bsmResult.setData(gridBean);
			bsmResult.setSuccess(true);
		} catch (Exception e) {
			logger.error("list workflowDef exception:", e);
		}
		return bsmResult;
	}
	/**
	 * 获取工作流非数据库里的信息
	 * @param workflow
	 * @return
	 */
	private Workflow getInfo(Workflow workflow){
		// 获取用户信息
		// 获取修改者信息
		try {
			User creator = userDao.query(workflow.getCreaterId());
			workflow.setCreatorName(creator.getName());
			User mender = userDao.query(workflow.getMenderId());
			workflow.setMenderName(mender.getName());
		} catch (Exception e) {
			logger.error("Get user info exception", e);
		}
		
		//获取工作流实例信息
		com.netflix.conductor.common.run.Workflow wf = getWorkflow(workflow.getWorkflowId());
		if (null == wf) {
			workflow.setStatus("NOT_RUNNING");
		} else {
			
			workflow.setStatus(String.valueOf(wf.getStatus()));
		}
		return workflow;
	}
	
	@Override
	public BsmResult workflowDefJSON(String workflowDefName, Integer version) {
		Workflow workflow = null;
		try {
			workflow = workflowDao.selectWorkflow(workflowDefName, version);
			if (null == workflow) {
				logger.error("Workflow info is null");
				return new BsmResult(false, "获取编排模板信息为空");
			}
			return new BsmResult(true, workflow, "获取编排模板信息成功");
		} catch (Exception e) {
			logger.error("Workflow info is Exception", e);
			return new BsmResult(false, "获取编排模板信息异常");
		}
	}
	
	@Override
	public BsmResult workflowDef(String workflowDefName, Integer version){
		WorkflowClient client = getClient();
		WorkflowDef workflowDef = null;
		try {
			workflowDef = client.getWorkflowDef(workflowDefName, version);
		} catch (Exception e) {
			logger.error("Get workflowDef infomation exception", e);
			return new BsmResult(false, "未获取到工作流模板的详情， 请检查网络连接");
		}
		return new BsmResult(true, workflowDef, "获取成功");
	}
	
	//=======================以下 是 workflow 功能代码 ===========================//
	
	@Override
	public BsmResult total(RequestUser requestUser) {
		JSONObject object = new JSONObject();
		String[] statuss = new String[]{"RUNNING", "TIMED_OUT", "TERMINATED", "COMPLETED", "FAILED", "PAUSED"};
		int stateful = 0;//统计有状态数量
		
		//获取当前用户所在的组织机构以及组织机构下的子机构ID
		String deptIds = userService.listDept(requestUser.getId());
		int count = 0;
		try {
			count = workflowDao.count(null,deptIds);
		} catch (Exception e) {
			logger.error("Get all exception");
		}
		object.put("TOTAL", count);//总的
		
		for (String status : statuss) {
			Integer num = getNum(status);
			object.put(status, num);
			stateful += num;
		}
		object.put("NOT_RUNNING", count-stateful);//无状态，NOT_RUNNING
		return new BsmResult(true, object, "统计成功");
	}
	
	@Override
	public BsmResult start(String workflowDefName, Integer version, String correlationId, Map<String, Object> input) {
		if (null == input) {
			input = new HashMap<String, Object>();
		}
		try {
			//1、启动流程编排
			WorkflowClient client = getClient();
			String workflowId = null;
			try {
				workflowId = client.startWorkflow(workflowDefName, version, correlationId, input);
			} catch (Exception e) {
				logger.error("start workflow exception, please check conductor network", e);
			}
			if (!StringUtils.hasText(workflowId)) {
				logger.error("start workflow【"+workflowDefName+"】fail");
				return new BsmResult(false, "启动流程编排失败");
			}
			
			//修改工作流信息
			if (!update(workflowDefName, version, workflowId)) {
				logger.error("启动流程编排成功，修改工作流信息失败");
				return new BsmResult(false, "启动流程编排成功，修改工作流信息失败");
			}
			
			return new BsmResult(true, "启动流程编排成功，编排ID:"+workflowId);
		} catch (Exception e) {
			logger.error("start workflow exception", e);
			return new BsmResult(false, "启动流程编排异常");
		}
	}
	/**
	 * 修改工作流ID
	 * @param name
	 * @param version
	 * @param workflowId
	 * @return
	 */
	private boolean update(String name, Integer version, String workflowId){
		try {
			Workflow workflow = workflowDao.selectWorkflow(name, version);
			workflow.setWorkflowId(workflowId);
			return workflowDao.update(workflow);
		} catch (Exception e) {
			logger.error("修改工作流信息ID失败， ID="+workflowId, e);
			return false;
		}
	}

	/**
	 * 过滤掉被删除的工作流id
	 * @param name
	 * @param version
	 * @param workflowId
	 * @return
	 */
//	public Boolean filterDeleteWF(String name, int version, String workflowId){
//		try {
//			Workflow workflow = workflowDao.selectWorkflow(name, version);
//			if (workflow == null) {
//				logger.warn("result is null, workerflow ["+name+"/"+version+"] mismatch in JDBC");
//				return false;
//			}
//			if (null != workflow.getWorkflowids()) {
//				if (!workflow.getWorkflowids().contains(workflowId)) {
//					return true;
//				}
//				return false;
//			}
//			return true;
//		} catch (Exception e) {
//			logger.error("search workflow ["+name+"/"+version+"] exception", e);;
//			return false;
//		}
//	}

	
	@Override
	public BsmResult workflow(String workflowId) {
		 JSONObject object = new JSONObject();
		 
		 //判断该工作流是否运行状态
		 if (!StringUtils.hasText(workflowId)){
			 logger.warn("workflow status is NOT_RUNNING");
			 return new BsmResult(true, null, "获取成功");
		 }
		 
		try {
			//获取workflow详情
			com.netflix.conductor.common.run.Workflow workflow = getWorkflow(workflowId);
			 if (null == workflow) {
				 return new BsmResult(false, "未获取到工作流信息");
			 }
			 
			 object.put("WFjson", (JSONObject) JSONObject.toJSON(workflow));
//			 object.put("arrayTasks", arrayTasks);
			 return new BsmResult(true, object, "获取成功");
		} catch (Exception e) {
			logger.error("get workflow detail exception",e);
			return new BsmResult(false, "获取流程编排信息异常");
		}
	}
	
	//==============================流程编排的管控========================================//
	@Override
	public BsmResult pauseWorkflow(String workflowId) {
		boolean result = managerClient(workflowId+"/pause");
		if (result == false) {
			return new BsmResult(false, "暂停工作流【"+workflowId+"】失败");
		}
		return new BsmResult(true, "暂停工作流【"+workflowId+"】成功");
	}

	@Override
	public BsmResult resumeWorkflow(String workflowId) {
		boolean result = managerClient(workflowId+"/resume");
		if (result == false) {
			return new BsmResult(false, "继续工作流【"+workflowId+"】失败");
		}
		return new BsmResult(true, "继续工作流【"+workflowId+"】成功");
	}

	@Override
	public BsmResult restart(String workflowId) {	
		try {
			WorkflowClient client = getClient();
			client.restart(workflowId);
			return new BsmResult(true, "重启工作流【"+workflowId+"】成功");
		} catch (Exception e) {
			logger.error("restart workflow ID ["+workflowId+"] exception, please check conductor network connection");
			return new BsmResult(false, "重启工作流【"+workflowId+"】异常, 请检查conductor资源连接");
		}
	}

	@Override
	public BsmResult retryLastFailedTask(String workflowId) {
		try{
			WorkflowClient client = getClient();
			client.retryLastFailedTask(workflowId);
			return new BsmResult(true, "从最后失败的任务重启成功");
		} catch (Exception e) {
			logger.error("retry last failed plugin exception, please check conductor network connection");
			return new BsmResult(false, "从最后失败的任务重启异常, 请检查conductor资源连接");
		}
	}

	@Override
	public BsmResult terminateWorkflow(String workflowId) {
		try{
			WorkflowClient client = getClient();
			client.terminateWorkflow(workflowId, "");
			return new BsmResult(true, "终止工作流【"+workflowId+"】成功");
		} catch (Exception e) {
			logger.error("terminate workflow ID ["+workflowId+"] exception, please check conductor network connection");
			return new BsmResult(false, "终止工作流【"+workflowId+"】异常, 请检查conductor资源连接");
		}
	}
	
	//========================== 自动获取workflow输入参数============================//
	/**
	 * 获取动态和选择任务中引用到的workflow参数
	 * @param tasks
	 * @return
	 */
	@Override
	public BsmResult getWorkflowInputParam(JSONArray tasks) {
		List<WorkflowTask> workflowTasks = (List<WorkflowTask>) JSONObject.parseArray(tasks.toJSONString(), WorkflowTask.class);
		JSONArray varietyParams = getVarietyParams(workflowTasks);
		return new BsmResult(true, varietyParams, "获取成功");
	}
	/**
	 * 获取不同类型的workflow输入参数
	 * @param workflowTasks
	 * @return
	 */
	private JSONArray getVarietyParams(List<WorkflowTask> workflowTasks){
		JSONArray array = new JSONArray();
		for (WorkflowTask workflowTask : workflowTasks) {
			String taskDefType = workflowTask.getType();
			switch (taskDefType) {
			case "DECISION":
				JSONArray decisionParams = decisionParam(workflowTask);
				 if (!ListTool.isEmpty(decisionParams) ) {
					 for (Object param : decisionParams) {
						 if (!array.contains(param)) {
							 array.add(param);
						}
					}
				}
				break;
				
			case "FORK_JOIN":
				List<List<WorkflowTask>> forkTasks = workflowTask.getForkTasks();
				JSONArray forkJoinParams = forkJoinParam(forkTasks);
				if (!ListTool.isEmpty(forkJoinParams)) {
					 for (Object param : forkJoinParams) {
						 if (!array.contains(param)) {
							 array.add(param);
						}
					}
				}
                break;
                
			case "HTTP":
				 break;
			default: //SIMPLE、 DYNAMIC、SUB_WORKFLOW
				JSONArray simpleParams = defaultParam(workflowTask);
				if (!ListTool.isEmpty(simpleParams)) {
					 for (Object param : simpleParams) {
						 if (!array.contains(param)) {
							 array.add(param);
						}
					}
				}
				break;
			}
		}
		return array;
	}
	/**
	 * 获取默认任务中，需要的workflow参数
	 * @param workflowTask
	 * @param workflowId
	 * @return
	 */
	private JSONArray defaultParam(WorkflowTask workflowTask){
		JSONArray paramArray = new JSONArray();
		//截取动态任务参数
		Map<String, Object> inputParameters = workflowTask.getInputParameters();
		for (Map.Entry<String, Object> entry : inputParameters.entrySet()) {
				if (entry.getValue().toString().contains("workflow")) {
					String replaceStr = entry.getValue().toString().replace("${", "").replace("}", "");
					if (replaceStr.substring(0, replaceStr.indexOf(".")).equals("workflow")) {
						String param = replaceStr.substring(replaceStr.lastIndexOf(".") + 1);//截取"${workflow.input.param}",获取param
						paramArray.add(param);
					}
				}
		}
		return paramArray;
	}
	/**
	 * 获取选择任务中，需要的workflow参数
	 * @param workflowTask
	 * @param workflowId
	 * @return
	 */
	private JSONArray decisionParam(WorkflowTask workflowTask){
		JSONArray paramArray = new JSONArray();
			Map<String, Object> inputParameters = workflowTask.getInputParameters();
			//输入参数循环
			for(Map.Entry<String, Object> inputParam : inputParameters.entrySet()){
				//从工作流的输入获取选择值
				String paramValue = inputParam.getValue().toString();
				if (paramValue.contains("workflow")) {
					String replaceStr = paramValue.replace("${", "").replace("}", "");
					if (replaceStr.substring(0, replaceStr.indexOf(".")).equals("workflow")) {
						String param = replaceStr.substring(replaceStr.lastIndexOf(".") + 1);//截取"${workflow.input.param}",获取param
						paramArray.add(param);
					}
				}
			}
			//检索选择任务中的分支任务
			Map<String, List<WorkflowTask>> decisionCases = workflowTask.getDecisionCases();
			for(Map.Entry<String, List<WorkflowTask>> decisionCase : decisionCases.entrySet()){
				List<WorkflowTask> values = decisionCase.getValue();
				JSONArray varietyParams = getVarietyParams(values);
				paramArray.addAll(varietyParams);
			}
			
		return paramArray;
	}
	/**
	 * 获取分支任务中，需要的workflow参数
	 * @param forkTasks
	 * @param workflowId
	 * @return
	 */
	private JSONArray forkJoinParam(List<List<WorkflowTask>> forkTasks){
		JSONArray array = new JSONArray();
		for (List<WorkflowTask> forkTask : forkTasks) {
			JSONArray varietyParams = getVarietyParams(forkTask);
			array.addAll(varietyParams);
		}
		return array;
	}
	
	//===============workflowDef 私有方法============================//
	
	/**
	 * 检测模板名称和版本号的唯一性
	 * @param name
	 * @param version
	 * @return
	 */
	private boolean checkName(String name, Integer version) {
		try {
			Workflow result = workflowDao.checkName(name, version);
			if (result != null) {
				logger.warn("name["+name+"]/version["+version+"] is exist ");
				return false;
			}
			return true;
		} catch (Exception e1) {
			logger.error("check workflow name["+name+"]/version["+version+"] exception", e1);
			return false;
		}
	}
	
	/**
	 * 修改WorkflowDef
	 * @param workflowDef
	 * @return
	 */
	private Result updateWorkflowDefClient(WorkflowDef workflowDef){
		ExtendHttpClient httpClient = new ExtendHttpClient();
		JSONArray jsonArray = new JSONArray();
		try {
			//转换成json格式
			jsonArray.add(workflowDef);
			String jsonstr = JSON.toJSONString(jsonArray);
			StringEntity se = new StringEntity(jsonstr);
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			
			String url = HTTPS+workflowConfig.getWorkflowUrl()+":"+workflowConfig.getWorkflowPort()+API+"metadata/workflow";
			Map<String, Object> headers = MapTools.simpleMap("Content-Type","application/json");
			headers.put("Keep-Alive", "10");
			
			return httpClient.put(headers, null, url, se);
		} catch (UnsupportedEncodingException e) {
			logger.error("update task ["+workflowDef.getName()+"] exception");
			return new Result(false, "修改任务【"+workflowDef.getName()+"】异常");
		}
	}
	
	
	
	//================workflow 私有方法=============================//

	/**
	 * 按条件查询所有的流程编排
	 * @param name
	 * @param workflowType
	 * @param status
	 * @return
	 */
	private SearchResult<WorkflowSummary> allWorkflow(String workflowType, String version, String status){
		StringBuilder builder = new StringBuilder();
		
		//获取资源连接
		WorkflowClient client = getClient();
		
		//获取查询条件
		if (StringUtils.hasText(workflowType)) {
			builder.append("workflowType%20IN%20(").append(workflowType).append(")%20%20");
		}
		if (StringUtils.hasText(version)) {
			if (builder.length() != 0) {
				builder.append("AND");
			}
			builder.append("%20version%20IN%20(").append(version).append(")%20%20");
		}
		if (StringUtils.hasText(status)){
			if (builder.length() != 0) {
				builder.append("AND");
			}
			builder.append("%20status%20IN%20(").append(status).append(")%20");
		}
		
		//执行
		SearchResult<WorkflowSummary> failedAll = null;
		try {
			failedAll = client.search(builder.toString());
		} catch (Exception e) {
			logger.error("Get conductor network exception", e);
		}
		
		return failedAll;
	}

	/**
	 * 暂停 / 继续  http请求
	 * @param taskDef
	 * @return
	 */
	private boolean managerClient(String url){
		String[] split = url.split("/");
		try {
			url = HTTPS+workflowConfig.getWorkflowUrl()+":"+workflowConfig.getWorkflowPort()+API + "workflow/"+url;
			HttpResponse<com.mashape.unirest.http.JsonNode> response = Unirest.put(url).asJson();
			if (204 == response.getStatus() && "No Content".equals(response.getStatusText())) {
				return true;
			}
			logger.info(split[1]+" workflow ID ["+split[0]+"] is fail");
			return false;
		} catch (Exception e) {
			logger.info("conductor network connection is exception , pleace check connection");
			return false;
		}
	}
	
	/**
	 * 获取某工作流下的所有job
	 * @param workflowId
	 * @return
	 */
	private List<String> getJobs(String workflowId) {
		List<String> jobNames = new ArrayList<>();
		
		//获取workflow信息
		com.netflix.conductor.common.run.Workflow workflow = getWorkflow(workflowId);
		if (null != workflow) {
			String wf = workflow.getWorkflowType()+"_"+workflow.getVersion()+"_"+workflow.getWorkflowId();
			
			//获取wf的tasks
			List<Task> tasks = workflow.getTasks();
			for (Task task : tasks) {
				jobNames.add(wf+"_"+task.getTaskType()+"_"+task.getTaskId());
			}
		}
		return jobNames;
	}
	/**
	 * 获取workflow详情
	 * @param workflowId
	 * @return
	 */
	@Override
	public com.netflix.conductor.common.run.Workflow getWorkflow(String workflowId){
		//获取连接
		WorkflowClient client = getClient();
		com.netflix.conductor.common.run.Workflow workflow = null;
		try {
			workflow = client.getWorkflow(workflowId, true);
		} catch (Exception e) {
			logger.error("该服务器端不存在ID=["+workflowId+"]这样的工作流", e);
		}
		return workflow;
	}
	/**
	 * 删除job
	 * @param jobNames
	 */
	private boolean deleteJob(List<String> jobNames){
		int deleteClunt = 0;
		JenkinsClient jenkinsUtil = new JenkinsClient(jenkinsConfig.getUri(), jenkinsConfig.getUsername(), jenkinsConfig.getPassword());

		for (String jobName : jobNames) {
			
			//判断job是否存在
			if (null != jenkinsUtil.getJob(jobName)) {
				Boolean isDelete = jenkinsUtil.deleteJob(jobName);
				if (isDelete) {
					deleteClunt ++;
				}
			} else { //若job不存在      1是不用删除，2是插件本身可能不与jenkins有关
				deleteClunt ++;
			}
		}
		return deleteClunt == jobNames.size() ? true:false;
	}
	/**
	 * 删除workflow  执行删除操作后，该工作流还是存在，只是工作流里tasks不存在
	 * @param workflowId
	 * @return
	 */
	public boolean deleteWorkflow(String workflowId){
		try {
			String url = HTTPS+workflowConfig.getWorkflowUrl()+":"+workflowConfig.getWorkflowPort()+API + "workflow/"+workflowId+"/remove";
			HttpResponse<com.mashape.unirest.http.JsonNode> response = Unirest.delete(url).asJson();
			if (204 == response.getStatus() && "No Content".equals(response.getStatusText())) {
				return true;
			}
			logger.info("delete workflow ID ["+workflowId+"] is fail");
			return false;
		} catch (Exception e) {
			logger.info("network connection is exception , pleace check connection");
			return false;
		}
	}

	/**
	 * 根据状态获取相应的状态数
	 * @param status
	 * @return
	 */
	private Integer getNum(String status){
		WorkflowClient client = getClient();
		Integer num = 0;
		SearchResult<WorkflowSummary> failedAll = null;
		try {
			if (null == status) {
				failedAll = client.search(status);
			} else {
				failedAll = client.search("status%20IN%20("+status+")");
			}
		} catch (Exception e) {
			logger.error("Get conductor network exception", e);
		}
		if (null != failedAll) {
			if (failedAll.getTotalHits() != 0) {
				List<WorkflowSummary> failedResults = failedAll.getResults();
				
				//过滤插件workflow
				failedResults = filterWorkflow(failedResults);
				if (!ListTool.isEmpty(failedResults)) {
					num = failedResults.size();
				}
			}
		}
		return num;
	}
	/**
	 * 过滤 workflow (插件服务端存在的wf，而本地数据库不存在的)
	 * @param list
	 * @return
	 */
	private List<WorkflowSummary> filterWorkflow(List<WorkflowSummary> list){
		Iterator<WorkflowSummary> iterator = list.iterator();
		while(iterator.hasNext()){
			WorkflowSummary workflowSummary = iterator.next();
			//根据名称和版本号过滤
			boolean isExist = filter(workflowSummary.getWorkflowType(), workflowSummary.getVersion());
			if (isExist) {
				iterator.remove();
			}
		}
		return list;
	}

	/**
	 * workflowDef 格式校验
	 * @param data
	 * @param tasks
	 * @return
	 */
	private WorkflowDef checkFormate(JSONObject data, JSONArray tasks){
		JSONObject workflowObject = new JSONObject();
		WorkflowDef workflowDef = null;
		//数据整理
		workflowObject.put("name", data.getString("name"));
		workflowObject.put("version", Integer.valueOf(data.getString("version")));
		workflowObject.put("inputParameters", data.getJSONArray("inputParameters"));
		workflowObject.put("outputParameters", data.getJSONObject("outputParameters"));
		workflowObject.put("description", data.getString("description"));
		workflowObject.put("schemaVersion", 2);
		workflowObject.put("tasks", tasks);
		
		//格式校验
		workflowDef = JSONObject.parseObject(workflowObject.toJSONString(), WorkflowDef.class);
		
		if (null == workflowDef) {
			logger.warn("check workflowDef formate fail");
			return null;
		}
		return workflowDef;
	}

	@Override
	public BsmResult running(Long id, RequestUser user) {
		//从数据库获取工作流信息
		Workflow workflow = getWorkflow(id);
		if (null == workflow) {
			return new BsmResult(false, "数据库不存在这样的工作流信息");
		}
		
		//获取工作流模板元数据信息
		WorkflowDef workflowDef = JSONObject.parseObject(workflow.getWorkflowDef(), WorkflowDef.class);
		if (null == workflowDef) {
			logger.error("workflow in jdbc is null");
			return new BsmResult(false, "工作流模板信息为空");
		}
		
		//创建
		WorkflowClient client = getClient();
		try {
			client.registerWorkflow(workflowDef);
		} catch (Exception ex) {
			logger.error("create workflow exception in conductor, pleace check conductor network", ex);
			return new BsmResult(false, "服务端注册工作流失败，请检测服务资源连接");
		}
		//重启
		BsmResult result = start(workflowDef.getName(), workflowDef.getVersion(), String.valueOf(user.getId()), null);
		if (result.isFailed()) {
			logger.error("start workflow fail in conductor, pleace check conductor network connecton");
			return new BsmResult(false, "服务端启动工作流失败，请检测服务资源连接");
		}
		logger.info(result.getMessage());
		return new BsmResult(true, "运行成功");
	}

}
