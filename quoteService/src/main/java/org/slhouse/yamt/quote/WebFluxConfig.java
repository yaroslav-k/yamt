/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.quote;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Yaroslav V. Khazanov
 **/
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebFluxConfig implements WebFluxConfigurer {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt().jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter()))
        ;
        return http.build();
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

    // For things like @PreAuthorize("#oauth2.isOAuth()") to work
    // It's doesn't work now anyway, because OAuth2SecurityExpressionMethods waits for OAuth2Authentication, while
    // we have OAuth2AuthenticationToken, or JwtAuthenticationToken. So will override OAuth2SecurityExpressionMethods later
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
