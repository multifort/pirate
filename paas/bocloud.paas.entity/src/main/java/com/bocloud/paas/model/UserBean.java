package com.bocloud.paas.model;

import java.util.Date;

/**
 * 用户业务类
 * 
 * @author dongkai
 *
 */
public class UserBean {
	private Long id; // ID
	private String name; // 真名
	private String username; // 用户名
	private String status; // 用户状态
	private String mobile; // 移动电话
	private String phone; // 电话
	private String loginStatus; // 登陆状态
	private String email; // 邮箱
	private String company; // 公司
	private String remark; // 描述
	private Long menderId; // 修改人
	private Date gmtModify; // 修改时间
	private Long departId; // 组织机构ID
	private String userId;//工号
	private Boolean sex;//性别

	/**
	 * @return the departId
	 */
	public Long getDepartId() {
		return departId;
	}

	/**
	 * @param departId
	 *            the departId to set
	 */
	public void setDepartId(Long departId) {
		this.departId = departId;
	}

	public UserBean() {
		super();
	}

	public UserBean(String name, String username, String mobile, String phone, String email, String company,
			String remark, Long menderId, Date gmtModify) {
		super();
		this.name = name;
		this.username = username;
		this.mobile = mobile;
		this.phone = phone;
		this.email = email;
		this.company = company;
		this.remark = remark;
		this.menderId = menderId;
		this.gmtModify = gmtModify;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the mobile
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * @param mobile
	 *            the mobile to set
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone
	 *            the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the loginStatus
	 */
	public String getLoginStatus() {
		return loginStatus;
	}

	/**
	 * @param loginStatus
	 *            the loginStatus to set
	 */
	public void setLoginStatus(String loginStatus) {
		this.loginStatus = loginStatus;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
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

	/**
	 * @return the remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * @param remark
	 *            the remark to set
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * @return the menderId
	 */
	public Long getMenderId() {
		return menderId;
	}

	/**
	 * @param menderId
	 *            the menderId to set
	 */
	public void setMenderId(Long menderId) {
		this.menderId = menderId;
	}

	/**
	 * @return the gmtModify
	 */
	public Date getGmtModify() {
		return gmtModify;
	}

	/**
	 * @param gmtModify
	 *            the gmtModify to set
	 */
	public void setGmtModify(Date gmtModify) {
		this.gmtModify = gmtModify;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Boolean getSex() {
		return sex;
	}

	public void setSex(Boolean sex) {
		this.sex = sex;
	}
	
}
