/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * @author Yaroslav V. Khazanov
 **/
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .pathMatchers("/login", "/webjars/**", "/users/signup", "/logout", "/").permitAll()
                .anyExchange().authenticated()
                .and()
                .httpBasic().and()
                .formLogin().loginPage("/login").and()
                .logout().logoutUrl("/logout")

        ;
        return http.build();
    }

    /*
        @Bean
        MapReactiveUserDetailsService userDetailsService() {
            UserDetails user = User.builder()
                    .username("user")
                    .password(passwordEncoder().encode("user"))
                    .roles("USER")
                    .build();
            return new MapReactiveUserDetailsService(user);
        }
    */

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
