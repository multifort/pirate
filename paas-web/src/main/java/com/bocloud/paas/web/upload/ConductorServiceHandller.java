package com.bocloud.paas.web.upload;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.Task.Status;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.bocloud.common.utils.ListTool;

@Component
public class ConductorServiceHandller implements WebSocketHandler {

    private static Logger logger = Logger.getLogger(ConductorServiceHandller.class);
    
    private static final String HTTPS = "http://";
	private static final String API = "/api/";	
	
	@Value("${workflow.url}")
	private String conductorUrl;
	@Value("${workflow.port}")
	private String conductorPort;
	
	private WorkflowDef workflowDef;
	
	private boolean isExist = false;
	
	private int tasks = 0;

    private static final String CANCEL = "cancel";
    private static final String CONTINUE = "continue";
    private static final String END = "end";

    private static List<WebSocketSession> currentUsers;

    /**
     * @return the currentUsers
     */
    public static List<WebSocketSession> getCurrentUsers() {
        return currentUsers;
    }

    /**
     * @param currentUsers the currentUsers to set
     */
    public static void setCurrentUsers(List<WebSocketSession> currentUsers) {
        ConductorServiceHandller.currentUsers = currentUsers;
    }

    static {
        ConductorServiceHandller.setCurrentUsers(new ArrayList<>());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ConductorServiceHandller.getCurrentUsers().add(session);
    }
    
    /**
   	 * 获取资源连接
   	 * @return
   	 */
   	private WorkflowClient getClient(){
   		WorkflowClient workflowClient = new WorkflowClient();
   		workflowClient.setRootURI(HTTPS+conductorUrl+":"+conductorPort+API);
   		return workflowClient;
   	} 

    @Override
    public void handleMessage(WebSocketSession session,
            WebSocketMessage<?> message) throws Exception {
    	
        if (message instanceof TextMessage) {
            handleText(session, (TextMessage) message);
        } else {
            logger.error("Unexpected WebSocket message type: " + message);
            throw new IllegalStateException("Unexpected WebSocket message type: " + message);
        }
    }

