package com.pirates.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PiratesGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PiratesGatewayApplication.class, args);
	}

}

