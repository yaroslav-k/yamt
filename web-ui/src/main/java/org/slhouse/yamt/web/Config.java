/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Yaroslav V. Khazanov
 **/
@Configuration
//@EnableWebFlux // not needed for now. would require configuration for correct view resolving.
public class Config /*extends WebFluxAutoConfiguration */{
    @Bean("quoteServiceWebClient")
    @LoadBalanced // to be used with service discovery
    WebClient.Builder quoteServiceWebClient() {
        return WebClient.builder().baseUrl("http://quoteservice"); // would need some tweaking for test, see org.slhouse.yamt.web.RestAPIController.quoteServiceURI
    }



}
