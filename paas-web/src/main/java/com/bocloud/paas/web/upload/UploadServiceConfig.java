package com.bocloud.paas.web.upload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.bocloud.paas.web.message.MessageHandshakeInterceptor;

@Configuration
@EnableWebMvc
@EnableWebSocket
public class UploadServiceConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {
	@Autowired
	private UploadServiceHandller uploadServiceHandller;
	@Autowired
	private MessageHandshakeInterceptor messageHandshakeInterceptor;

	// public UploadServiceConfig() {
	// }
	//
	// @Autowired
	// public UploadServiceConfig(UploadServiceHandller uploadServiceHandller,
	// MessageHandshakeInterceptor messageHandshakeInterceptor) {
	// this.uploadServiceHandller = uploadServiceHandller;
	// this.messageHandshakeInterceptor = messageHandshakeInterceptor;
	// }

	@Bean
	public DefaultHandshakeHandler handshakeHandler() {
		return new DefaultHandshakeHandler();
	}

	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(8192);
		container.setMaxBinaryMessageBufferSize(5 * 1024 * 1024);
		return container;
	}

	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(this.uploadServiceHandller, "uploadService")
				.addInterceptors(this.messageHandshakeInterceptor).setHandshakeHandler(handshakeHandler());
		registry.addHandler(this.uploadServiceHandller, "/sockjs/uploadService")
				.addInterceptors(this.messageHandshakeInterceptor).setHandshakeHandler(handshakeHandler()).withSockJS();
	}

	/**
	 * @return the uploadServiceHandller
	 */
	public UploadServiceHandller getUploadServiceHandller() {
		return uploadServiceHandller;
	}

	/**
	 * @param uploadServiceHandller
	 *            the uploadServiceHandller to set
	 */
	public void setUploadServiceHandller(UploadServiceHandller uploadServiceHandller) {
		this.uploadServiceHandller = uploadServiceHandller;
	}

	/**
	 * @return the messageHandshakeInterceptor
	 */
	public MessageHandshakeInterceptor getMessageHandshakeInterceptor() {
		return messageHandshakeInterceptor;
	}

	/**
	 * @param messageHandshakeInterceptor
	 *            the messageHandshakeInterceptor to set
	 */
	public void setMessageHandshakeInterceptor(MessageHandshakeInterceptor messageHandshakeInterceptor) {
		this.messageHandshakeInterceptor = messageHandshakeInterceptor;
	}

}
