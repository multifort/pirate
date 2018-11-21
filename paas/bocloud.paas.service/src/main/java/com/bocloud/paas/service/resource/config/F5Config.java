package com.bocloud.paas.service.resource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * F5访问配置相关类
 * @author caidongqing
 *
 */
@Component("F5Config")
public class F5Config {

	@Value("${f5.ip}")
	private String f5Ip;
	
	@Value("${f5.port}")
	private int f5Port;
	
	@Value("${f5.user}")
	private String f5User;
	
	@Value("${f5.password}")
	private String password;
	
	public String getF5Ip() {
		return f5Ip;
	}

	public void setF5Ip(String f5Ip) {
		this.f5Ip = f5Ip;
	}

	public int getF5Port() {
		return f5Port;
	}

	public void setF5Port(int f5Port) {
		this.f5Port = f5Port;
	}

	public String getF5User() {
		return f5User;
	}

	public void setF5User(String f5User) {
		this.f5User = f5User;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
