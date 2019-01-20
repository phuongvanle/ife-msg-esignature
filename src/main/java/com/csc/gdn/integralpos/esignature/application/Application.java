package com.csc.gdn.integralpos.esignature.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
@EnableConfigurationProperties
@ComponentScan(basePackages = "com.csc")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
