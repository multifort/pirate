package com.bocloud.paas.service.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("stiConfig")
public class STIConfig {

	@Value("${sti.url}")
	private String stiUrl;

	public String getStiUrl() {
		return stiUrl;
	}

	public void setStiUrl(String stiUrl) {
		this.stiUrl = stiUrl;
	}

}
