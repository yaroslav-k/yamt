package org.slhouse.yamt.quote;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@WebFluxTest
public class
QuoteServiceApplicationTests {
	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void testRestEndpoints() {
		webTestClient.get()
				.uri("/")
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).value(Matchers.startsWith("Hello from localquoteService"));


	}

	@Test
	public void testSecureEndpoints() {
		webTestClient.get()
				.uri("/sec")
//				.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString("user:user".getBytes()))
				.exchange()
				.expectStatus().is5xxServerError(); // because of no Principal supplied for now
//				.expectStatus().is2xxSuccessful()
//				.expectBody(String.class).returnResult().getResponseBody().startsWith("Secured for ");
	}

	@Test
	public void testQuotesEndpoint() {
		webTestClient.get()
				.uri("/quote")
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody()
				.jsonPath("@.[0].name").value(Matchers.startsWith("Quote from port "))
				.jsonPath("@.[1].name").isEqualTo("localquoteService");
	}

}

