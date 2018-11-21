package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 用户安全
 * 
 * @author dongkai
 *
 */
@Table("user_security")
public class UserSecurity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("user_id")
	private Long userId; // 用户ID
	@Column("tenant_id")
	private Long tenantId; // 租户ID
	@Column("salt")
	private String salt; // 随机数
	@Column("api_key")
	private String apiKey;
	@Column("sec_key")
	private String secKey;

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
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
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

	/**
	 * @return the salt
	 */
	public String getSalt() {
		return salt;
	}

	/**
	 * @param salt
	 *            the salt to set
	 */
	public void setSalt(String salt) {
		this.salt = salt;
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @param apiKey
	 *            the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * @return the secKey
	 */
	public String getSecKey() {
		return secKey;
	}

	/**
	 * @param secKey
	 *            the secKey to set
	 */
	public void setSecKey(String secKey) {
		this.secKey = secKey;
	}

}
