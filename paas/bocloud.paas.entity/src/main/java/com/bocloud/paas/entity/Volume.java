package com.bocloud.paas.entity;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

@Table("volume")
public class Volume extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("labels")
	private String labels;// 标签
	@Column("policy")
	private String policy;// 回收策略：Retain、Recyle、Delete
	@Column("access")
	private String access;// accessModes：ReadWriteOnce、ReadOnlyMany、ReadWriteMany
	@Column("capacity")
	private String capacity;// 容量大小
	@Column("type")
	private String type; // 类型
	@Column("env_id")
	private Long envId;// 环境id
	@Column("annotations")
	private String annotations; // 存储注解
	@Column("path")
	private String path;
	@Column("ip")
	private String ip;// NFS存储服务IP
	@IgnoreAll
	private String creater;// 创建者
	@IgnoreAll
	private String mendor;// 修改者
	@IgnoreAll
	private String envName; // 环境名称
	@Column("monitors")
	private String monitors; // ceph监控节点
	@Column("dept_id")
	private Long deptId;
	
	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
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
	 * @return the labels
	 */
	public String getLabels() {
		return labels;
	}

	/**
	 * @param labels
	 *            the labels to set
	 */
	public void setLabels(String labels) {
		this.labels = labels;
	}

	/**
	 * @return the policy
	 */
	public String getPolicy() {
		return policy;
	}

	/**
	 * @param policy
	 *            the policy to set
	 */
	public void setPolicy(String policy) {
		this.policy = policy;
	}

	/**
	 * @return the access
	 */
	public String getAccess() {
		return access;
	}

	/**
	 * @param access
	 *            the access to set
	 */
	public void setAccess(String access) {
		this.access = access;
	}

	/**
	 * @return the capacity
	 */
	public String getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity
	 *            the capacity to set
	 */
	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the envId
	 */
	public Long getEnvId() {
		return envId;
	}

	/**
	 * @param envId
	 *            the envId to set
	 */
	public void setEnvId(Long envId) {
		this.envId = envId;
	}

	/**
	 * @return the creater
	 */
	public String getCreater() {
		return creater;
	}

	/**
	 * @param creater
	 *            the creater to set
	 */
	public void setCreater(String creater) {
		this.creater = creater;
	}

	/**
	 * @return the mendor
	 */
	public String getMendor() {
		return mendor;
	}

	/**
	 * @param mendor
	 *            the mendor to set
	 */
	public void setMendor(String mendor) {
		this.mendor = mendor;
	}

	/**
	 * @return the annotations
	 */
	public String getAnnotations() {
		return annotations;
	}

	/**
	 * @param annotations
	 *            the annotations to set
	 */
	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip
	 *            the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the envName
	 */
	public String getEnvName() {
		return envName;
	}

	/**
	 * @param envName
	 *            the envName to set
	 */
	public void setEnvName(String envName) {
		this.envName = envName;
	}

	
	/**
	 * @param id
	 * @param labels
	 * @param policy
	 * @param access
	 * @param capacity
	 * @param type
	 * @param envId
	 * @param annotations
	 * @param path
	 * @param ip
	 * @param creater
	 * @param mendor
	 * @param envName
	 */
	public Volume(Long id, String labels, String policy, String access, String capacity, String type, Long envId,
			String annotations, String path, String ip, String creater, String mendor, String envName) {
		super();
		this.id = id;
		this.labels = labels;
		this.policy = policy;
		this.access = access;
		this.capacity = capacity;
		this.type = type;
		this.envId = envId;
		this.annotations = annotations;
		this.path = path;
		this.ip = ip;
		this.creater = creater;
		this.mendor = mendor;
		this.envName = envName;
	}

	/**
	 * 
	 */
	public Volume() {
		super();
	}

	public String getMonitors() {
		return monitors;
	}

	public void setMonitors(String monitors) {
		this.monitors = monitors;
	}

	/*@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof Volume) {
			Volume volume = (Volume) obj;
			if (volume.getStatus().equals(this.getStatus()) && volume.policy.equals(this.policy)
					&& volume.access.equals(this.access) && volume.capacity.equals(this.capacity)
					&& volume.type.equals(this.type) && volume.path.equals(this.path)
					&& volume.annotations.equals(this.annotations) && volume.labels.equals(volume.labels)) {
				if (null != volume.ip && null != this.ip) {
					if (volume.ip.equals(this.ip)) {
						return true;
					}
				}
				return true;
			}
		}

		return false;
	}*/
	
}
