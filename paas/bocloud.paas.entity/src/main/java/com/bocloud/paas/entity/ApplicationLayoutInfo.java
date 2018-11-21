package com.bocloud.paas.entity;

import org.springframework.beans.factory.annotation.Value;

import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 
 * @author zjm	
 * @date 2017年3月17日
 * @describe
 */
@Table("application_layout_info")
public class ApplicationLayoutInfo {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Value("application_id")
	private Long applicationId;
	@Value("layout_id")
	private Long layoutId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLayoutId() {
		return layoutId;
	}

	public void setLayoutId(Long layoutId) {
		this.layoutId = layoutId;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
}
