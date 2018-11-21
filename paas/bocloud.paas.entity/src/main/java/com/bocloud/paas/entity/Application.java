package com.bocloud.paas.entity;

import java.util.List;
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
@Table("application")
public class Application extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("namespace")
	private String namespace;
	@Column("env_id")
	private Long envId;
	@Column("address")
	private String address;
	@Column("tenant_id")
	private Long tenantId;
	@Column("dept_id")
	private Long deptId;
	@Column("quota_status")
	private String quotaStatus;
	
	@IgnoreAll
	private String requestCpu;
	@IgnoreAll
	private String requestMemory;
	@IgnoreAll
	private String currentCpu;
	@IgnoreAll
	private String currentMemory;
	@IgnoreAll
	private String limitCpu;
	@IgnoreAll
	private String limitMemory;
	@IgnoreAll
	private Integer instanceNum = 0;
	@IgnoreAll
	private Integer serviceNum = 0;
	@IgnoreAll
	private String menderName;
	@IgnoreAll
	private String creatorName;
	@IgnoreAll
	private String environmentName;
	@IgnoreAll
	private String servicePath;
	
	@IgnoreAll
	private Integer jobNum = 0;
	
	/**
	 * 应用所依赖的镜像和文件的集合
	 */
	@IgnoreAll
	private List<String> dependents;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getEnvId() {
		return envId;
	}

	public void setEnvId(Long envId) {
		this.envId = envId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public String getRequestCpu() {
		return requestCpu;
	}

	public void setRequestCpu(String requestCpu) {
		this.requestCpu = requestCpu;
	}

	public String getRequestMemory() {
		return requestMemory;
	}

	public void setRequestMemory(String requestMemory) {
		this.requestMemory = requestMemory;
	}

	public void setLimitCpu(String limitCpu) {
		this.limitCpu = limitCpu;
	}

	public void setLimitMemory(String limitMemory) {
		this.limitMemory = limitMemory;
	}

	public String getCurrentMemory() {
		return currentMemory;
	}

	public void setCurrentMemory(String currentMemory) {
		this.currentMemory = currentMemory;
	}

	public String getCurrentCpu() {
		return currentCpu;
	}

	public void setCurrentCpu(String currentCpu) {
		this.currentCpu = currentCpu;
	}

	public String getLimitCpu() {
		return limitCpu;
	}

	public String getLimitMemory() {
		return limitMemory;
	}

	public Integer getServiceNum() {
		return serviceNum;
	}

	public void setServiceNum(Integer serviceNum) {
		this.serviceNum = serviceNum;
	}
	
	public Integer getInstanceNum() {
		return instanceNum;
	}
	
	public void setInstanceNum(Integer instanceNum) {
		this.instanceNum = instanceNum;
	}

	public String getMenderName() {
		return menderName;
	}

	public void setMenderName(String menderName) {
		this.menderName = menderName;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public List<String> getDependents() {
		return dependents;
	}

	public void setDependents(List<String> dependents) {
		this.dependents = dependents;
	}
	
	public String getEnvironmentName() {
		return environmentName;
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getServicePath() {
		return servicePath;
	}

	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}

	public String getQuotaStatus() {
		return quotaStatus;
	}

	public void setQuotaStatus(String quotaStatus) {
		this.quotaStatus = quotaStatus;
	}

	public Integer getJobNum() {
		return jobNum;
	}

	public void setJobNum(Integer jobNum) {
		this.jobNum = jobNum;
	}
	
}
