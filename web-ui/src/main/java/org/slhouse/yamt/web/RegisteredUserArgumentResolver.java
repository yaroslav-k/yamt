/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.slhouse.yamt.entity.YamtUser;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Yaroslav V. Khazanov
 **/
public class RegisteredUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return (YamtUser.class.isAssignableFrom(parameterType) &&
                (AnnotatedElementUtils.findMergedAnnotation(
                        parameter.getParameter(), YamtRegisteredUser.class) != null));
    }


    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        return exchange.getPrincipal().ofType(Authentication.class)
                .flatMap(a -> Mono.justOrEmpty(resolvePrincipal(parameter, a)));
    }


    private Object resolvePrincipal(MethodParameter parameter, Object principal) {
        if (OAuth2AuthenticationToken.class.isAssignableFrom(principal.getClass())) {
            OAuth2AuthenticationToken t = (OAuth2AuthenticationToken) principal;
            return new YamtUser(t);
        }
        if (JwtAuthenticationToken.class.isAssignableFrom(principal.getClass())) {
            JwtAuthenticationToken t = (JwtAuthenticationToken) principal;
            return new YamtUser(t);
        }
        return principal;
    }


}
