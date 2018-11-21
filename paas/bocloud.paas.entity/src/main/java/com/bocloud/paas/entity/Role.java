package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 角色
 * 
 * @author dongkai
 *
 */
@Table("role")
public class Role extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("tenant_id")
	private Long tenantId; // 租户ID
	@Column("dept_id")
	private Long deptId;
	@IgnoreAll
	private boolean checked;

	public Role() {

	}

	public Role(String name, Long tenantId, Long createrId, Long menderId, String status) {
		this.setName(name);
		this.setTenantId(tenantId);
		this.setCreaterId(createrId);
		this.setMenderId(menderId);
		this.setStatus(status);
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param checked
	 *            the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
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

}
