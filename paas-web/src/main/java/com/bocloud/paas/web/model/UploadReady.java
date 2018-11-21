package com.bocloud.paas.web.model;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * 准备文件
 * @author lenovo
 *
 */
public class UploadReady implements Message<Object> {
	private MessageCategory category;
	private String content;


	/**
	 * @return the category
	 */
	public MessageCategory getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(MessageCategory category) {
		this.category = category;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public MessageHeaders getHeaders() {
		return null;
	}

	@Override
	public Object getPayload() {
		return null;
	}

	public UploadReady(String content) {
		this.setCategory(MessageCategory.UPLOAD_READY);
		this.setContent(content);
	}
}