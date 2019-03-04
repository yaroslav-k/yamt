package org.slhouse.yamt.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.Matchers.containsString;

@AutoConfigureWireMock(port = 8777)
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "quoteService.url=http://localhost:8777", // webController should go there for quoteService. Then we can mock quoteService
})
@WebFluxTest
@Import({
        ReactiveOAuth2ClientAutoConfiguration.class, // import the needed configuration
        OAuth2ResourceServerProperties.class, // import the needed configuration
        ThymeleafAutoConfiguration.class // this is needed to load view resolvers configuration to test @Controller's methods
})
public class WebUIApplicationTests {
    @Autowired
    WebTestClient client;

    @Test
    public void testRoot() {
        client.get()
                .uri("/")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class).value(containsString("Hello from Yet Another Money Tracker"));
    }

    @Test
    public void testSecure() {
        client.get()
                .uri("/sec")
                .exchange()
                .expectStatus().isUnauthorized();
    }


/* This requires OAuth2 authorization. Will be tested in e2e tests
    @Test
    public void testQuotes() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String quote = mapper.writeValueAsString(Arrays.asList(new Quote("quote"), new Quote("quote1")));
        // Here we'll need wiremock to mock the quoteService server
        WireMock.stubFor(WireMock.any(WireMock.urlMatching("/quote"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBody(quote)
                ));
        client
                .get()
                .uri("/quotes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("@.[0].name").isEqualTo("quote")
                .jsonPath("@.[1].name").isEqualTo("quote1");


    }
*/

}