    /**
     * 上传并处理字符串
     *
     * @author zjm
     * @date 2017年3月17日
     */
    private void handleText(WebSocketSession session, TextMessage message) {
    	ConductorMessage conductorMessage = new ConductorMessage();
    	try {
			ConductorBean conductorBean = JSONObject.parseObject(message.getPayload(), ConductorBean.class);
			
			//判断是否获取过WorkflowDef的值
			if (!this.isExist) {
				getWorkflowDef(conductorBean.getName(), Integer.valueOf(conductorBean.getVersion()));
			}
			if (this.tasks == 0) {
				logger.warn("工作流模板中不存在插件任务");
			 session.sendMessage(new TextMessage(JSONObject.toJSONString(new ConductorMessage(CANCEL, null, null))));
			}
   
			//获取有状态工作流信息
			Workflow workflow = getWorkflow(conductorBean.getWorkflowId());
			 if (null == workflow) {
				 logger.warn("未获取到工作流信息, 工作流ID：" +conductorBean.getWorkflowId());
				 session.sendMessage(new TextMessage(JSONObject.toJSONString(new ConductorMessage(CANCEL, null, null))));
			 }
			 
			//获取有状态工作流插件任务信息
			 List<com.netflix.conductor.common.metadata.tasks.Task> tasks = new ArrayList<>();
			 tasks = workflow.getTasks();
			 Set<String> set = new HashSet<String>();//重试或其他操作，会导致出现重复的插件信息，需要过滤，不然判断会有问题
			 if (ListTool.isEmpty(tasks)) {
			     logger.warn("有状态工作流中不存在插件任务");
				 session.sendMessage(new TextMessage(JSONObject.toJSONString(new ConductorMessage(CANCEL, null, null))));
			 }else {
				 for (Task task : tasks) {
					 set.add(task.getReferenceTaskName());
				}
			 }
			 //时间转换
			 JSONArray arrayTasks = new JSONArray();
			 arrayTasks = statusTasks(tasks);
			 
			 //根据不同的状态来给前端发送信号
			 Status status = tasks.get(tasks.size()-1).getStatus();
			 if (Status.IN_PROGRESS.equals(status) || Status.SCHEDULED.equals(status)) {
				 conductorMessage.setContext(CONTINUE);
				 conductorMessage.setTasks(arrayTasks);
				 conductorMessage.setWFjson((JSONObject) JSONObject.toJSON(workflow));
				 session.sendMessage(new TextMessage(JSONObject.toJSONString(conductorMessage)));
			 } else if (Status.COMPLETED.equals(status)) {
				 if (tasks.size() == this.tasks) {
					 logger.info("工作流完毕");
					 conductorMessage.setContext(END);
					 conductorMessage.setTasks(arrayTasks);
					 conductorMessage.setWFjson((JSONObject) JSONObject.toJSON(workflow));
					 session.sendMessage(new TextMessage(JSONObject.toJSONString(conductorMessage)));
				 }
				 conductorMessage.setContext(CONTINUE);
				 conductorMessage.setTasks(arrayTasks);
				 conductorMessage.setWFjson((JSONObject) JSONObject.toJSON(workflow));
				 session.sendMessage(new TextMessage(JSONObject.toJSONString(conductorMessage)));
			 } else {//失败、超时状态
				 conductorMessage.setContext(END);
				 conductorMessage.setTasks(arrayTasks);
				 conductorMessage.setWFjson((JSONObject) JSONObject.toJSON(workflow));
				 session.sendMessage(new TextMessage(JSONObject.toJSONString(conductorMessage))); 
			 }
			 
		} catch (NumberFormatException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		 
    }
    
	/**
	 * 获取工作流启动后，有状态任务
	 * @param tasks
	 * @return
	 */
	private JSONArray statusTasks(List<com.netflix.conductor.common.metadata.tasks.Task> tasks){
		JSONArray jsonArray = new JSONArray();
		try {
			for (com.netflix.conductor.common.metadata.tasks.Task task : tasks) {
				JSONObject object = JSONObject.parseObject(JSONObject.toJSONString(task));
				object.put("startDate", transformTime(task.getStartTime()));
				object.put("endDate", transformTime(task.getEndTime()));
				object.put("updateDate", transformTime(task.getUpdateTime()));
				object.put("scheduledDate", transformTime(task.getScheduledTime()));
				 jsonArray.add(object);
			}
		} catch (Exception e) {
			logger.error("date format exception ", e);
		}
		return jsonArray;
	}
    
	/**
	 * long 转换Date 
	 * @param time
	 * @return
	 */
	private String transformTime(Long time){
		if (0 == time) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date scheduledDate = new Date(time);
		return simpleDateFormat.format(scheduledDate);
	}
	
    /**
     * 获取有状态的工作流信息
     * @param workflowId
     * @return
     */
    private com.netflix.conductor.common.run.Workflow getWorkflow(String workflowId){
		//获取连接
		WorkflowClient client = getClient();
		com.netflix.conductor.common.run.Workflow workflow = null;
		try {
			workflow = client.getWorkflow(workflowId, true);
		} catch (Exception e) {
			logger.error("Get conductor connection exception ", e);
		}
		return workflow;
	}
    
    /**
     * 获取工作流模板信息
     * @param name
     * @param version
     */
	private void getWorkflowDef(String name, Integer version){
		int count = 0 ;
		//获取连接
		WorkflowClient client = getClient();
		try {
			this.workflowDef = client.getWorkflowDef(name, version);
			if (null != this.workflowDef) {
				LinkedList<WorkflowTask> workflowTasks = this.workflowDef.getTasks();
				for (WorkflowTask workflowTask : workflowTasks) {
					List<List<WorkflowTask>> forkTasks = workflowTask.getForkTasks();
					Map<String, List<WorkflowTask>> decisionCaseMap = workflowTask.getDecisionCases();
					
					//处理计算
					if (forkTasks.size() > 0) {
						count += countForkTask(forkTasks) + 1;
					} else if (!decisionCaseMap.isEmpty()) {
						count += countDecisionCases(decisionCaseMap) + 1;
					} else if (decisionCaseMap.isEmpty() && forkTasks.size() == 0) {
						count++;
					}
				}
				this.tasks = count;
			}
		} catch (Exception e) {
			logger.error("Get conductor connection exception ", e);
		}
	}
	
	/**
	 * 计算选择流里的子任务数量
	 * @param decisionCaseMap
	 * @return
	 */
	private int countDecisionCases(Map<String, List<WorkflowTask>> decisionCaseMap){
		int count = 0;
		
		for (Map.Entry<String, List<WorkflowTask>> entrySet : decisionCaseMap.entrySet()) {
			List<WorkflowTask> tasksList = entrySet.getValue();
			count += tasksList.size();
		}
		
		return count;
	}
	
	/**
	 * 处理分支合并任务里的子任务数量
	 * @param forkTasks
	 * @return
	 */
	private int countForkTask(List<List<WorkflowTask>> forkTasks){
		int count = 0;
		
		for (List<WorkflowTask> tasksList : forkTasks) {
			count += tasksList.size();
		}
		return count;
	}

    @Override
    public void afterConnectionClosed(WebSocketSession session,
            CloseStatus closeStatus) throws Exception {
        ConductorServiceHandller.getCurrentUsers().remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session,
            Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        ConductorServiceHandller.getCurrentUsers().remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
	//获取整个工作流下多类型的所有任务
//	public List<String> getVarietyTask(List<WorkflowTask> tasks, String workflowId){
//		List<String> array = new ArrayList<String>();
//		for (WorkflowTask workflowTask : tasks) {
//			String taskDefType = workflowTask.getType();
//			if ("SIMPLE".equals(taskDefType)) {
//				String simpleTask = simpleTask(workflowTask);
//				if (StringUtils.hasText(simpleTask)) {
//					array.add(simpleTask);
//				}
//			}
//			if ("DECISION".equals(taskDefType)) {
//				 List<String> decisionTasks = decisionTask(workflowTask, workflowId);
//				 BeanUtils.copyProperties(decisionTasks, array);
//			}
//			if ("DYNAMIC".equals(taskDefType)) {
//				String dynamicTask = dynamicTask(workflowTask, workflowId);
//				if (StringUtils.hasText(dynamicTask)) {
//					array.add(dynamicTask);
//				}
//			}
//		}
//		return array;
//	}
//	//DYNAMIC类型的任务
//	public String dynamicTask(WorkflowTask workflowTask, String workflowId){
//		String value = "";
//		boolean flag = false;
//		//获取该workflowId的工作流
//		WorkflowClient client = getClient();
//		com.netflix.conductor.common.run.Workflow workflow = client.getWorkflow(workflowId, false);
//		Map<String, Object> inputMap = workflow.getInput();//获取工作流的输入参数
//		//截取动态任务参数
//		Map<String, Object> inputParameters = workflowTask.getInputParameters();
//		for (Map.Entry<String, Object> entry : inputParameters.entrySet()) {
//			if (workflowTask.getDynamicTaskNameParam().equals(entry.getKey())) {
//				String param = entry.getValue().toString().replace("${", "").replace("}", "").split("\\.")[2];//截取"${workflow.input.param}",获取param
//				for(Map.Entry<String, Object> entryMap : inputMap.entrySet()){//循环工作流的输入参数，如果param 与 工作流的某个参数一致，
//																			//那么对应的值为这个动态task需要调度的任务名
//					if (param.equals(entryMap.getKey())) {
//						value =(String) entryMap.getValue();
//						entryMap.getValue();
//						flag = true;
//						break;
//					}
//				}
//			}
//			if (flag == true) {
//				break;
//			}
//		}
//		if (!StringUtils.hasText(value)) {
//			logger.warn("动态任务的动态参数名书写有误，请认真检查!!!");
//		}
//		return value;
//	}
//	//SIMPLE类型的任务
//	public String simpleTask(WorkflowTask workflowTask){
//		Task task = null;
//		try {
//			task = taskDao.checkName(workflowTask.getTaskReferenceName());
//		} catch (Exception e) {
//			logger.warn("数据库获取名为【"+workflowTask.getTaskReferenceName()+"】的任务信息异常");
//			return null;
//		}
//		if (task == null) {
//			logger.warn("数据库不存在【"+workflowTask.getTaskReferenceName()+"】这样的任务");
//			return null;
//		}
//		return workflowTask.getTaskReferenceName();
//	}
//	//DECISION类型的选择任务
//	public List<String> decisionTask(WorkflowTask workflowTask, String workflowId){
//		List<String> arrayList = new ArrayList<String>();
//		Map<String, List<WorkflowTask>> decisionCases = workflowTask.getDecisionCases();
//		for (Map.Entry<String, List<WorkflowTask>> decisionCase : decisionCases.entrySet()) {
//			List<WorkflowTask> workflowTasks = decisionCase.getValue();
//			List<String> simpleTask = getVarietyTask(workflowTasks, workflowId);
//			BeanUtils.copyProperties(simpleTask, arrayList);
//		}
//		return arrayList;
//	}
}
