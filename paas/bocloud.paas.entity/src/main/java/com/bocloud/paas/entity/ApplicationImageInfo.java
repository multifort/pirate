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
@Table("application_image_info")
public class ApplicationImageInfo {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Value("application_id")
	private Long applicationId;
	@Value("image_id")
	private Long imageId;
	@Value("use_count")
	private Integer useCount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getImageId() {
		return imageId;
	}

	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public Integer getUseCount() {
		return useCount;
	}

	public void setUseCount(Integer useCount) {
		this.useCount = useCount;
	}

}
