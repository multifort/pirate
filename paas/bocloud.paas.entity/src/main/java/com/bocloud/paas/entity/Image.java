package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 
 * @author zjm
 * @date 2017年3月17日
 * @describe
 */
@Table("image")
public class Image extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("tag")
	private String tag;
	@Column("property")
	private Integer property;
	@Column("type")
	private Integer type;
	@Column("usage_count")
	private Integer usage_count;
	@Column("dept_Id")
	private Long deptId;
	@Column("uuid")
	private String uuid;

	@IgnoreAll
	private String creatorName;
	@IgnoreAll
	private String menderName;
	@IgnoreAll
	private String namespace;
	@IgnoreAll
	private String repositoryName;
	@IgnoreAll
	private String repositoryAddress;
	@IgnoreAll
	private Integer repositoryPort;
	@IgnoreAll
	private Integer repositoryType;
	@IgnoreAll
	private Long repositoryId;
	
	public Integer getRepositoryType() {
		return repositoryType;
	}

	public void setRepositoryType(Integer repositoryType) {
		this.repositoryType = repositoryType;
	}

	public Integer getRepositoryPort() {
		return repositoryPort;
	}

	public void setRepositoryPort(Integer repositoryPort) {
		this.repositoryPort = repositoryPort;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getProperty() {
		return property;
	}

	public void setProperty(Integer property) {
		this.property = property;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getUsage_count() {
		return usage_count;
	}

	public void setUsage_count(Integer usage_count) {
		this.usage_count = usage_count;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public Long getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Long repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getRepositoryAddress() {
		return repositoryAddress;
	}

	public void setRepositoryAddress(String repositoryAddress) {
		this.repositoryAddress = repositoryAddress;
	}

}
