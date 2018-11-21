package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;

@Table("application_store")
public class ApplicationStore {
	
	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("name")
	private String name;
	@Column("version")
	private String version;
	@Column("icon")
	private String icon;
	@Column("template")
	private String template;
	@Column("type")
	private String type; //组件类型
	@Column("deploy_number")
	private Long deployNumber;
	@Column("file_path")
	private String filePath;
	@Column("picture_path")
	private String picturePath;
	@Column("deploy_type")
	private String deployType; //部署方式
	@Column("remark")
	private String remark;
	@Column("is_deleted")
	private Boolean deleted; // 是否被删除
	
	
	@IgnoreAll
	private String context;
	@IgnoreAll
	private String image;
	
	public String getDeployType() {
		return deployType;
	}
	public void setDeployType(String deployType) {
		this.deployType = deployType;
	}
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public String getPicturePath() {
		return picturePath;
	}
	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getDeployNumber() {
		return deployNumber;
	}
	public void setDeployNumber(Long deployNumber) {
		this.deployNumber = deployNumber;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public ApplicationStore() {}
	
	public ApplicationStore(Long id, String name, String version, String template) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.template = template;
	}
	
	public ApplicationStore(Long id, String name, String version, String icon, String template, String type,
			String remark) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.icon = icon;
		this.template = template;
		this.type = type;
		this.remark = remark;
	}
	
}
