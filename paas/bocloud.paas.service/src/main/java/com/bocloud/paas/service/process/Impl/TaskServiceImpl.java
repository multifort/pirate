package com.bocloud.paas.service.process.Impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.RequestUser;
import com.bocloud.common.utils.DateTools;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.dao.process.TaskDao;
import com.bocloud.paas.entity.BuildModel;
import com.bocloud.paas.entity.Task;
import com.bocloud.paas.entity.TaskModelDef;
import com.bocloud.paas.service.event.EventPublisher;
import com.bocloud.paas.service.process.TaskService;
import com.bocloud.paas.service.process.WorkflowService;
import com.bocloud.paas.service.process.config.JenkinsConfig;
import com.bocloud.paas.service.process.config.WorkflowConfig;
import com.bocloud.paas.service.process.util.JenkinsClient;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.run.Workflow;
import com.offbytwo.jenkins.model.Build;
/**
 * @Describe: 编排流程任务 业务层实现
 * @author Zaney
 * @Date 2017年6月16日
 */
@Service("taskService")
public class TaskServiceImpl implements TaskService {
	private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);
	private static final String HTTPS = "http://";
	private static final String API = "/api/";
	private static Map<String, String> map = new HashMap<>();
	@Autowired
	private WorkflowConfig workflowConfig;
	@Autowired
	private JenkinsConfig jenkinsConfig;
	@Autowired
	private TaskDao taskDao;
	@Autowired
	protected EventPublisher eventPublisher;
	@Autowired
	private WorkflowService workflowService;
	
	static {
		map.put("WAIT", "wait_task.yaml");
		map.put("EVENT", "event_task.yaml");
		map.put("DECISION", "decision_task.yaml");
		map.put("FORK_JOIN", "fork_join_task.yaml");
		map.put("FORK_JOIN_DYNAMIC", "fork_join_dynamic_task.yaml");
		map.put("JOIN", "join_task.yaml");
		map.put("joinOn", "join_on_task.yaml");
		map.put("SUB_WORKFLOW", "sub_workflow_task.yaml");
		map.put("HTTP", "http_task.yaml");
		map.put("SIMPLE", "simple_task.yaml");
		map.put("DYNAMIC", "dynamic_task.yaml");
	}
	
	private TaskClient getClient(){
		TaskClient taskClient = new TaskClient();
		taskClient.setRootURI(HTTPS+workflowConfig.getWorkflowUrl()+":"+workflowConfig.getWorkflowPort()+API);
		return taskClient;
	}

	@Override
	public BsmResult taskDefs() {
		JSONArray array = new JSONArray();
		try {
			TaskClient client = getClient();
			List<TaskDef> taskDefs = client.getTaskDef();
			for (TaskDef taskDef : taskDefs) {
				TaskModelDef taskModel = new TaskModelDef();
				BeanUtils.copyProperties(taskDef, taskModel);
				//TODO 循环次数太多
				Task task = taskDao.selectTask(taskDef.getName());
				if (task != null) {
					if (taskDef.getCreateTime() != null) {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date = new Date(taskDef.getCreateTime());
						taskModel.setCreateDate(simpleDateFormat.format(date));
					}
					array.add(taskModel);
				}
			}
			return new BsmResult(true, array, "获取成功");
		} catch (Exception e) {
			logger.error("获取所有的任务异常",e);
			return new BsmResult(false, "获取所有的任务异常");
		}
	}
	
	@Override
	public BsmResult task(String taskId) {
		try {
			TaskClient client = getClient();
			com.netflix.conductor.common.metadata.tasks.Task task = client.get(taskId);
			 if (null == task) {
				 return new BsmResult(false, "获取任务ID为【"+taskId+"】信息为空");
			}
			JSONObject taskJson = (JSONObject) JSON.toJSON(task);
			return new BsmResult(true, taskJson, "获取任务ID为【"+taskId+"】信息成功");
		} catch (Exception e) {
			logger.error("获取任务ID为【"+taskId+"】信息异常",e);
			return new BsmResult(false, "获取任务ID为【"+taskId+"】信息异常");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public BsmResult getTaskOuputParam(String taskName) {
		JSONArray params = new JSONArray();
		String filePath = FileUtil.filePath("correlation_table");
		String fileContent = FileUtil.readFile(filePath+"task_param_table.yaml");
		if (!StringUtils.hasText(fileContent)) {
			return new BsmResult(false, "获取参数模板失败");
		}
		JSONArray data = JSONObject.parseArray(fileContent);
		for (Object obj  : data) {
			Map<String, JSONArray> paramsMap = JSONObject.parseObject(obj.toString(), Map.class);
			for (Map.Entry<String, JSONArray> entry : paramsMap.entrySet()) {
				if (entry.getKey().equals(taskName)) {
					params = entry.getValue();
				}
			}
		}
		return new BsmResult(true, params, "获取成功");
	}

	@Override
	public BsmResult buildRecord(String workflowId, String taskId) {
		JSONArray json = new JSONArray();
		JSONObject object = new JSONObject();
		
		//获取jobName
		String jobName = jobName(workflowId, taskId);
		
		if (StringUtils.hasText(jobName)) {
			//获取job 构建信息
			List<BuildModel> jobBuild = getJobBuild(jobName);
			json =(JSONArray) JSONArray.toJSON(jobBuild);
		} 
		
		object.put("jobName", jobName);
		object.put("buildModels", json);
		return new BsmResult(true, object, "获取成功");
	}
	/**
	 * 获取job 构建信息
	 * @param jobName
	 * @return
	 */
	private List<BuildModel> getJobBuild(String jobName){
		
		//获取jenkins资源连接
		JenkinsClient jenkinsClient = new JenkinsClient(jenkinsConfig.getUri(), jenkinsConfig.getUsername(), jenkinsConfig.getPassword());
		
		//获取job所有构建信息
		List<Build> builds = jenkinsClient.getAllBuilds(jobName);
		
		List<BuildModel> buildModels = new ArrayList<BuildModel>();
		if (!ListTool.isEmpty(builds)) {
			for (Build build : builds) {
				BuildModel buildModel = new BuildModel();
				BeanUtils.copyProperties(build, buildModel);
				try {
					String createTime = DateTools.Timestamp2DateTime(build.details().getTimestamp());
				    buildModel.setCreateTime(createTime);
				} catch (Exception e) {
					logger.error("time transform is exception");
					continue;
				}
				buildModels.add(buildModel);
			}
		}
		return buildModels;
	}
	/**
	 * 获取jobName
	 * @param workflowId
	 * @param taskId
	 * @return
	 */
	private String jobName(String workflowId, String taskId){
		String jobName = null;
		Workflow workflow = workflowService.getWorkflow(workflowId);
		if (null != workflow) {
			List<com.netflix.conductor.common.metadata.tasks.Task> tasks = workflow.getTasks();
			for (com.netflix.conductor.common.metadata.tasks.Task task : tasks) {
				if (taskId.equals(task.getTaskId())) {
					jobName = workflow.getWorkflowType() + "_" + workflow.getVersion() + "_" + task.getWorkflowInstanceId()
									+ "_" + task.getReferenceTaskName();
				}
			}
		}
		return jobName;
	}

	@Override
	public BsmResult getJobBuildOutput(String jobName, int buildNum) {
		JenkinsClient jenkinsClient = new JenkinsClient(jenkinsConfig.getUri(), jenkinsConfig.getUsername(), jenkinsConfig.getPassword());

		String output = jenkinsClient.getBuildOutput(jobName, buildNum);
		if ("" != output) {
			output = output.replace("\r\n", "\\r\\n");
		}
		return new BsmResult(true, output, "成功获取job["+jobName+"/"+buildNum+"]输出信息");
	}
	
	@Override
	public BsmResult template(JSONObject object, RequestUser requestUser) {
		String[] args = new String[]{"SIMPLE", "HTTP", "DYNAMIC"};
		
		//获取模板内容
		String filePath = FileUtil.filePath("task_template") + map.get(object.getString("type"));
		File file = new File(filePath);
		String dataStr = FileUtil.readParameters(file);
		if (!StringUtils.hasText(dataStr)) {
			return new BsmResult(false, "文件内容为空");
		}
		
		//替换模板数据
		if (Arrays.asList(args).contains(object.getString("type"))) {
			if (null == chaNames()) {
				return new BsmResult(false, "没有任务存在，请先创建任务");
			}
			dataStr = dataStr.replace("\"${tasks}\"", chaNames().toJSONString());
		}
		if ("SUB_WORKFLOW".equals(object.getString("type"))) {
			if (ListTool.isEmpty(getWorkflowDefs(requestUser))) {
				return new BsmResult(false, "没有工作流存在，请先创建工作流");
			}
			dataStr = dataStr.replace("\"${workflowDefs}\"", getWorkflowDefs(requestUser).toJSONString());
		}
		
		return new BsmResult(true, JSONObject.parseObject(dataStr), "读取模板成功");
	}
	
	@Override
	public BsmResult getTaskParam(String taskName) {
		JSONArray resultArray = new JSONArray();
		String templateAttribute = null;
		
		//获取插件的参数模板文件
		String fileName = pluginParamFile(taskName);
		if (StringUtils.hasText(fileName)) {
			//文件名参数说明
			JSONObject propertiesFileName = new JSONObject();//参数模板追加jobName属性
			propertiesFileName.put("name", "fileName");
			propertiesFileName.put("displayName", "配置文件名称");
			propertiesFileName.put("type", "hidden");
			propertiesFileName.put("description", "插件配置文件名称");
			propertiesFileName.put("select", fileName);
			propertiesFileName.put("value", "");
			resultArray.add(propertiesFileName);
			//获取模板内容
			templateAttribute = FileUtil.readParameters(new File(FileUtil.filePath("plugin") + fileName));
		}
		
		if (StringUtils.hasText(templateAttribute)) {
			JSONObject jsonObject = JSONObject.parseObject(JSON.toJSON(templateAttribute).toString());
			JSONArray jsonArray = JSONObject.parseArray(jsonObject.getString("parameters"));
			resultArray.addAll(jsonArray);
		}
		return new BsmResult(true, resultArray, "获取成功");
	}
	
	/**
	 * 获取插件参数文件名
	 * @param taskName
	 * @return
	 */
	private String pluginParamFile(String taskName){
		com.bocloud.paas.entity.Task task = null;
		try {
			task = taskDao.selectTask(taskName);
			if (null == task) {
				logger.error("plungin information is null, plugin name is :" + taskName);
				return null;
			}
			return task.getFileName();
		} catch (Exception e) {
			logger.error("get plungin information is exception, plugin name is :" + taskName);
			return null;
		}
	}

	/**
	 * 获取所有模板信息
	 * @return
	 */
	private JSONArray getWorkflowDefs (RequestUser requestUser){
		JSONArray array = new JSONArray();
		
		BsmResult result = workflowService.listWorkflowDef(0, 0, null, null, true, requestUser);
		JSONObject json = JSONObject.parseObject(result.getData().toString());
		JSONArray rows = json.getJSONArray("rows");
		if (!ListTool.isEmpty(rows)) {
			for (Object row : rows) {
				JSONObject object = JSONObject.parseObject(row.toString());
				array.add(object.get("name"));
			}
		}
		return array;
	}
	
	/**
	 * 获取插件中文名
	 * @return
	 */
	private JSONObject chaNames(){
		JSONObject chNames = new JSONObject();
		try {
			TaskClient client = getClient();
			List<TaskDef> taskDefs = client.getTaskDef();
			for (TaskDef taskDef : taskDefs) {
				Task task = taskDao.selectTask(taskDef.getName());
				if (task != null) {
					chNames.put(task.getName(), task.getChName());
				}
			}
		} catch (Exception e) {
			logger.error("get plugin information exception!",e);
		}
		return chNames;
	}

}
