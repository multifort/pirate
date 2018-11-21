package com.bocloud.paas.service.process.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("jenkinsConfig")
public class JenkinsConfig {
	@Value("${jenkins.server.uri}")
	private String uri;//服务地址
	@Value("${jenkins.username}")
	private String username;//用户名
	@Value("${jenkins.password}")
	private String password;//密码
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
