package com.bocloud.paas.web.message;

public class StickyMessage implements Message {
	private String messageType;
	private String content;

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public StickyMessage(String content) {
		this.setMessageType(MessageType.STICKY);
		this.setContent(content);
	}
}
