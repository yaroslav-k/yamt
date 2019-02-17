/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.web;

import lombok.extern.slf4j.Slf4j;
import org.slhouse.yamt.entity.Quote;
import org.slhouse.yamt.entity.YamtUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
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

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

/**
 * @author Yaroslav V. Khazanov
 **/
@Slf4j
@RestController
@RequestMapping("/api")
public class RestAPIController {

//    private final WebClient webClient;

    @Value("${name}")
    private String name;
    // This one can be used to override quoteService address if we don't use discovery server at all. Useful for tests also.
    @Value("${quoteService.uri:}")// by default it's empty
    private String quoteServiceURI;

/*
    @Autowired
    public RestAPIController(@Qualifier("quoteServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }
*/

    // token is either OAuth2AuthenticationToken or JwtAuthenticationToken
    @GetMapping("/")
    public Mono<String> index(@AuthenticationPrincipal Authentication token, ServerHttpRequest request) throws UnknownHostException {
        String s="";
        if (token instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken t = (OAuth2AuthenticationToken) token;
            if (t.getPrincipal() instanceof DefaultOidcUser)
                    s = ((DefaultOidcUser) t.getPrincipal()).getIdToken().getTokenValue();
        } else if (token instanceof JwtAuthenticationToken) {
            s = ((JwtAuthenticationToken) token).getToken().getTokenValue();
        }
        return Mono.just(s + "<p>Hello from " + name + " @" + InetAddress.getLocalHost().getHostAddress() + " (" + InetAddress.getLocalHost().getHostName() + "), port: " + request.getURI().getPort());
    }

    @GetMapping("/sec")
    //@PreAuthorize("hasAnyRole('ROLE_USER')") //    @PreAuthorize("#oauth2.isOAuth()")
    public Mono<String> index(@YamtRegisteredUser YamtUser user, /*@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient, */@AuthenticationPrincipal Principal principal, ServerHttpRequest request) throws UnknownHostException {
        return Mono.just("Secured for " + user.getUsername() + ". Hello from " + name + " @" + InetAddress.getLocalHost().getHostAddress() + " (" + InetAddress.getLocalHost().getHostName() + "), port: " + request.getURI().getPort());
    }

    @Autowired
    WebClient.Builder webClient;


    @GetMapping("/quotes")
    public Flux<Quote> quotes(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient, Authentication token) {
        log.info("Start request");
        final OAuth2AuthorizedClient newAuthorizedClient = createAuthorizedClient(authorizedClient, token);
        Flux<Quote> quoteFlux = webClient.build().get()
                .uri(quoteServiceURI +"/quote").accept(MediaType.APPLICATION_JSON)
                .attributes(oauth2AuthorizedClient(newAuthorizedClient))
                .retrieve()
                .onStatus(HttpStatus::isError, resp -> resp.bodyToMono(String.class).map(FileNotFoundException::new)) // just as an example
                .bodyToFlux(Quote.class);
        log.info("End request");
        return quoteFlux;
    }

    @GetMapping("/qsec")
    public Flux<String> qsec(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient, Authentication token) {
        log.info("Start request");
        final OAuth2AuthorizedClient newAuthorizedClient = createAuthorizedClient(authorizedClient, token);

        Flux<String> quoteFlux = webClient.build().get()
                .uri(quoteServiceURI +"/sec").accept(MediaType.APPLICATION_JSON)
                .attributes(oauth2AuthorizedClient(newAuthorizedClient))
                .attribute("custom attr", "ca value")
                .header("Client-from", authorizedClient.getClientRegistration().getRegistrationId())
                .retrieve()
                .onStatus(HttpStatus::isError, resp -> resp.bodyToMono(String.class).map(FileNotFoundException::new)) // just as an example
                .bodyToFlux(String.class);
        log.info("End request");
        return quoteFlux;
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
            idToken = ((JwtAuthenticationToken) token).getToken();
            s = idToken.getTokenValue();
        };
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, s, idToken.getIssuedAt(), idToken.getExpiresAt());
    }


    // just as an example
    @ExceptionHandler
    public Mono<ResponseEntity<String>> handleException(FileNotFoundException ex) {
        return Mono.just(ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Error retrieving quotes: " + ex.getMessage()));

    }

}
