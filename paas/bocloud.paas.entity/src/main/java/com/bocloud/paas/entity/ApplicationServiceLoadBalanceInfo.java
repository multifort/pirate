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
@Table("application_service_loadbalance_info")
public class ApplicationServiceLoadBalanceInfo {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Value("application_id")
	private Long applicationId;
	@Value("service_id")
	private Long serviceId;
	@Value("loadbalance_id")
	private Long loadbalanceId;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
	public Long getServiceId() {
		return serviceId;
	}
	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	public Long getLoadbalanceId() {
		return loadbalanceId;
	}
	public void setLoadbalanceId(Long loadbalanceId) {
		this.loadbalanceId = loadbalanceId;
	}

}
