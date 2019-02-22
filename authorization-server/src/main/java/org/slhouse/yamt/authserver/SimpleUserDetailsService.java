/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.authserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yaroslav V. Khazanov
 **/
@Service
class SimpleUserDetailsService implements UserDetailsService {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    private final PasswordEncoder passwordEncoder;

    @Autowired
    SimpleUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.users.putIfAbsent("user", new User("user", passwordEncoder.encode("user"), AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = this.users.get(username);
        // there's a call to eraseCredentials, so we should return a clone
        return new User(user.getUsername(), user.getPassword(), user.getAuthorities());
    }
}
