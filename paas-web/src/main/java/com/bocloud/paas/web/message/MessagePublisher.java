package com.bocloud.paas.web.message;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class MessagePublisher {

	@Resource
	private MessageWebSocketHandler messageWebSocketHandler;
	public void pushMessage(final int userId, final String content) {
		messageWebSocketHandler.sendMessageToUser(userId, new StickyMessage(content));
	}

	public void pushMessage(final String content) {
		messageWebSocketHandler.sendMessageToUsers(new StickyMessage(content));
	}
}
