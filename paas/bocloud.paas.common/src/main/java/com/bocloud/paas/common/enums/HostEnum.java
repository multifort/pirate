package com.bocloud.paas.common.enums;

public enum HostEnum {

	NORMAL("1", "正常"), ABNORMAL("2", "不正常"), SCHEDUABLE("3", "可调度"), UNSCHEDUABLE("4", "不可调度"), ADDING("5",
			"添加中"), OUTING("6", "移出中");

	private String code;
	private String desc;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
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
	private HostEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

}
