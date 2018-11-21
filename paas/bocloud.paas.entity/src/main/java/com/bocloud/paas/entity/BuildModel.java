package com.bocloud.paas.entity;
/**
 * describe: job构建历史记录信息类
 * @author Zaney
 * @data 2017年7月24日
 */
public class BuildModel {
	private int number;
    private int queueId;
    private String url;
    private String createTime;
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getQueueId() {
		return queueId;
	}
	public void setQueueId(int queueId) {
		this.queueId = queueId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
    
}
