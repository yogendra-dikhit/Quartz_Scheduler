package com.taskscheduler.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GeneralConfiguration {

	@Bean
	public RestTemplate createRestTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
}
