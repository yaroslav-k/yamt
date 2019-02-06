/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 * Spring Cloud Config documentation can be found here:
 * https://cloud.spring.io/spring-cloud-config/single/spring-cloud-config.html
 */

package org.slhouse.yamt.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class YamtConfigurationServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(YamtConfigurationServerApplication.class, args);
	}

}

