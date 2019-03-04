/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.quote;

import net.minidev.json.JSONArray;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * @author Yaroslav V. Khazanov
 **/
public class JwtSecurityExpressionMethods {
    private final Authentication authentication;

    public JwtSecurityExpressionMethods(Authentication authentication) {
        this.authentication = authentication;
    }
    public boolean hasScope(String scope) {
        final Jwt token = ((JwtAuthenticationToken) authentication).getToken();
        final Object scopes = token.getClaims().get("scope");
        return (scopes instanceof JSONArray) && ((JSONArray) scopes).contains(scope);
    }
}
