package com.bocloud.paas.entity;

import java.util.List;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 用户类
 * 
 * @author dongkai
 *
 */
/**
 * @author langzi
 *
 */
@Table("user")
public class User extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("username")
	private String username; // 账号
	@Column("password")
	private String password; // 密码
	@Column("email")
	private String email; // 邮件
	@Column("phone")
	private String phone; // 电话
	@Column("mobile")
	private String mobile; // 移动电话
	@Column("login_status")
	private Boolean loginStatus; // 登录状态
	@Column("company")
	private String company; // 公司
	@Column("session_id")
	private String sessionId; // sessionId
	@IgnoreAll
	private String tenantName;
	@Column("is_tenant")
	private Boolean isTenant; // 是否租户
	@Column("tenant_id")
	private Long tenantId; // 租户ID
	@IgnoreAll
	private List<String> roleNames;
	@Column("depart_id")
	private Long departId; // 组织机构ID
	@Column("user_id")
	private String userId;
	@Column("sex")
	private Boolean sex;
	
	@IgnoreAll
	private String departName;// 组织机构名称
	@IgnoreAll
	private Integer cpu; // CPU
	@IgnoreAll
	private Integer memory;// 内存
	@IgnoreAll
	private Integer disk; // 磁盘
	@IgnoreAll
	private Integer instances;
	@IgnoreAll
	private Boolean checked;

	/**
	 * @return the instances
	 */
	public Integer getInstances() {
		return instances;
	}

	/**
	 * @param instances
	 *            the instances to set
	 */
	public void setInstances(Integer instances) {
		this.instances = instances;
	}

	/**
	 * @return the checked
	 */
	public Boolean getChecked() {
		return checked;
	}

	/**
	 * @param checked
	 *            the checked to set
	 */
	public void setChecked(Boolean checked) {
		this.checked = checked;
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
	 * @return the departName
	 */
	public String getDepartName() {
		return departName;
	}

	/**
	 * @param departName
	 *            the departName to set
	 */
	public void setDepartName(String departName) {
		this.departName = departName;
	}

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

	/**
	 * @return the tenantName
	 */
	public String getTenantName() {
		return tenantName;
	}

	/**
	 * @param tenantName
	 *            the tenantName to set
	 */
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	/**
	 * @return the roleNames
	 */
	public List<String> getRoleNames() {
		return roleNames;
	}

	/**
	 * @param roleNames
	 *            the roleNames to set
	 */
	public void setRoleNames(List<String> roleNames) {
		this.roleNames = roleNames;
	}

	public User() {
		super();
	}

	public User(String name, String username, String email, String company, String phone, String mobile, Long createrId,
			Long menderId, Boolean isTenant, Long tenantId) {
		this.setName(name);
		this.setCreaterId(createrId);
		this.setMenderId(menderId);
		this.username = username;
		this.company = company;
		this.email = email;
		this.phone = phone;
		this.mobile = mobile;
		this.isTenant = isTenant;
		this.tenantId = tenantId;
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
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
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
	 * @return the loginStatus
	 */
	public Boolean getLoginStatus() {
		return loginStatus;
	}

	/**
	 * @param loginStatus
	 *            the loginStatus to set
	 */
	public void setLoginStatus(Boolean loginStatus) {
		this.loginStatus = loginStatus;
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
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the isTenant
	 */
	public Boolean getIsTenant() {
		return isTenant;
	}

	/**
	 * @param isTenant
	 *            the isTenant to set
	 */
	public void setIsTenant(Boolean isTenant) {
		this.isTenant = isTenant;
	}

	/**
	 * @return the tenantId
	 */
	public Long getTenantId() {
		return tenantId;
	}

	/**
	 * @param tenantId
	 *            the tenantId to set
	 */
	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
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
