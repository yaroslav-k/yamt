/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @author Yaroslav V. Khazanov
 **/
public class YamtUser implements UserDetails {

    private final Collection<? extends GrantedAuthority> authorities;
    private final String password;
    private final String username;
    private String fullName;

    public YamtUser(String password, String username) {
        this.password = password;
        this.username = username;
        authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        fullName = username;
    }
    public YamtUser(String password, String username, String fullName) {
        this(password, username);
        this.fullName = fullName;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "YamtUser{" +
                "authorities=" + authorities +
                ", password=***********" +  '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
