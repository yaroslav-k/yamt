/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 * Spring Reactive Stack documentation:
 * https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html
 */

package org.slhouse.yamt.quote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class QuoteServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuoteServiceApplication.class, args);
	}

}

