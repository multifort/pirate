package com.bocloud.paas.service.event.model;

import com.bocloud.common.model.Result;

public class OperateResult extends Result {

	private String operate;
	private Object data;
	private Long userId;

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the operate
	 */
	public String getOperate() {
		return operate;
	}

	/**
	 * @param operate
	 *            the operate to set
	 */
	public void setOperate(String operate) {
		this.operate = operate;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	public OperateResult() {
		super();
	}

	public OperateResult(String operate, Object data) {
		super();
		this.operate = operate;
		this.data = data;
	}

	public OperateResult(boolean success, String message) {
		super(success, message);
	}

	/**
	 * @param success
	 * @param message
	 * @param operate
	 * @param data
	 */
	public OperateResult(boolean success, String message, String operate, Object data) {
		super(success, message);
		this.operate = operate;
		this.data = data;
	}

	/**
	 * @param success
	 * @param message
	 * @param operate
	 */
	public OperateResult(boolean success, String message, String operate) {
		super(success, message);
		this.operate = operate;
	}

	public OperateResult(boolean success, String message, String operate, Object data, Long userId) {
		super(success, message);
		this.operate = operate;
		this.data = data;
		this.userId = userId;
	}

}
