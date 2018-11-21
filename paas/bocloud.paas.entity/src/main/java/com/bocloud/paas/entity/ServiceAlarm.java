package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;

/**
 * describe: 服务可用实例数告警对象
 * @author Zaney
 * @data 2017年11月6日
 */
@Table("service_alarm")
public class ServiceAlarm {
	public static enum AlarmStatus {NORMAL, ALARM, NOSTRATEGY} //正常 警告 无策略
	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("name")
	private String name;
	@Column("number")
	private Integer number;
	@Column("email")
	private String email;
	@Column("status")
	private String status;
	@Column("application_id")
	private Long applicationId;
	
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
	
}
