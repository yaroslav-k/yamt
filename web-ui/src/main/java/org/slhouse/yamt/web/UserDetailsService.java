/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author Yaroslav V. Khazanov
 **/
@Component
public class UserDetailsService implements ReactiveUserDetailsService {
    final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.just(new YamtUser(passwordEncoder.encode("user"), "user", "custom user"));
    }


}
