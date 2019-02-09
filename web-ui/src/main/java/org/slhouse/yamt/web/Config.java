/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Yaroslav V. Khazanov
 **/
@Configuration
@EnableWebFlux
public class Config implements WebFluxConfigurer {
    @Bean
    @LoadBalanced // to be used with service discovery
    WebClient.Builder webClient() {
        return WebClient.builder().baseUrl("http://quoteservice");
    }


}
