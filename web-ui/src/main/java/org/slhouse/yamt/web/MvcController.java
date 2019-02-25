/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import lombok.extern.slf4j.Slf4j;
import org.slhouse.yamt.entity.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

/**
 * @author Yaroslav V. Khazanov
 * Note: to use @PreAuthorize("#oauth2.XXXX('')"), these methods should return {@code Mono<Rendering>} instead of
 * merely Rendering
 **/
@Slf4j
@Controller
public class MvcController {
    @Value("${quoteService.uri:}")// by default it's empty
    private String quoteServiceURI;

    private final WebClient.Builder webClient;

    @Autowired
    public MvcController(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/")
    Rendering root() {
        return Rendering.view("index").build();
    }

    @GetMapping("/sec")
    @PreAuthorize("hasRole('USER')")
//    @PreAuthorize("#oauth2.denyOAuthClient()")
    Mono<Rendering> secured(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient, @AuthenticationPrincipal OidcUser oidcUser) {
        return Mono.just(Rendering.view("secured").build());

    }


    @GetMapping("/quotes")
    public Rendering quotes(@RegisteredOAuth2AuthorizedClient("authserver") OAuth2AuthorizedClient authorizedClient, @AuthenticationPrincipal OidcUser oidcUser, Authentication token) {
        final OAuth2AuthorizedClient newAuthorizedClient = createAuthorizedClient(authorizedClient, token);
        Flux<String> quoteFlux = webClient.build().get()
                .uri(quoteServiceURI + "/quote").accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_STREAM_JSON)
                .attributes(oauth2AuthorizedClient(newAuthorizedClient))
                .attribute("custom attr", "ca value")
                .header("Client-from", authorizedClient.getClientRegistration().getRegistrationId())
                .retrieve()
                .onStatus(HttpStatus::isError, resp -> resp.bodyToMono(String.class).map(FileNotFoundException::new)) // just as an example
                .bodyToFlux(Quote.class)
                .map(quote -> quote.getName() + " " + quote.getPrice() + "\n");
        return Rendering.view("quotes").modelAttribute("quotes", quoteFlux).build();
    }

    private OAuth2AuthorizedClient createAuthorizedClient(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient, Authentication token) {
        return new OAuth2AuthorizedClient(authorizedClient.getClientRegistration(), authorizedClient.getPrincipalName(),
                createAccessToken(token),
                authorizedClient.getRefreshToken()
        );
    }

    private OAuth2AccessToken createAccessToken(Authentication token) {
        String s;
        AbstractOAuth2Token idToken;
        if (token instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken t = (OAuth2AuthenticationToken) token;
            idToken = ((DefaultOidcUser) t.getPrincipal()).getIdToken();
            s = idToken.getTokenValue();
        } else /*if (token instanceof JwtAuthenticationToken)*/ {
            idToken = ((JwtAuthenticationToken) token).getToken(); // shoouldn't be here, only in RestController
            s = idToken.getTokenValue();
        };
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, s, idToken.getIssuedAt(), idToken.getExpiresAt());
    }

}
