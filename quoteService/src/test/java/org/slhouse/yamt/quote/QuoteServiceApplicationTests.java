package org.slhouse.yamt.quote;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWireMock(port = 8090)
@RunWith(SpringRunner.class)
@WebFluxTest
@Import(OAuth2ResourceServerProperties.class) // otherwise it will not be available in org.slhouse.yamt.quote.WebFluxConfig
public class QuoteServiceApplicationTests {
	@Autowired
	private WebTestClient webTestClient;

    private final String bearer = "Bearer " + "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiYXVkIjoid2VidWkiLCJhenAiOiJ3ZWJ1aSIsImlzcyI6Imh0dHA6XC9cL2traXQtbWFpbiIsInNjb3BlcyI6WyJvcGVuaWQiLCJxdW90ZXMiXSwiZXhwIjoxNTUxMzE0MjU4LCJpYXQiOjE1NTEyNzEwNTgsImp0aSI6IjliNGM2ODEwLTg2NmUtNDEzMS05YjM2LTRmNzk1NjU0YzAyOSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdfQ.kaMuutj6t7npS9Q-44bdxLNDnxpVsZ5g8hb104syBWe4-lQJBG63mH-paQGlVSJ9ZOcWh0MVh3pVWPbe_Q-8p-5yf_0hgyKFT5ugihO1I3KuOT_a-4l79gbXAD88OwFYqa7_T8EPDlPco4nJH_OTOS70DCqDBUhVVunjdrBu3xILLme8UzLSplNfdnaUlyxHtyfePQZVlrZoTSAbr90QiZnSCu_BIDyuhJWqgxyCBY5DqQ8D_PigL_afadTCQxXSJcaukmuQXxvVSRowQj_DCqfoGT3enWhUtbx73HQsQSZrm9Kx8BuoaDBItEptX-ueK_U-mAf3FWFZBzgQLl3DlA";

    @Before
    public void setUp() {
        String keys = "{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",\"n\":\"sLijSXrUjNmDeJdQNxvGGhsyi2Aivqm5xiNPS1EDEDRyGQgvhgxsWofcsMMmo5HMls2CocLIB7WHdz0J7fWIW7xs0sj9WkcjqdF3LEe7G_KRzoIEdis4ZaOaQlt5sixqAoADvKuuTEjIuoxyGrAw-AVxfYVQ5QnksKAGJpO-TY92VgRnviRcTTayGv6QQ6p1YMKcw4honizYIGsPIGYk6-vS-eKoG8wIVvDlKjZedsi-ZmX4S-2kKepERi8dJeC2z5hkeC3MRgJvLeEAc0JkdOcAt-N9VjXqpeEV4KIcrZjDaUZHow7zYI35puajy1OC8wh1WDPqxyZ4W0i9zL1vnw\"}]}";
        WireMock.stubFor(WireMock.any(WireMock.urlMatching("/.well-known/jwks.json"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .withBody(keys)
                ));
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

