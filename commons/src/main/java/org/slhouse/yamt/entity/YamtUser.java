/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Yaroslav V. Khazanov
 **/
public class YamtUser implements UserDetails {

    private Collection<? extends GrantedAuthority> authorities;
    private String password;
    private String username;
    private String fullName;

    public YamtUser() {
    }

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

    public YamtUser(OAuth2AuthenticationToken token) {
        this(UUID.randomUUID().toString(),token.getAuthorizedClientRegistrationId() + " " + token.getPrincipal().getName(), token.getPrincipal().getAuthorities());

    }

    public YamtUser(JwtAuthenticationToken token) {
        this(UUID.randomUUID().toString(),token.getName());
        // todo calculate auth provider

    }

    public YamtUser(String password, String username, Collection<? extends GrantedAuthority> authorities) {
        this(password, username);
        this.authorities = new ArrayList<>(authorities);
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
