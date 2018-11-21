package com.bocloud.paas.entity;

import org.springframework.beans.factory.annotation.Value;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 仓库镜像对象
 * @author Zaney
 * @data:2017年3月15日
 * @describe:
 */
@Table("repository_image_info")
public class RepositoryImage {
	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Value("repository_id")
	private Long repositoryId;
	@Value("namespace")
	private String namespace;
	@Value("image_id")
	private Long imageId;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getRepositoryId() {
		return repositoryId;
	}
	public void setRepositoryId(Long repositoryId) {
		this.repositoryId = repositoryId;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public Long getImageId() {
		return imageId;
	}
	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}
}
