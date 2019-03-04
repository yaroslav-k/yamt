/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru
 */

package org.slhouse.yamt.authserver;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AuthorizationServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootConfiguration // needed to use configuration files from standard /resource folder
@TestPropertySource(properties = {
		"eureka.client.enabled=false",  // switching off all cloud-related services
		"spring.cloud.config.enabled=false"
})
public class AuthorizationServerApplicationTests {
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void contextLoads() throws JSONException {
		// oauth/token?grant_type=password&username=user&password=user
		//oauth/token?grant_type=client_credentials
		ResponseEntity<String> entity = testRestTemplate
				.postForEntity("/oauth/token?grant_type={1}", null, String.class, "client_credentials");
		assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
		entity = testRestTemplate
				.withBasicAuth("webui", "webui-secret")
				.postForEntity("/oauth/token?grant_type={1}", null, String.class, "client_credentials");
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertNotNull(entity.getBody());
		final JSONObject json = new JSONObject(entity.getBody());
		assertThat(json.getString("token_type"), is("bearer"));

	}

}
