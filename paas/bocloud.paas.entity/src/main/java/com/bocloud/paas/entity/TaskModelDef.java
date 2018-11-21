package com.bocloud.paas.entity;

import com.netflix.conductor.common.metadata.tasks.TaskDef;
/**
 * @Describe: 任务模板列表展示类
 * @author Zaney
 * @Date 2017年6月18日
 */
public class TaskModelDef extends TaskDef {
	private String createDate;
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

}
