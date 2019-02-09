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
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
public class WebController {

    private final WebClient.Builder webClientBuilder;

    @Value("${name}")
    private String name;
    // This one can be used to override quoteService address if we don't use discovery server at all. Useful for tests also.
    @Value("${quoteService.uri:}")// by default it's empty
    private String quoteServiceURI;

    @Autowired
    public WebController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/")
    public Mono<String> index(ServerHttpRequest request) throws UnknownHostException {
        return Mono.just("Hello from " + name + " @" + InetAddress.getLocalHost().getHostAddress() + " (" + InetAddress.getLocalHost().getHostName() + "), port: " + request.getURI().getPort());
    }

    @GetMapping("/sec")
    public Mono<String> index(Principal principal, ServerHttpRequest request) throws UnknownHostException {
        return Mono.just("Secured for " + principal.getName() + ". Hello from " + name + " @" + InetAddress.getLocalHost().getHostAddress() + " (" + InetAddress.getLocalHost().getHostName() + "), port: " + request.getURI().getPort());
    }

    @GetMapping("/quotes")
    public Flux<Quote> quotes() {
        WebClient client = webClientBuilder.build();
        log.info("Start request");
        Flux<Quote> quoteFlux = client.get().uri(quoteServiceURI +"/quote").accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatus::isError, resp -> resp.bodyToMono(String.class).map(FileNotFoundException::new)) // just as an example
                .bodyToFlux(Quote.class);
        log.info("End request");
        return quoteFlux;
    }

    // just as an example
    @ExceptionHandler
    public ResponseEntity<String> handleException(FileNotFoundException ex) {
        return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Error retrieving quotes: " + ex.getMessage());

    }

}
