package com.bocloud.paas.service.application.model;

/**
 * describe: 用于部署模板 ，对配置管理配置项，参数管理的类
 * @author Zaney
 * @data 2017年10月20日
 */
public class ConfManage {
	private String cmName; //配置文件名称
	private String path; //配置文件挂载路径
	
	private String mappingPath; //配置文件Key属性映射路径
	private String cmKey; //配置文件的key属性
	
	public ConfManage() {
		super();
	}
	
	public ConfManage(String cmName) {
		super();
		this.cmName = cmName;
	}

	public ConfManage(String cmName, String path) {
		super();
		this.cmName = cmName;
		this.path = path;
	}

	public ConfManage(String cmName, String path, String mappingPath, String cmKey) {
		super();
		this.cmName = cmName;
		this.path = path;
		this.mappingPath = mappingPath;
		this.cmKey = cmKey;
	}



	public String getCmName() {
		return cmName;
	}
	public void setCmName(String cmName) {
		this.cmName = cmName;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getMappingPath() {
		return mappingPath;
	}
	public void setMappingPath(String mappingPath) {
		this.mappingPath = mappingPath;
	}
	public String getCmKey() {
		return cmKey;
	}
	public void setCmKey(String cmKey) {
		this.cmKey = cmKey;
	}
	
}
