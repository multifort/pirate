package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * describe: 代码仓库数据库映射对象表
 * @author Zaney
 * @data 2017年10月27日
 */
@Table("code_repository")
public class CodeRepository extends GenericEntity {
	public static enum Status {ACTIVATE, Lock}
	
	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("code_source")
	private String codeSource;
	@Column("user_name")
	private String username;
	@Column("password")
	private String password;
	@Column("type")
	private String type;
	@Column("software_type")
	private String softwareType;
	@Column("dept_id")
	private Long deptId;
	
	@IgnoreAll
	private String creatorName;
	@IgnoreAll
	private String menderName;
	
	public Long getDeptId() {
		return deptId;
	}
	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}
	public String getSoftwareType() {
		return softwareType;
	}
	public void setSoftwareType(String softwareType) {
		this.softwareType = softwareType;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCodeSource() {
		return codeSource;
	}
	public void setCodeSource(String codeSource) {
		this.codeSource = codeSource;
	}
}
