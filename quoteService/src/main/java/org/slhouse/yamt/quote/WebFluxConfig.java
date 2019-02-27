/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.quote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

/**
 * @author Yaroslav V. Khazanov
 **/
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebFluxConfig implements WebFluxConfigurer {
    @Autowired
    OAuth2ResourceServerProperties resourceServerProperties;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt()
                    .jwtDecoder(decoder())// this is really needed only when starting from tests. Otherwise it's created automatically
        ;
        return http.build();
    }

    private ReactiveJwtDecoder decoder() {
        return new NimbusReactiveJwtDecoder(Objects.requireNonNull(resourceServerProperties.getJwt().getJwkSetUri()));
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().enableLoggingRequestDetails(true); // this allows to see more details on a log
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder().codecs(c -> c.defaultCodecs().enableLoggingRequestDetails(true)).build()) // enable headers logging
                .build();

    }

    // For things like @PreAuthorize("#jwt.hasScope()") to work
    @Bean
    @Primary
    public DefaultMethodSecurityExpressionHandler reactiveMethodSecurityExpressionHandler() {
        return new JwtMethodSecurityExpressionHandler();
    }



    @Override
    public void configurePathMatching(PathMatchConfigurer configurer) {
        // to make all RestControllers answer on /api only
//        configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class));
    }

}
