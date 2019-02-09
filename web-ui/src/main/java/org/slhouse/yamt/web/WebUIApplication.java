package org.slhouse.yamt.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WebUIApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebUIApplication.class, args);
	}

}

