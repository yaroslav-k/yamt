/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.quote;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.expression.OAuth2ExpressionParser;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;

/**
 * @author Yaroslav V. Khazanov
 **/
public class JwtMethodSecurityExpressionHandler extends OAuth2MethodSecurityExpressionHandler {

    public JwtMethodSecurityExpressionHandler() {
        setExpressionParser(new OAuth2ExpressionParser(getExpressionParser()));
    }

    @Override
    public StandardEvaluationContext createEvaluationContextInternal(Authentication authentication, MethodInvocation mi) {
        StandardEvaluationContext ec = super.createEvaluationContextInternal(authentication, mi);
        ec.setVariable("jwt", new JwtSecurityExpressionMethods(authentication));
        return ec;
    }
}
