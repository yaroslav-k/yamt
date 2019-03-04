/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.net.URI;

/**
 * @author Yaroslav V. Khazanov
 **/
@EnableWebFluxSecurity
@Slf4j
@EnableReactiveMethodSecurity
//@EnableOAuth2Client  //reactive spring doesn't work well enough with OAuth2 authentication.
// in particular, it doesn't support token refresh when accessing resource server via browser
// see https://github.com/spring-projects/spring-security/issues/5330
public class MvcConfig implements WebFluxConfigurer {
    @Autowired
    private ReactiveClientRegistrationRepository authorizedRepository;
    @Autowired
    private ServerOAuth2AuthorizedClientRepository clientRepository;
    @Autowired
    private OAuth2ResourceServerProperties resourceServerProperties;

    @Value("${authserver.url}")
    String authserverURL;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClient() {
        ServerOAuth2AuthorizedClientExchangeFilterFunction function = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedRepository, clientRepository);
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder().codecs(c -> c.defaultCodecs().enableLoggingRequestDetails(true)).build()) // enable headers logging
                .baseUrl("lb://quoteservice") // http://quoteservice also works
                .filter(function) // this adds Bearer authentication to webClient. supports refresh token
                ;

    }


    // for @YamtRegisteredUser to work in controllers
    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new RegisteredUserArgumentResolver());
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        final RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create(authserverURL + "/logout/token"));

        http
            .authorizeExchange()
                .pathMatchers("/").permitAll()
                .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // short way to allow static resources
                .anyExchange().authenticated()
        .and()
            .oauth2Login()
        .and()
            .oauth2Client()
        .and() // seems that it's not really needed here
            .oauth2ResourceServer()
                .jwt()
                    .jwtDecoder(decoder())
                .and()
        .and()
            .logout().logoutSuccessHandler(logoutSuccessHandler)
        ;
        return http.build();
    }

    private ReactiveJwtDecoder decoder() {
        return new NimbusReactiveJwtDecoder(resourceServerProperties.getJwt().getJwkSetUri());
    }
}
