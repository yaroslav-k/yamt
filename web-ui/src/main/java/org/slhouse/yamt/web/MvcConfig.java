/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.util.Objects;

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
    // For things like @PreAuthorize("#oauth2.isOAuth()") to work
    // It's doesn't work now anyway, because OAuth2SecurityExpressionMethods waits for OAuth2Authentication, while
    // we have OAuth2AuthenticationToken, or JwtAuthenticationToken. So will override OAuth2SecurityExpressionMethods later
    @Bean
    @Primary
    public DefaultMethodSecurityExpressionHandler reactiveMethodSecurityExpressionHandler() {
        return new OAuth2MethodSecurityExpressionHandler();
    }


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
/* // here we could check if the access token expired and deny access
                .access(new ReactiveAuthorizationManager<AuthorizationContext>() {
                            @Override
                            public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
                                return authentication
                                        .map(a -> {
                                            if (!(a instanceof OAuth2AuthenticationToken))
                                                return new AuthorizationDecision(a.isAuthenticated());
                                            // @see org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.shouldRefresh
                                            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) a;
                                            if (!(token.getPrincipal() instanceof DefaultOidcUser))
                                                return new AuthorizationDecision(a.isAuthenticated());
                                            DefaultOidcUser idcUser = (DefaultOidcUser) token.getPrincipal();
                                            Instant now = Clock.systemUTC().instant();
                                            Instant expiresAt = idcUser.getExpiresAt();
                                            if (now.isAfter(expiresAt.minus(Duration.ofMinutes(1)))) {
                                                return new AuthorizationDecision(false);
                                            }
                                            return new AuthorizationDecision(a.isAuthenticated());
                                        })
                                        .defaultIfEmpty(new AuthorizationDecision(false));
                            }
                })
*/
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
/*  // here, when the access is denied, we could redirect user somewhere. to auth server, for example. though actually we'd need to merely refresh token.
                .exceptionHandling().accessDeniedHandler(new ServerAccessDeniedHandler() {
            @Override
            public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
                return new DefaultServerRedirectStrategy().sendRedirect(exchange, URI.create("/"));
            }
        })
        .and()
*/
            .logout()//.logoutSuccessHandler(new RedirectServerLogoutSuccessHandler()) // todo we'll need to logout on the auth server
        ;
        return http.build();
        // @formatter:off
    }

    private ReactiveJwtDecoder decoder() {
        return new NimbusReactiveJwtDecoder(Objects.requireNonNull(env.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri")));
    }
}
