package com.bocloud.paas.web.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MessageWebSocketHandler implements WebSocketHandler {

	private static final Logger logger = Logger.getLogger(MessageWebSocketHandler.class);

	private static List<WebSocketSession> currentUsers;

	public static List<WebSocketSession> getCurrentUsers() {
		return currentUsers;
	}

	private static void setCurrentUsers(List<WebSocketSession> currentUsers) {
		MessageWebSocketHandler.currentUsers = currentUsers;
	}

	static {
		MessageWebSocketHandler.setCurrentUsers(new ArrayList<WebSocketSession>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.socket.WebSocketHandler#
	 * afterConnectionEstablished(org.springframework.web.socket.
	 * WebSocketSession)
	 */
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		MessageWebSocketHandler.getCurrentUsers().add(session);
		JSONObject user = (JSONObject) session.getAttributes().get("user");
		if (user != null) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.socket.WebSocketHandler#handleMessage(org.
	 * springframework.web.socket.WebSocketSession,
	 * org.springframework.web.socket.WebSocketMessage)
	 */
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.socket.WebSocketHandler#handleTransportError(org.
	 * springframework.web.socket.WebSocketSession, java.lang.Throwable)
	 */
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		MessageWebSocketHandler.getCurrentUsers().remove(session);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.socket.WebSocketHandler#afterConnectionClosed(org
	 * .springframework.web.socket.WebSocketSession,
	 * org.springframework.web.socket.CloseStatus)
	 */
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		MessageWebSocketHandler.getCurrentUsers().remove(session);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.socket.WebSocketHandler#supportsPartialMessages()
	 */
	public boolean supportsPartialMessages() {
		return false;
	}

	public void sendMessageToUsers(Message message) {
		for (WebSocketSession user : MessageWebSocketHandler.getCurrentUsers()) {
			try {
				if (user.isOpen()) {
					user.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(message)));
				}
			} catch (IOException e) {
				logger.error("send message error", e);
			}
		}
	}

	public void sendMessageToUser(int userId, Message message) {
		for (WebSocketSession user : MessageWebSocketHandler.getCurrentUsers()) {
			JSONObject iterator = (JSONObject) user.getAttributes().get("user");
			if (iterator != null) {
				if ((int) iterator.get("id") == userId) {
					try {
						if (user.isOpen()) {
							user.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(message)));
						}
					} catch (IOException e) {
						logger.error("send message error", e);
					}
				}
			}
		}
	}

}