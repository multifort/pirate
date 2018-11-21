package com.bocloud.paas.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.bocloud.common.utils.DateSerializer;
import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 租户类
 * 
 * @author dongkai
 *
 */
@Table("tenant")
public class Tenant extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("tenant_phone")
	private String tenantPhone; // 租户电话
	@Column("contacter")
	private String contacter; // 联系人
	@Column("contact_email")
	private String contactEmail; // 联系邮箱
	@Column("contact_phone")
	private String contactPhone; // 联系电话
	@Column("address")
	private String address; // 公司地址
	@Column("company")
	private String company; // 公司名称
	@Column("expired_time")
	@JsonSerialize(using = DateSerializer.class)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date expiredTime;// 过期时间
	@IgnoreAll
	private Integer cpu; // CPU
	@IgnoreAll
	private Integer memory;// 内存
	@IgnoreAll
	private Integer disk; // 磁盘
	@IgnoreAll
	private Integer instance;
	@IgnoreAll
	private String vendorId;

	/**
	 * @return the vendorId
	 */
	public String getVendorId() {
		return vendorId;
	}

	/**
	 * @param vendorId
	 *            the vendorId to set
	 */
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	/**
	 * @return the instance
	 */
	public Integer getInstance() {
		return instance;
	}

	/**
	 * @param instance
	 *            the instance to set
	 */
	public void setInstance(Integer instance) {
		this.instance = instance;
	}

	/**
	 * @return the expiredTime
	 */
	public Date getExpiredTime() {
		return expiredTime;
	}

	/**
	 * @param expiredTime
	 *            the expiredTime to set
	 */
	public void setExpiredTime(Date expiredTime) {
		this.expiredTime = expiredTime;
	}

	/**
	 * @return the cpu
	 */
	public Integer getCpu() {
		return cpu;
	}

	/**
	 * @param cpu
	 *            the cpu to set
	 */
	public void setCpu(Integer cpu) {
		this.cpu = cpu;
	}

	/**
	 * @return the memory
	 */
	public Integer getMemory() {
		return memory;
	}

	/**
	 * @param memory
	 *            the memory to set
	 */
	public void setMemory(Integer memory) {
		this.memory = memory;
	}

	/**
	 * @return the disk
	 */
	public Integer getDisk() {
		return disk;
	}

	/**
	 * @param disk
	 *            the disk to set
	 */
	public void setDisk(Integer disk) {
		this.disk = disk;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the tenantPhone
	 */
	public String getTenantPhone() {
		return tenantPhone;
	}

	/**
	 * @param tenantPhone
	 *            the tenantPhone to set
	 */
	public void setTenantPhone(String tenantPhone) {
		this.tenantPhone = tenantPhone;
	}

	/**
	 * @return the contacter
	 */
	public String getContacter() {
		return contacter;
	}

	/**
	 * @param contacter
	 *            the contacter to set
	 */
	public void setContacter(String contacter) {
		this.contacter = contacter;
	}

	/**
	 * @return the contactEmail
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * @param contactEmail
	 *            the contactEmail to set
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * @return the contactPhone
	 */
	public String getContactPhone() {
		return contactPhone;
	}

	/**
	 * @param contactPhone
	 *            the contactPhone to set
	 */
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * @param company
	 *            the company to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}

}
