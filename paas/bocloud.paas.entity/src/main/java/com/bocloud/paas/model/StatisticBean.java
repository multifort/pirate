package com.bocloud.paas.model;

import java.util.List;
import java.util.Map;

import com.bocloud.paas.entity.Environment;

public class StatisticBean {

	private Integer appTotal;// 应用总数

	private Integer appRunNum;// 运行中应用总数

	private Integer podTotal;// pod总数

	private Integer podRunTotal;// 运行中实例数

	private Integer appPodNum;// 应用实例数

	private Integer appRunPodNum;// 运行中应用实例总数

	private Integer servicePodNum;// 服务实例总数

	private Integer serviceRunPodNum;// 运行中服务实例总数

	private Integer pvTotal;// persistentVolume总数

	private Integer pvUsedNum; // pv使用中个数

	private Integer pvCapacityTotal;// pv总容量

	private Integer pvUsedTotal;// 使用中

	private Integer nodeTotal;// 机器总数

	private Integer scheduNodeNum;// 可调度机器总数

	private Integer unscheduNodeNum;// 不可调度机器总数

	private Integer imageTotal;// 镜像总数

	private Integer publicImageNum;// 公有镜像总数

	private Integer privateImageNum;// 私有镜像总数

	private List<Environment> environments;// 环境信息

	private String status;// 系统运行状态

	private Map<String, List<ApplicationBean>> appMap; // 存放排序之后的应用

	private Integer abnormalHostNum; // 不在环境中单状态不正常的主机：如果环境代理IP不可达，则视该环境下的所有主机都是不正常的

	private Integer normalHostNum; // 不在环境中但状态正常的主机
	
	private Integer addingHostNum; // 添加中主机
	
	private Integer outingHostNum; // 移出中主机

	/**
	 * @return the appTotal
	 */
	public Integer getAppTotal() {
		return appTotal;
	}

	/**
	 * @param appTotal
	 *            the appTotal to set
	 */
	public void setAppTotal(Integer appTotal) {
		this.appTotal = appTotal;
	}

	/**
	 * @return the appRunNum
	 */
	public Integer getAppRunNum() {
		return appRunNum;
	}

	/**
	 * @param appRunNum
	 *            the appRunNum to set
	 */
	public void setAppRunNum(Integer appRunNum) {
		this.appRunNum = appRunNum;
	}

	/**
	 * @return the podTotal
	 */
	public Integer getPodTotal() {
		return podTotal;
	}

	/**
	 * @param podTotal
	 *            the podTotal to set
	 */
	public void setPodTotal(Integer podTotal) {
		this.podTotal = podTotal;
	}

	/**
	 * @return the podRunTotal
	 */
	public Integer getPodRunTotal() {
		return podRunTotal;
	}

	/**
	 * @param podRunTotal
	 *            the podRunTotal to set
	 */
	public void setPodRunTotal(Integer podRunTotal) {
		this.podRunTotal = podRunTotal;
	}

	/**
	 * @return the appPodNum
	 */
	public Integer getAppPodNum() {
		return appPodNum;
	}

	/**
	 * @param appPodNum
	 *            the appPodNum to set
	 */
	public void setAppPodNum(Integer appPodNum) {
		this.appPodNum = appPodNum;
	}

	/**
	 * @return the appRunPodNum
	 */
	public Integer getAppRunPodNum() {
		return appRunPodNum;
	}

	/**
	 * @param appRunPodNum
	 *            the appRunPodNum to set
	 */
	public void setAppRunPodNum(Integer appRunPodNum) {
		this.appRunPodNum = appRunPodNum;
	}

	/**
	 * @return the servicePodNum
	 */
	public Integer getServicePodNum() {
		return servicePodNum;
	}

	/**
	 * @param servicePodNum
	 *            the servicePodNum to set
	 */
	public void setServicePodNum(Integer servicePodNum) {
		this.servicePodNum = servicePodNum;
	}

	/**
	 * @return the serviceRunPodNum
	 */
	public Integer getServiceRunPodNum() {
		return serviceRunPodNum;
	}

