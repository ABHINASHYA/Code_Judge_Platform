package com.onlinejudge.online_code_judge.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(Judge0Properties.class)
public class Judge0Config {

	@Bean
	@Qualifier("judge0RestTemplate")
	public RestTemplate judge0RestTemplate(RestTemplateBuilder builder, Judge0Properties properties) {
		return builder
				.rootUri(properties.getBaseUrl())
				.setConnectTimeout(properties.getConnectTimeout())
				.setReadTimeout(properties.getReadTimeout())
				.build();
	}
}
