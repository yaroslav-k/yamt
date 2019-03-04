package org.slhouse.yamt.quote;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import wiremock.com.github.jknack.handlebars.internal.Files;

import java.io.IOException;
import java.nio.charset.Charset;

@AutoConfigureWireMock(port = 8090)
@RunWith(SpringRunner.class)
@WebFluxTest
@Import(OAuth2ResourceServerProperties.class) // otherwise it will not be available in org.slhouse.yamt.quote.WebFluxConfig
public class QuoteServiceApplicationTests {
	@Autowired
	private WebTestClient webTestClient;

	// Encoded and signed JWT. We're using a token with very long life
	@Value("classpath:bearer.token")
	Resource bearerToken;


	private String bearer;

    @Before
    public void setUp() throws IOException {
    	bearer = Files.read(bearerToken.getFile(), Charset.defaultCharset());
/* // we don't need this here because we have src\test\resources\__files\.well-known\jwks.json used by @AutoConfigureWireMock
// see https://cloud.spring.io/spring-cloud-static/spring-cloud-contract/1.1.2.RELEASE/#_using_files_to_specify_the_stub_bodies
		String keys = "{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",\"n\":\"sLijSXrUjNmDeJdQNxvGGhsyi2Aivqm5xiNPS1EDEDRyGQgvhgxsWofcsMMmo5HMls2CocLIB7WHdz0J7fWIW7xs0sj9WkcjqdF3LEe7G_KRzoIEdis4ZaOaQlt5sixqAoADvKuuTEjIuoxyGrAw-AVxfYVQ5QnksKAGJpO-TY92VgRnviRcTTayGv6QQ6p1YMKcw4honizYIGsPIGYk6-vS-eKoG8wIVvDlKjZedsi-ZmX4S-2kKepERi8dJeC2z5hkeC3MRgJvLeEAc0JkdOcAt-N9VjXqpeEV4KIcrZjDaUZHow7zYI35puajy1OC8wh1WDPqxyZ4W0i9zL1vnw\"}]}";
        WireMock.stubFor(WireMock.any(WireMock.urlMatching("/.well-known/jwks.json"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .withBody(keys)
                ));
*/
    }

	@Test
	public void testRestEndpoints() {
		webTestClient.get()
				.uri("/")
                .header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).value(Matchers.startsWith("Hello from localquoteService"));
	}

	@Test
	public void testSecureEndpoints() {
        webTestClient
				.get()
				.uri("/sec")
				.header("Client-from", "test")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class).value(Matchers.startsWith("Secured for "));
		;
	}

	@Test
	public void testIncorrectToken() {
		webTestClient.get()
				.uri("/quote")
                .header(HttpHeaders.AUTHORIZATION, bearer+"1")
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@Test
	public void testNoToken() {
		webTestClient.get()
				.uri("/quote")
				.exchange()
				.expectStatus().is4xxClientError();
	}
}

