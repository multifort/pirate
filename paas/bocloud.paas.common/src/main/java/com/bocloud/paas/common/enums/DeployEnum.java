package com.bocloud.paas.common.enums;

public enum DeployEnum {
	
	DEPLOYMENT("Deployment"), STATEFULSET("StatefulSet"), JOB("Job");
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private DeployEnum(String name) {
		this.name = name;
	}
	
	
	

}
