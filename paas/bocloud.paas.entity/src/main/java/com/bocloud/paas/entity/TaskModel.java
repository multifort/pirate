package com.bocloud.paas.entity;

/**
 * @Describe: 任务列表展示类
 * @author Zaney
 * @Date 2017年6月18日
 */
public class TaskModel extends com.netflix.conductor.common.metadata.tasks.Task {
	
	private String startDate;
	private String endDate;
	private String updateDate;
	private String scheduledDate;
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public String getScheduledDate() {
		return scheduledDate;
	}
	public void setScheduledDate(String scheduledDate) {
		this.scheduledDate = scheduledDate;
	}
	
}
