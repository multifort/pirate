package com.bocloud.paas.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.bocloud.common.utils.DateSerializer;
import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.Table;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Zaney
 * @data:2018年3月38日
 * @describe: GPU监控类
 */
@Table("gpu_monitor")
public class GpuMonitor {
	
	private Integer id;
	private Integer gpuNumber;  //gpu序列号
	private String gpuName;
	private String gpuModel;  //gpu型号
	private String gpuTemp;   //gpu温度
	private String gpuUsage;  //gpu使用率
	private String memoryUsage;  //gpu内存使用量
	private String memoryTotal;  //gpu内存总量
	private String hostName;  //主机名称
	@Column("time")
	@JsonSerialize(using = DateSerializer.class)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date time;
	public Integer getId() {
		return id;
	}
	public String getGpuUsage() {
		return gpuUsage;
	}
	public String getMemoryUsage() {
		return memoryUsage;
	}
	public String getMemoryTotal() {
		return memoryTotal;
	}
	public String getHostName() {
		return hostName;
	}
	public Date getTime() {
		return time;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public void setGpuUsage(String gpuUsage) {
		this.gpuUsage = gpuUsage;
	}
	public void setMemoryUsage(String memoryUsage) {
		this.memoryUsage = memoryUsage;
	}
	public void setMemoryTotal(String memoryTotal) {
		this.memoryTotal = memoryTotal;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public Integer getGpuNumber() {
		return gpuNumber;
	}
	public void setGpuNumber(Integer gpuNumber) {
		this.gpuNumber = gpuNumber;
	}
	public String getGpuName() {
		return gpuName;
	}
	public void setGpuName(String gpuName) {
		this.gpuName = gpuName;
	}
	public String getGpuModel() {
		return gpuModel;
	}
	public void setGpuModel(String gpuModel) {
		this.gpuModel = gpuModel;
	}
	public String getGpuTemp() {
		return gpuTemp;
	}
	public void setGpuTemp(String gpuTemp) {
		this.gpuTemp = gpuTemp;
	}

}
