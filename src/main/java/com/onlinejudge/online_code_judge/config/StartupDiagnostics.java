package com.onlinejudge.online_code_judge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;

@Component
public class StartupDiagnostics {

	private static final Logger log = LoggerFactory.getLogger(StartupDiagnostics.class);

	private final RequestMappingHandlerMapping mappings;

	@Value("${spring.application.name:online-code-judge}")
	private String appName;

	@Value("${APP_VERSION:dev}")
	private String appVersion;

	@Value("${RAILWAY_GIT_COMMIT_SHA:unknown}")
	private String commitSha;

	public StartupDiagnostics(RequestMappingHandlerMapping mappings) {
		this.mappings = mappings;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logStartupDetails() {
		log.info("Application ready: name='{}', version='{}', commit='{}'", appName, appVersion, commitSha);

		mappings.getHandlerMethods().forEach((info, handler) -> {
			if (matchesPath(info, "/api/auth/login") || matchesPath(info, "/ping") || matchesPath(info, "/api/auth/ping")) {
				log.info("Mapped endpoint {} -> {}#{}", describeMapping(info), handler.getBeanType().getSimpleName(),
						handler.getMethod().getName());
			}
		});
	}

	private boolean matchesPath(RequestMappingInfo info, String path) {
		if (info.getPathPatternsCondition() != null) {
			return info.getPathPatternsCondition().getPatternValues().contains(path);
		}
		if (info.getPatternsCondition() != null) {
			return info.getPatternsCondition().getPatterns().contains(path);
		}
		return false;
	}

	private String describeMapping(RequestMappingInfo info) {
		Set<String> methods = info.getMethodsCondition().getMethods().stream()
				.map(Enum::name)
				.collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

		Set<String> patterns;
		if (info.getPathPatternsCondition() != null) {
			patterns = info.getPathPatternsCondition().getPatternValues();
		} else if (info.getPatternsCondition() != null) {
			patterns = info.getPatternsCondition().getPatterns();
		} else {
			patterns = Set.of("<unknown>");
		}

		return methods + " " + patterns;
	}
}
