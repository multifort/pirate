package com.bocloud.paas.entity;

import org.springframework.beans.factory.annotation.Value;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;
/**
 * describe:服务依赖关系类
 * @author Zaney
 * @data 2017年8月1日
 */
@Table("service_rely_info")
public class ServiceRelyInfo {
	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Value("current_name")
	private String currentName;
	@Value("current_namespace")
	private String currentNamespace;
	@Value("rely_name")
	private String relyName;
	@Value("rely_namespace")
	private String relyNamespace;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCurrentName() {
		return currentName;
	}
	public void setCurrentName(String currentName) {
		this.currentName = currentName;
	}
	public String getCurrentNamespace() {
		return currentNamespace;
	}
	public void setCurrentNamespace(String currentNamespace) {
		this.currentNamespace = currentNamespace;
	}
	public String getRelyName() {
		return relyName;
	}
	public void setRelyName(String relyName) {
		this.relyName = relyName;
	}
	public String getRelyNamespace() {
		return relyNamespace;
	}
	public void setRelyNamespace(String relyNamespace) {
		this.relyNamespace = relyNamespace;
	}
	
}
