/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.authserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Yaroslav V. Khazanov
 **/
@Slf4j
@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${webui.redirect.host}")
    String redirectURI;


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    KeyPair keyPair() {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(2048, random);
            return keyGen.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.error("Error generating keypair: " + e.getMessage(), e);
            return null;
        }
    }
/*
    @Bean
    OidcAuthorizationCodeAuthenticationProvider authProvider() {
        return new OidcAuthorizationCodeAuthenticationProvider();

    }
*/

/*
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
    }
*/

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()// needed for logoutUrl to be available via simple GET. this is not a big problem, because this is an authorization server only.
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
                .httpBasic()
            .and()
/* // Leave only basic form login for now
                .oauth2Login()
            .and()
*/
                .formLogin()
                    // in case of incorrect login we can redirect user to a secure area in WebUI, which in turn will redirect back to login form.
                    // instead of hardcoded url we could use something like SavedRequestAwareAuthenticationSuccessHandler does
                    .failureUrl(UriComponentsBuilder.fromHttpUrl(redirectURI).path("sec").queryParam("loginerror").build().toUriString())
                    .successHandler(new SavedRequestAwareAuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
                            super.onAuthenticationSuccess(request, response, authentication);
                            // here we'll create new user, or update lastLogin for existing one.
                            log.info("Successful formLogin: " + authentication.getName());
                        }
                    })
            .and()
        .logout().logoutUrl("/logout/token").clearAuthentication(true).invalidateHttpSession(true).logoutSuccessUrl(redirectURI)
        ;

    }

}
