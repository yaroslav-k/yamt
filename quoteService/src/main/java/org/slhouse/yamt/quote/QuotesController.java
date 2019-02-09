/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.quote;

import lombok.extern.slf4j.Slf4j;
import org.slhouse.yamt.entity.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.time.Duration;

/**
 * @author Yaroslav V. Khazanov
 **/
@RestController
@Slf4j
public class QuotesController {
    @Value("${name}")
    private String name;

    @GetMapping("/")
    public Mono<String> index(ServerHttpRequest request) throws UnknownHostException {
        return Mono.just("Hello from " + name + " @" + InetAddress.getLocalHost().getHostAddress() + " (" + InetAddress.getLocalHost().getHostName() + "), port: " + request.getURI().getPort());
    }

    @GetMapping("/sec")
    public Mono<String> index(Principal principal, ServerHttpRequest request) throws UnknownHostException {
        return Mono.just("Secured for " + principal.getName() + ". Hello from " + name + " @" + InetAddress.getLocalHost().getHostAddress() + " (" + InetAddress.getLocalHost().getHostName() + "), port: " + request.getURI().getPort());
    }

    @GetMapping("/quote")
    public Flux<Quote> getQuotes(ServerHttpRequest request) throws FileNotFoundException {
        // just as an example
//        if (new Random().nextInt(10) <= 5) throw new FileNotFoundException("Quote not found");
        log.info("Started getQuotes");
        Flux<Quote> stringFlux = Flux.just(new Quote("Quote from port " + request.getURI().getPort()), new Quote(name), new Quote("Quote1"), new Quote("Quote2"), new Quote("Quote3")).delayElements(Duration.ofMillis(100));
        log.info("Finished getQuotes");
        return stringFlux;
    }

    // just as an example
    @ExceptionHandler
    public ResponseEntity<String> handleException(FileNotFoundException ex) {
        return ResponseEntity.badRequest().body("Error: " + ex.getMessage());

    }
}
