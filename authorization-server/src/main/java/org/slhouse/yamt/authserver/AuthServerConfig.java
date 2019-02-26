/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.authserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Yaroslav V. Khazanov
 * // WebClientReactiveAuthorizationCodeTokenResponseClient, which is used to initial log in to OAuth2 server,
 * // has hard-coded WebClient, which doesn't use any load balancers.
 * // Meanwhile,
 * // org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.refreshAuthorizedClient
 * // is called from user-created WebClient, which DOES USE Load Balancer.
 * // refreshAuthorizedClient method uses clientRegistration.getProviderDetails().getTokenUri(),
 * // which is supposed to pass through load balancer, but stores absolute auth server's address.
 * // for now this can be fixed by setting authorization server's service name in Eureka the same as a physical host's name
 * // for example, we can map some host to 127.0.0.1 in etc/hosts on windows
 * // it seems that Spring Security v.5.2 will allow to override WebClient used in WebClientReactiveAuthorizationCodeTokenResponseClient
 *
 * For spring-security-oauth2 v.2.3.3 we need to
 * Make Authorization Server Compatible with Spring Security 5.1 Resource Server and Client:
 * https://docs.spring.io/spring-security-oauth2-boot/docs/current-SNAPSHOT/reference/htmlsingle/#oauth2-boot-authorization-server-spring-security-oauth2-resource-server
 **/
@Slf4j
@Configuration
//@EnableAuthorizationServer
@Import(AuthorizationServerEndpointsConfiguration.class)
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Value("${authserver.registration-id}")
    String registrationID;

    @Value("${webui.redirect.host}")
    String redirectURI;

    /**
     * authenticationManager is really needed for a "password" grant type only:
     * https://docs.spring.io/spring-security-oauth2-boot/docs/current-SNAPSHOT/reference/htmlsingle/#oauth2-boot-authorization-server-authentication-manager
     */
    private final AuthenticationManager authenticationManager;
    // Key pair used to sign JWT. Generated on server restart
    private final KeyPair keyPair;

    @Autowired
    public AuthServerConfig(AuthenticationManager authenticationManager, KeyPair keyPair) {
        this.authenticationManager = authenticationManager;
        this.keyPair = keyPair;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()") ;
    }

    // Client applications configuration - hard-coded, there's no need to store it anywhere else
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient("webui")
                .secret(passwordEncoder().encode("webui-secret"))
                .authorizedGrantTypes("authorization_code", "client_credentials", "password", "refresh_token") // client-credentials may be will not be needed
                .scopes("openid", "quotes") // openid is necessary, others are custom
                .autoApprove(true)// we don't want it to ask any questions to user about approving the scopes
                .redirectUris(redirectURI+"/login/oauth2/code/"+registrationID)// the host should be load balanced maybe?
        ;
    }


    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .authenticationManager(authenticationManager)
                .accessTokenConverter(accessTokenConverter())
                .tokenStore(tokenStore())
                .tokenServices(authorizationTokenServices())
        ;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }



    private JwtAccessTokenConverter accessTokenConverter() {
        final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyPair);
        return converter;
    }


    @Autowired
    private OIDCTokenEnhancer oidcTokenEnhancer;

    @Bean
    @Primary
    public AuthorizationServerTokenServices authorizationTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setReuseRefreshToken(false);
        JWTTokenConverter tokenConverter = new JWTTokenConverter();
        tokenConverter.setKeyPair(keyPair);
        tokenServices.setTokenStore(new JwtTokenStore(tokenConverter));
        TokenEnhancerChain chain = new TokenEnhancerChain();
        chain.setTokenEnhancers(Arrays.asList(tokenConverter, oidcTokenEnhancer));
        tokenServices.setTokenEnhancer(chain);
//        tokenServices.setAccessTokenValiditySeconds(70);
        return tokenServices;
    }


    /**
     * JWKS endpoint support. Exposing public keys for JWT verification
     * https://docs.spring.io/spring-security-oauth2-boot/docs/current-SNAPSHOT/reference/htmlsingle/#oauth2-boot-authorization-server-spring-security-oauth2-resource-server
     */
    @FrameworkEndpoint
    class JwkSetEndpoint {
        private final KeyPair keyPair;

        JwkSetEndpoint(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        @GetMapping("/.well-known/jwks.json")
        @ResponseBody
        public Map<String, Object> getKey(Principal principal) {
            RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();
            RSAKey key = new RSAKey.Builder(publicKey).build();
            return new JWKSet(key).toJSONObject();
        }
    }

    @Configuration
    class JwkSetEndpointConfiguration extends AuthorizationServerSecurityConfiguration {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http
                    .requestMatchers()
                    .mvcMatchers("/.well-known/jwks.json")
                    .and()
                    .authorizeRequests()
                    .mvcMatchers("/.well-known/jwks.json").permitAll();
        }
    }
}