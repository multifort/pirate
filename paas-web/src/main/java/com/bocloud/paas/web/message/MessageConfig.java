package com.bocloud.paas.web.message;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebMvc
@EnableWebSocket
public class MessageConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

	@Resource
	private MessageWebSocketHandler messageWebSocketHandler;
	@Resource
	private MessageHandshakeInterceptor messageHandshakeInterceptor;

	@Bean
	public DefaultHandshakeHandler handshakeHandler() {
		return new DefaultHandshakeHandler();
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(messageWebSocketHandler, "/messageService").addInterceptors(messageHandshakeInterceptor)
				.setHandshakeHandler(handshakeHandler());
		registry.addHandler(messageWebSocketHandler, "/sockjs/messageService")
				.addInterceptors(messageHandshakeInterceptor).setHandshakeHandler(handshakeHandler()).withSockJS();

	}

}
