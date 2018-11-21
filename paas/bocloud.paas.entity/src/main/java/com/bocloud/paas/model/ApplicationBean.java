package com.bocloud.paas.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.bocloud.common.utils.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ApplicationBean {

	private String name;
	@JsonSerialize(using = DateSerializer.class)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date gmtCreate;// 创建时间
	@JsonSerialize(using = DateSerializer.class)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date gmtModify;// 更改时间
	private Integer imageNum;// 使用镜像数量
	private Integer memoryUsed;// 内存使用量
	private Integer cpuUesd;// cpu使用量
	private Integer appTotal;// 应用总数
	private Integer appRunTotal;// 运行中应用

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the gmtCreate
	 */
	public Date getGmtCreate() {
		return gmtCreate;
	}

	/**
	 * @param gmtCreate
	 *            the gmtCreate to set
	 */
	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	/**
	 * @return the imageNum
	 */
	public Integer getImageNum() {
		return imageNum;
	}

	/**
	 * @param imageNum
	 *            the imageNum to set
	 */
	public void setImageNum(Integer imageNum) {
		this.imageNum = imageNum;
	}

	/**
	 * @return the memoryUsed
	 */
	public Integer getMemoryUsed() {
		return memoryUsed;
	}

	/**
	 * @param memoryUsed
	 *            the memoryUsed to set
	 */
	public void setMemoryUsed(Integer memoryUsed) {
		this.memoryUsed = memoryUsed;
	}

	/**
	 * @return the cpuUesd
	 */
	public Integer getCpuUesd() {
		return cpuUesd;
	}

	/**
	 * @param cpuUesd
	 *            the cpuUesd to set
	 */
	public void setCpuUesd(Integer cpuUesd) {
		this.cpuUesd = cpuUesd;
	}

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
	 * @return the appRunTotal
	 */
	public Integer getAppRunTotal() {
		return appRunTotal;
	}

	/**
	 * @param appRunTotal
	 *            the appRunTotal to set
	 */
	public void setAppRunTotal(Integer appRunTotal) {
		this.appRunTotal = appRunTotal;
	}
	

	/**
	 * @return the gmtModify
	 */
	public Date getGmtModify() {
		return gmtModify;
	}

	/**
	 * @param gmtModify the gmtModify to set
	 */
	public void setGmtModify(Date gmtModify) {
		this.gmtModify = gmtModify;
	}


	/**
	 * @param name
	 * @param gmtCreate
	 * @param gmtModify
	 * @param imageNum
	 * @param memoryUsed
	 * @param cpuUesd
	 * @param appTotal
	 * @param appRunTotal
	 */
	public ApplicationBean(String name, Date gmtCreate, Date gmtModify, Integer imageNum, Integer memoryUsed,
			Integer cpuUesd, Integer appTotal, Integer appRunTotal) {
		super();
		this.name = name;
		this.gmtCreate = gmtCreate;
		this.gmtModify = gmtModify;
		this.imageNum = imageNum;
		this.memoryUsed = memoryUsed;
		this.cpuUesd = cpuUesd;
		this.appTotal = appTotal;
		this.appRunTotal = appRunTotal;
	}

	/**
	 * 
	 */
	public ApplicationBean() {
		super();
	}

}
