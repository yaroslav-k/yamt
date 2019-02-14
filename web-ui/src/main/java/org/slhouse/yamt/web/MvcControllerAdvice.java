/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;

/**
 * @author Yaroslav V. Khazanov
 **/
@ControllerAdvice
public class MvcControllerAdvice {
    @ModelAttribute("currentUser")
    Mono<YamtUser> currentUser(@AuthenticationPrincipal Mono<YamtUser> currentUser) {
        return currentUser;
    }

}
