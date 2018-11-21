package com.bocloud.paas.service.application.model;

/**
 * openshift pod的资源信息
 * 
 * @author zjm
 * @date 2017年4月13日
 */
public class PodInfo {

	private String cpuRequest;
	private String cpuLimit;
	private String memoryRequest;
	private String memoryLimit;

	public String getCpuRequest() {
		return cpuRequest;
	}

	public void setCpuRequest(String cpuRequest) {
		this.cpuRequest = cpuRequest;
	}

	public String getCpuLimit() {
		return cpuLimit;
	}

	public void setCpuLimit(String cpuLimit) {
		this.cpuLimit = cpuLimit;
	}

	public String getMemoryRequest() {
		return memoryRequest;
	}

	public void setMemoryRequest(String memoryRequest) {
		this.memoryRequest = memoryRequest;
	}

	public String getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(String memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

}
