package com.bocloud.paas.service.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("esConfig")
public class EsConfig {

	@Value("${es.name}")
	private String esName;
	@Value("${es.url}")
	private String esUrl;
	@Value("${es.port}")
	private String esPort;

	public String getEsName() {
		return esName;
	}

	public void setEsName(String esName) {
		this.esName = esName;
	}

	public String getEsUrl() {
		return esUrl;
	}

	public void setEsUrl(String esUrl) {
		this.esUrl = esUrl;
	}

	public String getEsPort() {
		return esPort;
	}

	public void setEsPort(String esPort) {
		this.esPort = esPort;
	}

}