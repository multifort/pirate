package com.bocloud.paas.entity;
/**
 * openshift监控类
 * @author Zaney
 * @data:2017年3月31日
 * @describe:
 */
public class Monitor {
	public static enum Type {CPU, MEMORY, NETWORKTX, NETWORKRX, FILEUSAGE, FILELIMIT}
	//应用
	private Long appId;
	//节点、pod
	private String resourceName;
	private Long envId;
	//公用属性
	private String type;//监控类型：pod/node
//	private String domainName;//监控主机域名
	private String ResourceType;//资源类型：cpu/memory/network
	private String startTime;//监控的开始时间
	private String bucketDuration;
	private String descripteName;//描述名称  cpu/memory/network监控对象  如: "cpu/usage_rate"
	private String namespace;
//	private String port;
	private String url;
	private String dataSource;//监控数据库名称
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getEnvId() {
		return envId;
	}

	public void setEnvId(Long envId) {
		this.envId = envId;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getDescripteName() {
		return descripteName;
	}
	public void setDescripteName(String descripteName) {
		this.descripteName = descripteName;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getBucketDuration() {
		return bucketDuration;
	}
	public void setBucketDuration(String bucketDuration) {
		this.bucketDuration = bucketDuration;
	}
	public String getResourceType() {
		return ResourceType;
	}
	public void setResourceType(String resourceType) {
		ResourceType = resourceType;
	}

}
