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
public class UploadPictureConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

	@Autowired
	private UploadPictureHandller uploadPictureHandller;

	@Autowired
	private MessageHandshakeInterceptor messageHandshakeInterceptor;

	public UploadPictureConfig() {
	}
//
//	@Autowired
//	public UploadPictureConfig(UploadPictureHandller uploadPictureHandller,
//			MessageHandshakeInterceptor messageHandshakeInterceptor) {
//		this.uploadPictureHandller = uploadPictureHandller;
//		this.messageHandshakeInterceptor = messageHandshakeInterceptor;
//	}

	/**
	 * @return the uploadPictureHandller
	 */
	public UploadPictureHandller getUploadPictureHandller() {
		return uploadPictureHandller;
	}

	/**
	 * @param uploadPictureHandller
	 *            the uploadPictureHandller to set
	 */
	public void setUploadPictureHandller(UploadPictureHandller uploadPictureHandller) {
		this.uploadPictureHandller = uploadPictureHandller;
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

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		System.err.println("22222" + this.uploadPictureHandller);
		registry.addHandler(this.uploadPictureHandller, "uploadPicture")
				.addInterceptors(this.messageHandshakeInterceptor).setHandshakeHandler(handshakeHandler());
		registry.addHandler(this.uploadPictureHandller, "/sockjs/uploadPicture")
				.addInterceptors(this.messageHandshakeInterceptor).setHandshakeHandler(handshakeHandler()).withSockJS();
	}
}
