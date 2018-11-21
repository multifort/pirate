package com.bocloud.paas.model;

public class Labels {

	private String key;
	private String value;

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @param key
	 * @param value
	 */
	public Labels(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * 
	 */
	public Labels() {
		super();
	}

}
