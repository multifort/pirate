package com.bocloud.paas.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.bocloud.common.utils.DateSerializer;
import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * describe: 服务部署历史记录
 * @author Zaney
 * @data 2017年9月4日
 */
@Table("deploy_history")
public class DeployHistory {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("service_name")
	private String serviceName;
	@Column("app_id")
	private Long applicationId;
	@Column("version")
	@JsonSerialize(using = DateSerializer.class)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date version;
	@Column("object")
	private String object;
	@Column("data_info")
	private String dataInfo;
	@Column("param_info")
	private String paramInfo;
	@Column("remark")
	private String remark;
	@Column("gmt_create")
	@JsonSerialize(using = DateSerializer.class)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date gmtCreate;// 创建时间
	@Column("creater_id")
	private Long createId;
	@Column("result")
	private String result;

	@IgnoreAll
	private String creatorName;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public Date getVersion() {
		return version;
	}

	public void setVersion(Date version) {
		this.version = version;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getDataInfo() {
		return dataInfo;
	}

	public void setDataInfo(String dataInfo) {
		this.dataInfo = dataInfo;
	}

	public String getParamInfo() {
		return paramInfo;
	}

	public void setParamInfo(String paramInfo) {
		this.paramInfo = paramInfo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Long getCreateId() {
		return createId;
	}

	public void setCreateId(Long createId) {
		this.createId = createId;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	
}
