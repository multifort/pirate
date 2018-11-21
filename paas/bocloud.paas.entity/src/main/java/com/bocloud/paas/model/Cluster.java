package com.bocloud.paas.model;

import java.util.List;

public class Cluster {

	private Long envId;// 环境ID
	private List<Long> ids; // 要添加的主机id
	private String networkType; // 网络类型：默认使用flanneld
	private String networkSegment;// 网络网段
	private String load;// 是否创建负载：默认创建负载
	private String monitor;// 是否搭建监控
	private String dns; // 是否搭建dns
	private String log;// 是否搭建日志监控
	private String highAvai;// 是否高可用
	private String envName;// 环境名称
	private String platform;// 平台类型
	private String remark;// 描述
	private String virtualIp;// 集群对外提供访问的虚拟IP

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
	 * @return the networkType
	 */
	public String getNetworkType() {
		return networkType;
	}

	/**
	 * @param networkType
	 *            the networkType to set
	 */
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	/**
	 * @return the networkSegment
	 */
	public String getNetworkSegment() {
		return networkSegment;
	}

	/**
	 * @param networkSegment
	 *            the networkSegment to set
	 */
	public void setNetworkSegment(String networkSegment) {
		this.networkSegment = networkSegment;
	}

	/**
	 * @return the load
	 */
	public String getLoad() {
		return load;
	}

	/**
	 * @param load
	 *            the load to set
	 */
	public void setLoad(String load) {
		this.load = load;
	}

	/**
	 * @return the monitor
	 */
	public String getMonitor() {
		return monitor;
	}

	/**
	 * @param monitor
	 *            the monitor to set
	 */
	public void setMonitor(String monitor) {
		this.monitor = monitor;
	}

	/**
	 * @return the dns
	 */
	public String getDns() {
		return dns;
	}

	/**
	 * @param dns
	 *            the dns to set
	 */
	public void setDns(String dns) {
		this.dns = dns;
	}

	/**
	 * @return the log
	 */
	public String getLog() {
		return log;
	}

	/**
	 * @param log
	 *            the log to set
	 */
	public void setLog(String log) {
		this.log = log;
	}

	/**
	 * @return the highAvai
	 */
	public String getHighAvai() {
		return highAvai;
	}

	/**
	 * @param highAvai
	 *            the highAvai to set
	 */
	public void setHighAvai(String highAvai) {
		this.highAvai = highAvai;
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
	 * @return the virtualIp
	 */
	public String getVirtualIp() {
		return virtualIp;
	}

	/**
	 * @param virtualIp the virtualIp to set
	 */
	public void setVirtualIp(String virtualIp) {
		this.virtualIp = virtualIp;
	}
	
}