	/**
	 * @param serviceRunPodNum
	 *            the serviceRunPodNum to set
	 */
	public void setServiceRunPodNum(Integer serviceRunPodNum) {
		this.serviceRunPodNum = serviceRunPodNum;
	}

	/**
	 * @return the pvTotal
	 */
	public Integer getPvTotal() {
		return pvTotal;
	}

	/**
	 * @param pvTotal
	 *            the pvTotal to set
	 */
	public void setPvTotal(Integer pvTotal) {
		this.pvTotal = pvTotal;
	}

	/**
	 * @return the pvCapacityTotal
	 */
	public Integer getPvCapacityTotal() {
		return pvCapacityTotal;
	}

	/**
	 * @param pvCapacityTotal
	 *            the pvCapacityTotal to set
	 */
	public void setPvCapacityTotal(Integer pvCapacityTotal) {
		this.pvCapacityTotal = pvCapacityTotal;
	}

	/**
	 * @return the nodeTotal
	 */
	public Integer getNodeTotal() {
		return nodeTotal;
	}

	/**
	 * @param nodeTotal
	 *            the nodeTotal to set
	 */
	public void setNodeTotal(Integer nodeTotal) {
		this.nodeTotal = nodeTotal;
	}

	/**
	 * @return the scheduNodeNum
	 */
	public Integer getScheduNodeNum() {
		return scheduNodeNum;
	}

	/**
	 * @param scheduNodeNum
	 *            the scheduNodeNum to set
	 */
	public void setScheduNodeNum(Integer scheduNodeNum) {
		this.scheduNodeNum = scheduNodeNum;
	}

	/**
	 * @return the unscheduNodeNum
	 */
	public Integer getUnscheduNodeNum() {
		return unscheduNodeNum;
	}

	/**
	 * @param unscheduNodeNum
	 *            the unscheduNodeNum to set
	 */
	public void setUnscheduNodeNum(Integer unscheduNodeNum) {
		this.unscheduNodeNum = unscheduNodeNum;
	}

	/**
	 * @return the imageTotal
	 */
	public Integer getImageTotal() {
		return imageTotal;
	}

	/**
	 * @param imageTotal
	 *            the imageTotal to set
	 */
	public void setImageTotal(Integer imageTotal) {
		this.imageTotal = imageTotal;
	}

	/**
	 * @return the publicImageNum
	 */
	public Integer getPublicImageNum() {
		return publicImageNum;
	}

	/**
	 * @param publicImageNum
	 *            the publicImageNum to set
	 */
	public void setPublicImageNum(Integer publicImageNum) {
		this.publicImageNum = publicImageNum;
	}

	/**
	 * @return the privateImageNum
	 */
	public Integer getPrivateImageNum() {
		return privateImageNum;
	}

	/**
	 * @param privateImageNum
	 *            the privateImageNum to set
	 */
	public void setPrivateImageNum(Integer privateImageNum) {
		this.privateImageNum = privateImageNum;
	}

	/**
	 * @return the environments
	 */
	public List<Environment> getEnvironments() {
		return environments;
	}

