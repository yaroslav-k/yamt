package org.slhouse.yamt.discovery;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class YamtDiscoveryServiceApplicationTests {
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void catalogLoads() {
		ResponseEntity<Map> entity = testRestTemplate.getForEntity("/eureka/apps", Map.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
	}
}

