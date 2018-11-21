package com.bocloud.paas.service.process.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 * @Describe: workflow 配置文件
 * @author Zaney
 * @2017年6月15日
 */
@Component("workflowConfig")
public class WorkflowConfig {
	@Value("${workflow.url}")
	private String workflowUrl; //流程编排路径
	@Value("${workflow.port}")
	private String workflowPort; //流程编排端口号
	
	public String getWorkflowUrl() {
		return workflowUrl;
	}
	public void setWorkflowUrl(String workflowUrl) {
		this.workflowUrl = workflowUrl;
	}
	public String getWorkflowPort() {
		return workflowPort;
	}
	public void setWorkflowPort(String workflowPort) {
		this.workflowPort = workflowPort;
	}

}
