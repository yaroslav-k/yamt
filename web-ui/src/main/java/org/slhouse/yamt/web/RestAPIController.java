/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import lombok.extern.slf4j.Slf4j;
import org.slhouse.yamt.entity.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;

/**
 * @author Yaroslav V. Khazanov
 **/
@Slf4j
@RestController
@RequestMapping("/api")
public class RestAPIController {
    @Value("${name}")
    private String name;
    // This one can be used to override quoteService address if we don't use discovery server at all. Useful for tests also.
    @Value("${quoteService.uri:}")// by default it's empty
    private String quoteServiceURI;

    private final WebClient.Builder webClient;

    @Autowired
    public RestAPIController(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    // token is either OAuth2AuthenticationToken or JwtAuthenticationToken
    @GetMapping("/")
    public Mono<String> index(JwtAuthenticationToken token, ServerHttpRequest request) throws UnknownHostException {
        String s = token.getToken().getTokenValue();
        return Mono.just(s + "<p>Hello from " + name + " @" + InetAddress.getLocalHost().getHostAddress() + " (" + InetAddress.getLocalHost().getHostName() + "), port: " + request.getURI().getPort());
    }

    @GetMapping("/jwt")
    public Mono<String> jwt(JwtAuthenticationToken jwtAuthenticationToken, @AuthenticationPrincipal Principal principal, ServerHttpRequest request) throws UnknownHostException {
        return Mono.just("Jwt Endpoint only. Secured for " + jwtAuthenticationToken.getName() + ". Hello from " + name + " @" + InetAddress.getLocalHost().getHostAddress() + " (" + InetAddress.getLocalHost().getHostName() + "), port: " + request.getURI().getPort());
    }

    @GetMapping("/quotes")
    public Flux<Quote> quotes(JwtAuthenticationToken jwtAuthenticationToken, @AuthenticationPrincipal OidcUser oidcUser, Authentication token) {
        log.info("Start request");
        Flux<Quote> quoteFlux = webClient.build().get()
                .uri(quoteServiceURI +"/quote").accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAuthenticationToken.getToken().getTokenValue())
                .attribute("custom attr", "ca value")
                .retrieve()
                .onStatus(HttpStatus::isError, resp -> resp.bodyToMono(String.class).map(FileNotFoundException::new)) // just as an example
                .bodyToFlux(Quote.class);
        log.info("End request");
        return quoteFlux;
    }

    // just as an example
    @ExceptionHandler
    public Mono<ResponseEntity<String>> handleException(FileNotFoundException ex) {
        return Mono.just(ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Error retrieving quotes: " + ex.getMessage()));

    }

}
