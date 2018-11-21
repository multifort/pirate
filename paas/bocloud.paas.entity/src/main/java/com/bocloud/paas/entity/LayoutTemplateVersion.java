package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

public class LayoutTemplateVersion extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id;
	@Column("template_file_path")
	private String templateFilePath;
	@Column("version")
	private String version;
	@Column("layout_template_id")
	private Long layoutTemplateId;

	@IgnoreAll
	private String fileContent;
	@IgnoreAll
	private String fileTemplateContent;
	@IgnoreAll
	private String fileDescContent;
	@IgnoreAll
	private String creatorName;
	@IgnoreAll
	private String menderName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTemplateFilePath() {
		return templateFilePath;
	}

	public void setTemplateFilePath(String templateFilePath) {
		this.templateFilePath = templateFilePath;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

	public String getFileTemplateContent() {
		return fileTemplateContent;
	}

	public void setFileTemplateContent(String fileTemplateContent) {
		this.fileTemplateContent = fileTemplateContent;
	}

	public String getFileDescContent() {
		return fileDescContent;
	}

	public void setFileDescContent(String fileDescContent) {
		this.fileDescContent = fileDescContent;
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

	public Long getLayoutTemplateId() {
		return layoutTemplateId;
	}

	public void setLayoutTemplateId(Long layoutTemplateId) {
		this.layoutTemplateId = layoutTemplateId;
	}

}
