package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;
/**
 * @Describe: 任务实体类
 * @author Zaney
 * @Date 2017年6月16日
 */
@Table("task")
public class Task extends GenericEntity {
	
	@PK(value = PKStrategy.AUTO)
	private Long id;
	@Column("file_name")
	private String fileName;//插件配置文件的名称以及类型，如：name.xml
	@Column("ch_name")
	private String chName;//插件中文名
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getChName() {
		return chName;
	}
	public void setChName(String chName) {
		this.chName = chName;
	}
	
}
