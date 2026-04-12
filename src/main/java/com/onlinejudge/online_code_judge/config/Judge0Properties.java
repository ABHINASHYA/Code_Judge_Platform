package com.onlinejudge.online_code_judge.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "judge0")
public class Judge0Properties {

	private String baseUrl;
	private String submissionPath;
	private String rapidApiKey;
	private String rapidApiHost;
	private Duration connectTimeout = Duration.ofSeconds(5);
	private Duration readTimeout = Duration.ofSeconds(30);

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getSubmissionPath() {
		return submissionPath;
	}

	public void setSubmissionPath(String submissionPath) {
		this.submissionPath = submissionPath;
	}

	public String getRapidApiKey() {
		return rapidApiKey;
	}

	public void setRapidApiKey(String rapidApiKey) {
		this.rapidApiKey = rapidApiKey;
	}

	public String getRapidApiHost() {
		return rapidApiHost;
	}

	public void setRapidApiHost(String rapidApiHost) {
		this.rapidApiHost = rapidApiHost;
	}

	public Duration getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Duration getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = readTimeout;
	}
}
