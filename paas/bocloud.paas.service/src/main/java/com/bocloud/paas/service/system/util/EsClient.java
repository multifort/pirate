package com.bocloud.paas.service.system.util;

import java.net.InetAddress;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bocloud.paas.service.system.config.EsConfig;

@Component("esClient")
public class EsClient {

	private static Logger logger = LoggerFactory.getLogger(EsClient.class);

	public static Client client = null;

	@Autowired
	private EsConfig esConfig;

	@SuppressWarnings("resource")
	public Client getClient() {

		try {
			// client settings
			Settings settings = Settings.builder()
					.put("cluster.name", esConfig.getEsName())
					.put("client.transport.sniff", false).build();
			client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(
							InetAddress.getByName(esConfig.getEsUrl()), Integer
									.parseInt(esConfig.getEsPort())));
			logger.info("this is EsClient:" + client);
		} catch (Exception e) {
			logger.error("EsClient连接失败！");
		}
		return client;
	}

	public void closeClient() {
		client.close();
	}

}
