package com.bocloud.paas.service.application.model;

/**
 * describe: 用于配置管理存放data值的对象,也可以用于所有map类型的值
 * @author Zaney
 * @data 2017年10月17日
 */
public class DataMap {
	private String key;
	private String value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public DataMap() {
		super();
	}
	
	public DataMap(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
}
