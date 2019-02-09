package org.slhouse.yamt.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Pretty simple test. Not sure if it will be needed later
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = YamtConfigurationServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@SpringBootConfiguration // needed to use configuration files from standard /resource folder
public class YamtConfigurationServerApplicationTests {
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void catalogLoads() {
		ResponseEntity<Map> entity = testRestTemplate.getForEntity("/configserver/default/master", Map.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertNotNull(entity.getBody());
		assertTrue(entity.getBody().containsKey("propertySources"));
		assertTrue(entity.getBody().get("propertySources") instanceof List);
		assertTrue(((List)entity.getBody().get("propertySources")).size()>0);

	}


}

