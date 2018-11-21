package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * @Describe: 流程编排类
 * @author Zaney
 * @2017年6月14日
 */
@Table("workflow")
public class Workflow extends GenericEntity {
	@PK(value = PKStrategy.AUTO)
	private Long id;
	@Column("version")
	private Integer version;
	@Column("workflow_json")
	private String workflowJson;//页面还原图形需要的信息格式
	@Column("workflow_def")
	private String workflowDef;//workflowDef对象
	@Column("workflow_id")
	private String workflowId;
	@Column("dept_id")
	private Long deptId;
	
	@IgnoreAll
	private String creatorName;
	@IgnoreAll
	private String menderName;
	@IgnoreAll
	private String status;
	
	public Long getDeptId() {
		return deptId;
	}
	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}
	public String getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getWorkflowDef() {
		return workflowDef;
	}
	public void setWorkflowDef(String workflowDef) {
		this.workflowDef = workflowDef;
	}
	public String getWorkflowJson() {
		return workflowJson;
	}
	public void setWorkflowJson(String workflowJson) {
		this.workflowJson = workflowJson;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCreatorName() {
		return creatorName;
	}
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	public String getMenderName() {
		return menderName;
	}
	public void setMenderName(String menderName) {
		this.menderName = menderName;
	}
	
}