	/**
	 * @param environments
	 *            the environments to set
	 */
	public void setEnvironments(List<Environment> environments) {
		this.environments = environments;
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
	 * @return the appMap
	 */
	public Map<String, List<ApplicationBean>> getAppMap() {
		return appMap;
	}

	/**
	 * @param appMap
	 *            the appMap to set
	 */
	public void setAppMap(Map<String, List<ApplicationBean>> appMap) {
		this.appMap = appMap;
	}

	/**
	 * @return the pvUsedTotal
	 */
	public Integer getPvUsedTotal() {
		return pvUsedTotal;
	}

	/**
	 * @param pvUsedTotal
	 *            the pvUsedTotal to set
	 */
	public void setPvUsedTotal(Integer pvUsedTotal) {
		this.pvUsedTotal = pvUsedTotal;
	}

	/**
	 * @return the pvUsedNum
	 */
	public Integer getPvUsedNum() {
		return pvUsedNum;
	}

	/**
	 * @param pvUsedNum
	 *            the pvUsedNum to set
	 */
	public void setPvUsedNum(Integer pvUsedNum) {
		this.pvUsedNum = pvUsedNum;
	}

	/**
	 * @return the abnormalHostNum
	 */
	public Integer getAbnormalHostNum() {
		return abnormalHostNum;
	}

	/**
	 * @param abnormalHostNum
	 *            the abnormalHostNum to set
	 */
	public void setAbnormalHostNum(Integer abnormalHostNum) {
		this.abnormalHostNum = abnormalHostNum;
	}

	/**
	 * @return the normalHostNum
	 */
	public Integer getNormalHostNum() {
		return normalHostNum;
	}

	/**
	 * @param normalHostNum
	 *            the normalHostNum to set
	 */
	public void setNormalHostNum(Integer normalHostNum) {
		this.normalHostNum = normalHostNum;
	}
	

	/**
	 * @return the addingHostNum
	 */
	public Integer getAddingHostNum() {
		return addingHostNum;
	}

	/**
	 * @param addingHostNum the addingHostNum to set
	 */
	public void setAddingHostNum(Integer addingHostNum) {
		this.addingHostNum = addingHostNum;
	}

	/**
	 * @return the outingHostNum
	 */
	public Integer getOutingHostNum() {
		return outingHostNum;
	}

	/**
	 * @param outingHostNum the outingHostNum to set
	 */
	public void setOutingHostNum(Integer outingHostNum) {
		this.outingHostNum = outingHostNum;
	}

	/**
	 * @param appTotal
	 * @param appRunNum
	 * @param podTotal
	 * @param podRunTotal
	 * @param appPodNum
	 * @param appRunPodNum
	 * @param servicePodNum
	 * @param serviceRunPodNum
	 * @param pvTotal
	 * @param pvCapacityTotal
	 * @param nodeTotal
	 * @param scheduNodeNum
	 * @param unscheduNodeNum
	 * @param imageTotal
	 * @param publicImageNum
	 * @param privateImageNum
	 * @param environments
	 * @param status
	 * @param appMap
	 */
	public StatisticBean(Integer appTotal, Integer appRunNum, Integer podTotal, Integer podRunTotal, Integer appPodNum,
			Integer appRunPodNum, Integer servicePodNum, Integer serviceRunPodNum, Integer pvTotal,
			Integer pvCapacityTotal, Integer nodeTotal, Integer scheduNodeNum, Integer unscheduNodeNum,
			Integer imageTotal, Integer publicImageNum, Integer privateImageNum, List<Environment> environments,
			String status, Map<String, List<ApplicationBean>> appMap, Integer pvUsedTotal, Integer pvUsedNum,
			Integer abnormalHostNum, Integer normalHostNum, Integer addingHostNum, Integer outingHostNum) {
		super();
		this.appTotal = appTotal;
		this.appRunNum = appRunNum;
		this.podTotal = podTotal;
		this.podRunTotal = podRunTotal;
		this.appPodNum = appPodNum;
		this.appRunPodNum = appRunPodNum;
		this.servicePodNum = servicePodNum;
		this.serviceRunPodNum = serviceRunPodNum;
		this.pvTotal = pvTotal;
		this.pvCapacityTotal = pvCapacityTotal;
		this.nodeTotal = nodeTotal;
		this.scheduNodeNum = scheduNodeNum;
		this.unscheduNodeNum = unscheduNodeNum;
		this.imageTotal = imageTotal;
		this.publicImageNum = publicImageNum;
		this.privateImageNum = privateImageNum;
		this.environments = environments;
		this.status = status;
		this.appMap = appMap;
		this.pvUsedTotal = pvUsedTotal;
		this.pvUsedNum = pvUsedNum;
		this.abnormalHostNum = abnormalHostNum;
		this.normalHostNum = normalHostNum;
		this.addingHostNum = addingHostNum;
		this.outingHostNum = outingHostNum;
	}

	/**
	 * 
	 */
	public StatisticBean() {
		super();
	}

}
