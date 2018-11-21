package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 负载实体类
 * 
 * @author caidongqing
 *
 */
@Table("loadbalance")
public class LoadBalance extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id;
	@Column("manager_ip")
	private String managerIp;
	@Column("port")
	private Integer port;
	@Column("type")
	private Integer type;
	@Column("tenant_id")
	private Integer tenantId;
	@Column("env_id")
	private Long envId;
	@IgnoreAll
	private String envName;// 应用数量
	@IgnoreAll
	private int appCount;// 应用数量
	@IgnoreAll
	private String mender;// 修改者
	@IgnoreAll
	private String creator;// 创建者
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getManagerIp() {
		return managerIp;
	}
	public void setManagerIp(String managerIp) {
		this.managerIp = managerIp;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getTenantId() {
		return tenantId;
	}
	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}
	public Long getEnvId() {
		return envId;
	}
	public void setEnvId(Long envId) {
		this.envId = envId;
	}
	public String getEnvName() {
		return envName;
	}
	public void setEnvName(String envName) {
		this.envName = envName;
	}
	public int getAppCount() {
		return appCount;
	}
	public void setAppCount(int appCount) {
		this.appCount = appCount;
	}
	public String getMender() {
		return mender;
	}
	public void setMender(String mender) {
		this.mender = mender;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}

}
