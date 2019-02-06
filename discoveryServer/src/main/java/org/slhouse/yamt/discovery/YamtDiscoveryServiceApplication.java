/*
 * Copyright (c) 2019. Yaroslav Khazanov y@5505.ru

 * Full Spring Cloud documentation can be found here:
 * https://cloud.spring.io/spring-cloud-netflix/single/spring-cloud-netflix.html
 * Documentation on service discovery: https://cloud.spring.io/spring-cloud-netflix/single/spring-cloud-netflix.html#_service_discovery_eureka_clients
 */

package org.slhouse.yamt.discovery;


import com.netflix.appinfo.AmazonInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableEurekaServer
public class YamtDiscoveryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(YamtDiscoveryServiceApplication.class, args);
	}

	// Configuration for AWS,
	// see https://cloud.spring.io/spring-cloud-netflix/single/spring-cloud-netflix.html#_using_eureka_on_aws
	@Bean
	@Profile("!default")
	public EurekaInstanceConfigBean eurekaInstanceConfigBean(InetUtils inetUtils) {
		EurekaInstanceConfigBean bean = new EurekaInstanceConfigBean(inetUtils);
		AmazonInfo info = AmazonInfo.Builder.newBuilder().autoBuild("eureka");
		bean.setDataCenterInfo(info);
		return bean;
	}

}

