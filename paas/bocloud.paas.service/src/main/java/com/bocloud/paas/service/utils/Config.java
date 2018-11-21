package com.bocloud.paas.service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("config")
public class Config {
	@Value("${ansible.host.ip}")
	private String ansibleHostIp;
	@Value("${ansible.host.username}")
	private String ansibleHostUsername;
	@Value("${ansible.host.password}")
	private String ansibleHostPassword;

	/**
	 * @return the ansibleHostIp
	 */
	public String getAnsibleHostIp() {
		return ansibleHostIp;
	}

	/**
	 * @param ansibleHostIp
	 *            the ansibleHostIp to set
	 */
	public void setAnsibleHostIp(String ansibleHostIp) {
		this.ansibleHostIp = ansibleHostIp;
	}

	/**
	 * @return the ansibleHostUsername
	 */
	public String getAnsibleHostUsername() {
		return ansibleHostUsername;
	}

	/**
	 * @param ansibleHostUsername
	 *            the ansibleHostUsername to set
	 */
	public void setAnsibleHostUsername(String ansibleHostUsername) {
		this.ansibleHostUsername = ansibleHostUsername;
	}

	/**
	 * @return the ansibleHostPassword
	 */
	public String getAnsibleHostPassword() {
		return ansibleHostPassword;
	}

	/**
	 * @param ansibleHostPassword
	 *            the ansibleHostPassword to set
	 */
	public void setAnsibleHostPassword(String ansibleHostPassword) {
		this.ansibleHostPassword = ansibleHostPassword;
	}

}
