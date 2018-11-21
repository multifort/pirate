package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 租户-权限
 * 
 * @author dongkai
 *
 */
@Table("tenant_authority")
public class TenantAuthority {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("tenant_id")
	private Long tenantId; // 租户ID
	@Column("auth_id")
	private Long authId; // 权限ID

	public TenantAuthority() {
		super();
	}

	public TenantAuthority(Long tenantId, Long authId) {
		super();
		this.tenantId = tenantId;
		this.authId = authId;
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
	 * @return the authId
	 */
	public Long getAuthId() {
		return authId;
	}

	/**
	 * @param authId
	 *            the authId to set
	 */
	public void setAuthId(Long authId) {
		this.authId = authId;
	}

}
