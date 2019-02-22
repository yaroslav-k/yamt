package org.slhouse.yamt.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slhouse.yamt.entity.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;

import static org.hamcrest.Matchers.startsWith;

@AutoConfigureWireMock(port = 8777)
@RunWith(SpringRunner.class)
@WebFluxTest
@TestPropertySource(properties = {
        "quoteService.url=http://localhost:8777", // webController should go there for quoteService. Then we can mock quoteService
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
                .expectBody(String.class).value(startsWith("Hello from localWebUI"));
    }

    @Test
    public void testSecure() {
        client.get()
                .uri("/sec")
                .exchange()
                .expectStatus().is5xxServerError();
//				.expectStatus().is2xxSuccessful()
//				.expectBody(String.class).value(startsWith("Hello from localWebUI"));
    }


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

}

