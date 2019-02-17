/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;

/**
 * @author Yaroslav V. Khazanov
 **/
@Controller
public class LoginLogoutController {
    @GetMapping("/login")
    public Rendering login() {
        return Rendering.view("login").build();
    }
    @GetMapping("/logout")
    public Rendering logout() {
        return Rendering.view("logout").build();
    }
}
