package com.bocloud.paas.entity;

import java.util.List;

import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.IgnoreAll;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 节点实体类，包括主节点与字节点
 * 
 * @author songsong
 *
 */
@Table("host")
public class Host extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id; // ID
	@Column("ip")
	private String ip;// ip地址
	@Column("env_id")
	private Long envId;// 环境id
	@Column("username")
	private String username;// 主机用户名
	@Column("password")
	private String password;// 主机密码
	@Column("labels")
	private String labels;// 主机标签
	@Column("host_name")
	private String hostName;// 主机在集群中显示的名字
	@Column("source")
	private String source; // 主机来源：create、receive
	@Column("etcd")
	private String etcd; // 是否是作为etcd服务器
	@Column("dept_id")
	private Long deptId;
	@IgnoreAll
	private String envName;// 所在环境的名称
	@IgnoreAll
	private List<Long> ids;// 主机ID集合
	@IgnoreAll
	private String creater;// 创建者
	@IgnoreAll
	private String mendor;// 修改者
	@IgnoreAll
	private String platform; // 平台类型
	@IgnoreAll
	private String master; // 主节点IP，用来存放yaml文件
	@IgnoreAll
	private String port;// 环境端口
	@IgnoreAll
	private String ipRange;// IP范围：批量添加时ip的范围
	
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
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName
	 *            the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the etcd
	 */
	public String getEtcd() {
		return etcd;
	}

	/**
	 * @param etcd
	 *            the etcd to set
	 */
	public void setEtcd(String etcd) {
		this.etcd = etcd;
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
	 * @return the ids
	 */
	public List<Long> getIds() {
		return ids;
	}

	/**
	 * @param ids
	 *            the ids to set
	 */
	public void setIds(List<Long> ids) {
		this.ids = ids;
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
	 * @return the platform
	 */
	public String getPlatform() {
		return platform;
	}

	/**
	 * @param platform
	 *            the platform to set
	 */
	public void setPlatform(String platform) {
		this.platform = platform;
	}

	/**
	 * @return the master
	 */
	public String getMaster() {
		return master;
	}

	/**
	 * @param master
	 *            the master to set
	 */
	public void setMaster(String master) {
		this.master = master;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the ipRange
	 */
	public String getIpRange() {
		return ipRange;
	}

	/**
	 * @param ipRange
	 *            the ipRange to set
	 */
	public void setIpRange(String ipRange) {
		this.ipRange = ipRange;
	}

	/**
	 * @param id
	 * @param ip
	 * @param envId
	 * @param username
	 * @param password
	 * @param labels
	 * @param hostName
	 * @param source
	 * @param etcd
	 * @param envName
	 * @param ids
	 * @param creater
	 * @param mendor
	 * @param platform
	 * @param master
	 * @param port
	 * @param ipRange
	 */
	public Host(Long id, String ip, Long envId, String username, String password, String labels, String hostName,
			String source, String etcd, String envName, List<Long> ids, String creater, String mendor, String platform,
			String master, String port, String ipRange) {
		super();
		this.id = id;
		this.ip = ip;
		this.envId = envId;
		this.username = username;
		this.password = password;
		this.labels = labels;
		this.hostName = hostName;
		this.source = source;
		this.etcd = etcd;
		this.envName = envName;
		this.ids = ids;
		this.creater = creater;
		this.mendor = mendor;
		this.platform = platform;
		this.master = master;
		this.port = port;
		this.ipRange = ipRange;
	}

	/**
	 * 
	 */
	public Host() {
		super();
	}

}
