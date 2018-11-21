package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * describe: 配置管理对象
 * @author Zaney
 * @data 2017年10月17日
 */
@Table("config_manage")
public class ConfigManage extends GenericEntity {
	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("file_Dir")
	private String fileDir;
	@Column("app_id")
	private Long appId;
	@Column("type")
	private String type;
	@Column("dept_id")
	private Long deptId;
	
	@IgnoreAll
	private String menderName;
	@IgnoreAll
	private String creatorName;
	@IgnoreAll
	private String appName;
	@IgnoreAll
	private String envName;
	
	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public String getMenderName() {
		return menderName;
	}

	public void setMenderName(String menderName) {
		this.menderName = menderName;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
