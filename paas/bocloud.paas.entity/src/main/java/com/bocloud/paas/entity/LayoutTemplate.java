package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

public class LayoutTemplate extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id;
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
