package com.onlinejudge.online_code_judge.service;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.onlinejudge.online_code_judge.config.Judge0Properties;
import com.onlinejudge.online_code_judge.dto.CodeRequest;
import com.onlinejudge.online_code_judge.dto.CodeResponse;
import com.onlinejudge.online_code_judge.exception.Judge0IntegrationException;
import com.onlinejudge.online_code_judge.model.Language;

@Service
public class Judge0Service {

	private static final Map<Language, Integer> LANGUAGE_IDS = Map.of(
			Language.JAVA, 62,
			Language.CPP, 54,
			Language.PYTHON, 71);

	private static final Set<Integer> RUNTIME_ERROR_STATUS_IDS = Set.of(5, 7, 8, 9, 10, 11, 12, 14);

	private final RestTemplate restTemplate;
	private final Judge0Properties properties;

	public Judge0Service(
			@Qualifier("judge0RestTemplate") RestTemplate restTemplate,
			Judge0Properties properties) {
		this.restTemplate = restTemplate;
		this.properties = properties;
	}

	public CodeResponse execute(CodeRequest request) {
		ExecutionResult result = submitToJudge0(request.getCode(), request.getInput(), request.getLanguage());

		if (result.statusId == 3) {
			return new CodeResponse(result.output, "Accepted");
		}

		if (result.statusId == 6) {
			return new CodeResponse(result.output, "Compilation Error");
		}

		if (RUNTIME_ERROR_STATUS_IDS.contains(result.statusId)) {
			return new CodeResponse(result.output, "Runtime Error");
		}

		throw new Judge0IntegrationException("Judge0 execution failed: " + result.statusDescription);
	}

	// Compatibility output used by current async judge worker pipeline.
	public String executeForJudge(String code, String input, Language language) {
		ExecutionResult result = submitToJudge0(code, input, language);

		if (result.statusId == 3) {
			return result.output;
		}
		if (result.statusId == 6) {
			return withPrefix("Compilation Error", result.output);
		}
		if (result.statusId == 5) {
			return "Time Limit Exceeded";
		}
		if (RUNTIME_ERROR_STATUS_IDS.contains(result.statusId)) {
			return withPrefix("Runtime Error", result.output);
		}
		return withPrefix("Internal Error", result.statusDescription);
	}

	private ExecutionResult submitToJudge0(String code, String input, Language language) {
		Integer languageId = LANGUAGE_IDS.get(language);
		if (languageId == null) {
			throw new IllegalArgumentException("Unsupported language. Use JAVA, CPP, or PYTHON.");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Judge0SubmissionRequest payload = new Judge0SubmissionRequest();
		payload.setLanguageId(languageId);
		payload.setSourceCode(code);
		payload.setStdin(input == null ? "" : input);

		HttpEntity<Judge0SubmissionRequest> entity = new HttpEntity<>(payload, headers);

		try {
			ResponseEntity<Judge0SubmissionResponse> response = restTemplate.exchange(
					properties.getSubmissionPath(),
					HttpMethod.POST,
					entity,
					Judge0SubmissionResponse.class);

			Judge0SubmissionResponse body = response.getBody();
			if (body == null || body.getStatus() == null || body.getStatus().getId() == null) {
				throw new Judge0IntegrationException("Invalid response from Judge0");
			}

			int statusId = body.getStatus().getId();
			String statusDescription = body.getStatus().getDescription() == null
					? "Unknown"
					: body.getStatus().getDescription();
			String output = resolveOutput(body);

			return new ExecutionResult(statusId, statusDescription, output);
		} catch (HttpStatusCodeException ex) {
			throw new Judge0IntegrationException(
					"Judge0 request failed with HTTP " + ex.getStatusCode().value(),
					ex);
		} catch (ResourceAccessException ex) {
			throw new Judge0IntegrationException("Judge0 request timed out or is unreachable", ex);
		} catch (RestClientException ex) {
			throw new Judge0IntegrationException("Judge0 request failed", ex);
		}
	}

	private String resolveOutput(Judge0SubmissionResponse body) {
		if (hasText(body.getStdout())) {
			return body.getStdout().trim();
		}
		if (hasText(body.getCompileOutput())) {
			return body.getCompileOutput().trim();
		}
		if (hasText(body.getStderr())) {
			return body.getStderr().trim();
		}
		if (hasText(body.getMessage())) {
			return body.getMessage().trim();
		}
		return "";
	}

	private String withPrefix(String prefix, String details) {
		if (!hasText(details)) {
			return prefix;
		}
		return prefix + ": " + details.trim();
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private static class ExecutionResult {
		private final int statusId;
		private final String statusDescription;
		private final String output;

		private ExecutionResult(int statusId, String statusDescription, String output) {
			this.statusId = statusId;
			this.statusDescription = statusDescription;
			this.output = output;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Judge0SubmissionRequest {

		@JsonProperty("source_code")
		private String sourceCode;

		@JsonProperty("language_id")
		private Integer languageId;

		@JsonProperty("stdin")
		private String stdin;

		public String getSourceCode() {
			return sourceCode;
		}

		public void setSourceCode(String sourceCode) {
			this.sourceCode = sourceCode;
		}

		public Integer getLanguageId() {
			return languageId;
		}

		public void setLanguageId(Integer languageId) {
			this.languageId = languageId;
		}

		public String getStdin() {
			return stdin;
		}

		public void setStdin(String stdin) {
			this.stdin = stdin;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Judge0SubmissionResponse {

		@JsonProperty("stdout")
		private String stdout;

		@JsonProperty("stderr")
		private String stderr;

		@JsonProperty("compile_output")
		private String compileOutput;

		@JsonProperty("message")
		private String message;

		@JsonProperty("status")
		private Judge0Status status;

		public String getStdout() {
			return stdout;
		}

		public void setStdout(String stdout) {
			this.stdout = stdout;
		}

		public String getStderr() {
			return stderr;
		}

		public void setStderr(String stderr) {
			this.stderr = stderr;
		}

		public String getCompileOutput() {
			return compileOutput;
		}

		public void setCompileOutput(String compileOutput) {
			this.compileOutput = compileOutput;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Judge0Status getStatus() {
			return status;
		}

		public void setStatus(Judge0Status status) {
			this.status = status;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Judge0Status {

		@JsonProperty("id")
		private Integer id;

		@JsonProperty("description")
		private String description;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
}
