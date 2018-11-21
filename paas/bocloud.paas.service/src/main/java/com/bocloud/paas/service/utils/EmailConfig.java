package com.bocloud.paas.service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * describe: 邮件发送-发送人信息类
 * @author Zaney
 * @data 2017年11月6日
 */
@Component("emailConfig")
public class EmailConfig {
	@Value("${email.host}")
	private String host;
	@Value("${email.from}")
	private String sender; //发送人
	@Value("${email.account}")
	private String account;  //发送人账号
	@Value("${email.password}")
	private String password; 
	@Value("${email.port}")
	private Integer port;
	@Value("${email.protocol}")
	private String protocol;
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
}
