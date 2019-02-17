/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author Yaroslav V. Khazanov
 **/
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
public class MvcConfig implements WebFluxConfigurer {
    @Autowired
    ReactiveClientRegistrationRepository authorizedRepository;
    @Autowired
    ServerOAuth2AuthorizedClientRepository clientRepository;
    @Autowired
    private Environment env;


    @Bean
    @LoadBalanced
    public WebClient.Builder webClient() {
        ServerOAuth2AuthorizedClientExchangeFilterFunction function = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedRepository, clientRepository);
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder().codecs(c -> c.defaultCodecs().enableLoggingRequestDetails(true)).build()) // enable headers logging
                .baseUrl("http://quoteservice")
                .filter(function) // this adds Bearer authentication to webClient. supports refresh token
                ;

    }


/*  // Different ways to include http headers and body information to logs
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().enableLoggingRequestDetails(true); // this allows to see more details on a log
    }


    @Configuration
    @AutoConfigureAfter({WebClientAutoConfiguration.class })
    public class ClientCodecConfigurer extends DefaultClientCodecConfigurer {
        public ClientCodecConfigurer() {
            super();
            defaultCodecs().enableLoggingRequestDetails(true);
            for (HttpMessageWriter<?> w : getWriters()) {
                if (w instanceof LoggingCodecSupport)
                    ((LoggingCodecSupport) w).setEnableLoggingRequestDetails(true);
            }
        }

    }
*/

    // for @YamtRegisteredUser to work in controllers
    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new RegisteredUserArgumentResolver());
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange()
            .pathMatchers("/", "/webjars/**").permitAll()
            .anyExchange().authenticated()
        .and()
            .oauth2Login()
        .and()
            .oauth2Client()
        .and() // seems that it's not really needed here
            .oauth2ResourceServer()
                .jwt().jwtDecoder(multipleServersJWTDecoder())
        ;
        return http.build();
    }

    // This helps get JWT sign public keys from different authorization servers
    private ReactiveJwtDecoder multipleServersJWTDecoder() {
        return new ReactiveJwtDecoder() {
            @Override
            public Mono<Jwt> decode(String token) throws JwtException {
                try {
                    final JWT jwt = JWTParser.parse(token);
                    final String issuer = jwt.getJWTClaimsSet().getIssuer();
                    // todo check "iss" from JWT, not from property, create issuerUri, then delegate the call to NimbusReactiveJwtDecoder(issuer's jwk-set-uri)
                    // don't create it each time, keep some cache
                    return new NimbusReactiveJwtDecoder(Objects.requireNonNull(env.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri"))).decode(token);
                } catch (Exception ex) {
                    throw new JwtException("An error occurred while attempting to decode the Jwt: " + ex.getMessage(), ex);
                }
            }
        };
    }
}
