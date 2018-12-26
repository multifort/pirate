package com.pirates.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@SpringBootApplication
@RestController
@EnableFeignClients
public class PiratesConsumerApplication {

    @Resource
    private HelloService helloService;

    @GetMapping("/actuator/health")
    public String health() {
        return "SUCCESS";
    }

    @GetMapping("/hello")
    public String sayHello(String name) {
        return helloService.sayHello(name);
    }

    public static void main(String[] args) {
        SpringApplication.run(PiratesConsumerApplication.class, args);
    }

}

