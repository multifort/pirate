package com.pirates.pirateregister;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PirateRegisterApplication {

	public static void main(String[] args) {
		SpringApplication.run(PirateRegisterApplication.class, args);
	}

	@GetMapping("/actuator/health")
	public String health(){
		return "SUCCESS";
	}

	@GetMapping("/sayHello")
	public String sayHello(String name){
		return "Hello" + name;
	}
}

