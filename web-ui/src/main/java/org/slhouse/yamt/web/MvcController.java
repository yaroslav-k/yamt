/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;

/**
 * @author Yaroslav V. Khazanov
 **/
@Controller
public class MvcController {
    @GetMapping("/")
    Rendering root() {
        return Rendering.view("index").build();
    }

    @GetMapping("/sec")
    Rendering secured(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        return Rendering.view("secured").build();

    }
}
