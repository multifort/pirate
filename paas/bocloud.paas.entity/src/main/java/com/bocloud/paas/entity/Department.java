package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.IgnoreUpdate;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 组织机构管理
 * 
 * @author dongkai
 *
 */
@Table("department")
public class Department extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("parent_id")
	@IgnoreUpdate
	private Long parentId; // 父节点
	@Column("tenant_id")
	private Long tenantId; // 租户ID
	@IgnoreAll
	private String children;
	@IgnoreAll
	private Integer cpu; // CPU
	@IgnoreAll
	private Integer memory;// 内存
	@IgnoreAll
	private Integer disk; // 磁盘
	@IgnoreAll
	private Integer instances;

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
	 * @return the children
	 */
	public String getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(String children) {
		this.children = children;
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
	 * @return the parentId
	 */
	public Long getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(Long parentId) {
		this.parentId = parentId;
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
