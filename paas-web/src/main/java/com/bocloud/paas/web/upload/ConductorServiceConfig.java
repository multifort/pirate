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
public class ConductorServiceConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

	@Autowired
	private ConductorServiceHandller conductorServiceHandller;

	@Autowired
	private MessageHandshakeInterceptor messageHandshakeInterceptor;

	public ConductorServiceConfig() {
	}
	// @Autowired
	// public ConductorServiceConfig(ConductorServiceHandller
	// conductorServiceHandller,
	// MessageHandshakeInterceptor messageHandshakeInterceptor) {
	// this.conductorServiceHandller = conductorServiceHandller;
	// this.messageHandshakeInterceptor = messageHandshakeInterceptor;
	// }

	@Bean
	public DefaultHandshakeHandler handshakeHandler() {
		return new DefaultHandshakeHandler();
	}

	/**
	 * @return the conductorServiceHandller
	 */
	public ConductorServiceHandller getConductorServiceHandller() {
		return conductorServiceHandller;
	}

	/**
	 * @param conductorServiceHandller
	 *            the conductorServiceHandller to set
	 */
	public void setConductorServiceHandller(ConductorServiceHandller conductorServiceHandller) {
		this.conductorServiceHandller = conductorServiceHandller;
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
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(8192);
		container.setMaxBinaryMessageBufferSize(5 * 1024 * 1024);
		return container;
	}

	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(this.conductorServiceHandller, "conductorService")
				.addInterceptors(this.messageHandshakeInterceptor).setHandshakeHandler(handshakeHandler());
		registry.addHandler(this.conductorServiceHandller, "/sockjs/conductorService")
				.addInterceptors(this.messageHandshakeInterceptor).setHandshakeHandler(handshakeHandler()).withSockJS();
	}

}
