package com.bocloud.paas.service.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("applicationConfig")
public class ApplicationConfig {
	
	@Value("${application.store.namespace}")
	private String namespace;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
}
