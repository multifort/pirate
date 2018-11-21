package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 角色-权限
 * 
 * @author dongkai
 *
 */
@Table("role_authority")
public class RoleAuthority {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("role_id")
	private Long roleId; // 角色ID
	@Column("auth_id")
	private Long authId; // 权限ID

	public RoleAuthority() {
		super();
	}

	public RoleAuthority(Long roleId, Long authId) {
		super();
		this.roleId = roleId;
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
	 * @return the roleId
	 */
	public Long getRoleId() {
		return roleId;
	}

	/**
	 * @param roleId
	 *            the roleId to set
	 */
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
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
