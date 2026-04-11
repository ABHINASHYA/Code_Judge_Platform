package com.onlinejudge.online_code_judge.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class SystemController {

	@Value("${spring.application.name:online-code-judge}")
	private String appName;

	@Value("${APP_VERSION:dev}")
	private String appVersion;

	@Value("${RAILWAY_GIT_COMMIT_SHA:unknown}")
	private String commitSha;

	@GetMapping("/ping")
	public Map<String, Object> ping() {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("status", "ok");
		response.put("app", appName);
		response.put("version", appVersion);
		response.put("commit", commitSha);
		response.put("timestamp", Instant.now().toString());
		return response;
	}
}
