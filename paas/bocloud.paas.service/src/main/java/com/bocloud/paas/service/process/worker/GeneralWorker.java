package com.bocloud.paas.service.process.worker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bocloud.paas.common.util.FileUtil;
import com.bocloud.paas.service.process.config.JenkinsConfig;
import com.bocloud.paas.service.process.config.WorkflowConfig;
import com.bocloud.paas.service.process.util.JenkinsClient;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.TaskResult.Status;
import com.netflix.conductor.common.run.Workflow;
import com.offbytwo.jenkins.model.Job;
/**
 * @describe: 任务worker
 * @author: zaney
 * @date: 2017年7月10日
 */
public class GeneralWorker implements Worker {

	private static Logger logger = LoggerFactory.getLogger(GeneralWorker.class);
	private String taskDefName;
	private WorkflowConfig workflowConfig;
	private JenkinsConfig jenkinsConfig;
	
	private final static String GIT_CLONE = "git_clone";
	private final static String CHECK_OUT = "check_out";
	private final static String BUILD_IMAGE = "build_image";
	private final static String PLUGIN_FILE_PATH = "plugin";
	private final static String OUT_PUT = "output";

	public GeneralWorker(String taskDefName, WorkflowConfig workflowConfig, JenkinsConfig jenkinsConfig){
		this.taskDefName = taskDefName;
		this.workflowConfig = workflowConfig;
		this.jenkinsConfig = jenkinsConfig;
	}
	
	
	@Override
	public String getTaskDefName() {
		return taskDefName;
	}

	@Override
	public TaskResult execute(Task task) {
		//参数处理
		logger.info("Executing...task [" + task.getTaskDefName() + "]");
		TaskResult taskResult = new TaskResult(task);

		Map<String, Object> inputData = task.getInputData();
		WorkflowClient client = getClient(workflowConfig);
		Workflow workflow = client.getWorkflow(task.getWorkflowInstanceId(), false);
		
		//jobName由工作流名称_版本号_ID_任务名称_任务ID
		String jobName = workflow.getWorkflowType() + "_" + workflow.getVersion() + "_" + task.getWorkflowInstanceId()
				+ "_" + task.getReferenceTaskName();
		
		boolean successed = buildJob(inputData, jobName);
		
		if (successed) {//没有信息返回，说明成功
			taskResult = setOutputParam(task.getTaskDefName(), taskResult, jobName);//设置输出参数
			taskResult.setStatus(Status.COMPLETED);
		} else {
			taskResult.setStatus(Status.FAILED);
			taskResult.setReasonForIncompletion("create or build job failed");
		}

		return taskResult;
	}
	/**
	 * 创建和构建job
	 * @param inputParam   输入参数
	 * @param fileDirPath  文件目录路径
	 * @param fileName    文件名
	 * @param jobName    job名
	 * @param params     构建job需要的参数
	 * @return
	 */
	private boolean buildJob(Map<String, Object> params, String name){
		
		String filePath = FileUtil.filePath(PLUGIN_FILE_PATH);
		String fileName = (String) params.get("fileName");
		//创建job
		Map<String, String> jobParam = null;//获取git clone时创建的jobName
		if (params.containsKey(OUT_PUT)) {
			jobParam = new HashMap<String, String>();
			jobParam.put(OUT_PUT, (String) params.get(OUT_PUT));
		}
		
		JenkinsClient client = new JenkinsClient(jenkinsConfig.getUri(), jenkinsConfig.getUsername(), jenkinsConfig.getPassword());
		
		boolean exsited = client.existed(name);
		boolean successed = false;
		if(exsited){
			successed = client.updateJob(filePath, fileName, name, params) && client.buildJob(name, jobParam);
		}else{
			successed = client.createJob(filePath, fileName, name, params) && client.buildJob(name, jobParam);
		}
		
//		if (successed) {
//			return successed;
//		} else {
//			client.deleteJob(name);
//			return successed;
//		}
		return successed;
	}
	
	/**
	 * 根据不同的任务类型输出参数
	 * @param type
	 * @param inputData
	 * @return
	 */
	private TaskResult setOutputParam(String type, TaskResult taskResult, String jobName){
		
		JenkinsClient jenkinsClient = new JenkinsClient(jenkinsConfig.getUri(), jenkinsConfig.getUsername(), jenkinsConfig.getPassword());
		switch (type) {
			case GIT_CLONE:
				taskResult.getOutputData().put(OUT_PUT, jobName);
				break;
			case CHECK_OUT:
				taskResult.getOutputData().put(OUT_PUT, jobName);
				break;
			case BUILD_IMAGE:
				String buildFullDisplayName = "";
				String imageName = "";
				try {
					Job job = jenkinsClient.getJob(jobName);
					if (null != job) {
						buildFullDisplayName = job.details().getLastBuild().details().getFullDisplayName();
						imageName = buildFullDisplayName.substring(buildFullDisplayName.lastIndexOf(" ") + 1);
					}
				} catch (IOException e) {
					logger.error("get job [" + jobName + "] last build info error: \n", e);
				} 
				taskResult.getOutputData().put(OUT_PUT, imageName);
				break;
		}
		return taskResult;
	}
	
	/**
	 * 获取conductor连接
	 * @param workflowConnfig
	 */
	private WorkflowClient getClient(WorkflowConfig workflowConnfig){
		WorkflowClient client = new WorkflowClient();
		client.setRootURI("http://"+workflowConnfig.getWorkflowUrl()+":"+workflowConnfig.getWorkflowPort()+"/api/");
		return client;
	}
	
}
