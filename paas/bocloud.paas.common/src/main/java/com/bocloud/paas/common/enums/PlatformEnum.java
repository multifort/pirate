package com.bocloud.paas.common.enums;

public enum PlatformEnum {

	KUBERNETES(1, "kubernetes");

	private Integer code;
	private String desc;

	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc
	 *            the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @param code
	 * @param desc
	 */
	private PlatformEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

}
