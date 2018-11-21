package com.bocloud.paas.service.process.init;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.bocloud.common.utils.ListTool;
import com.bocloud.paas.dao.process.TaskDao;
import com.bocloud.paas.entity.Task;
import com.bocloud.paas.service.process.config.JenkinsConfig;
import com.bocloud.paas.service.process.config.WorkflowConfig;
import com.bocloud.paas.service.process.worker.GeneralWorker;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.task.WorkflowTaskCoordinator;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
/**
 * describe:初始化插件信息
 * @author Zaney
 * @data 2017年7月25日
 */
@Repository("taskInit")
public class TaskInit {
	
	private static Logger logger = LoggerFactory.getLogger(TaskInit.class);
	
	private TaskClient client;
	
	@Autowired
	private WorkflowConfig workflowConfig;
	@Autowired
	private JenkinsConfig jenkinsConfig;
	@Autowired
	private TaskDao taskDao;
	
	
	/**
	 * 初始化插件信息，并启动worker线程池
	 */
	//@PostConstruct
	public void onStart() {
		start();
	}
	public void start(){
		String host = workflowConfig.getWorkflowUrl();
		String port = workflowConfig.getWorkflowPort();
		logger.info("["+host+":"+port+"] init workers,create workers thread pool......");
		executeWorkers(host, port);
	}
	/**
	 * 创建worker线程池
	 * @param host
	 * @param port
	 * @param taskDefList
	 */
	private void executeWorkers(String host, String port){
		List<Worker> workers = new ArrayList<Worker>();
		
		//获取连接
		getClient(host, port);
		
		//获取数据库插件信息
		List<TaskDef> taskDefs = getTasks();
		if (ListTool.isEmpty(taskDefs)) {
			return;
		}
		
		//将数据库插件信息注册到conductor里
		if (!registerTaskDefs(taskDefs)) {
			return;
		}
		
		//将插件注册到线程池里
		for (TaskDef taskDef : taskDefs) {
			workers.add(new GeneralWorker(taskDef.getName(), workflowConfig, jenkinsConfig));
		}
		
		//创建线程池
		if (0 != workers.size()) {
			logger.info("execute workers ......");
			WorkflowTaskCoordinator workflowTaskCoordinator = new WorkflowTaskCoordinator(null, this.client, workers.size(), workers);
			workflowTaskCoordinator.init();
		} else {
			logger.warn("custom define plugin is null, please add plugin...");
		}
	}
	/**
	 * 获取连接
	 * @param host
	 * @param port
	 * @param taskDefList
	 */
	private void getClient(String host, String port){
		this.client = new TaskClient();
		this.client.setRootURI("http://"+host+":"+port+"/api/");
	}
	/**
	 * 构建conductor初始化数据
	 * @return
	 */
//	private static List<String> pluginInitData(){
//		List<String> taskDefs = new ArrayList<>();
//		for(int i = 0; i < 40; i++) {
//			taskDefs.add("task_" + i);
//		}
//		taskDefs.add("search_elasticsearch");
//		return taskDefs;
//	}
	/**
	 * 在服务端注册插件信息
	 * @param taskDefs
	 * @return
	 */
	private boolean registerTaskDefs(List<TaskDef> taskDefs) {
		try {
			client.registerTaskDefs(taskDefs);
			logger.info("conductor 服务端注册插件成功");
			return true;
		} catch (Exception e) {
			logger.error("在服务端注册插件异常", e);
		}
		return false;
	}
	
	/**
	 * 获取插件信息
	 * @return
	 */
	private List<TaskDef> getTasks(){
		List<TaskDef> taskDefs = new ArrayList<>();
		try {
			List<Task> tasks = taskDao.select();
			if (ListTool.isEmpty(tasks)) {
				logger.warn("数据库插件初始化数据为空");
				return null;
			}
			
			for (Task task : tasks) {
				taskDefs.add(new TaskDef(task.getName(), task.getChName(), 1, 60));
			}
			return taskDefs;
		} catch (Exception e) {
			logger.error("获取数据库插件信息异常", e);
			return null;
		}
	}
	
}
