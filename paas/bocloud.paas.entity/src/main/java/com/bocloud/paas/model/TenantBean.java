package com.bocloud.paas.model;

import java.util.Date;

/**
 * 租户业务类
 * 
 * @author dongkai
 *
 */
public class TenantBean {

	private Long id;
	private Date gmt_modify;
	private Long menderId;
	private String remark;
	private String tenantPhone;
	private String contacter;
	private String contactEmail;
	private String contactPhone;
	private String address;
	private String company;
	private String name;
	private String props;
	// private String authIds;// 权限Id们 （1,2,3,4）

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getGmt_modify() {
		return gmt_modify;
	}

	public void setGmt_modify(Date gmt_modify) {
		this.gmt_modify = gmt_modify;
	}

	public Long getMenderId() {
		return menderId;
	}

	public void setMenderId(Long menderId) {
		this.menderId = menderId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getTenantPhone() {
		return tenantPhone;
	}

	public void setTenantPhone(String tenantPhone) {
		this.tenantPhone = tenantPhone;
	}

	public String getContacter() {
		return contacter;
	}

	public void setContacter(String contacter) {
		this.contacter = contacter;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the props
	 */
	public String getProps() {
		return props;
	}

	/**
	 * @param props
	 *            the props to set
	 */
	public void setProps(String props) {
		this.props = props;
	}

}
