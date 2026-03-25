package com.onlinejudge.online_code_judge.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.onlinejudge.online_code_judge.dto.RunRequest;
import com.onlinejudge.online_code_judge.service.CodeExecutionService;

@RestController
@RequestMapping("/api/run")
public class RunController {

	private final CodeExecutionService codeExecutionService;

	public RunController(CodeExecutionService codeExecutionService) {
		this.codeExecutionService = codeExecutionService;
	}

	@PostMapping
	public Map<String, Object> run(@RequestBody RunRequest request) {
		if (request == null || request.getCode() == null || request.getCode().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required");
		}
		if (request.getLanguage() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Language is required");
		}

		try {
			String output = codeExecutionService.executeCode(
					request.getCode(),
					request.getInput() == null ? "" : request.getInput(),
					request.getLanguage());

			String status = mapRunStatus(output);
			return Map.of(
					"status", status,
					"output", output);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Run failed");
		}
	}

	private String mapRunStatus(String output) {
		if (output == null) {
			return "INTERNAL_ERROR";
		}
		if (output.startsWith("Compilation Error")) {
			return "COMPILE_ERROR";
		}
		if (output.startsWith("Runtime Error")) {
			return "RUNTIME_ERROR";
		}
		if (output.startsWith("Time Limit Exceeded")) {
			return "TLE";
		}
		if (output.startsWith("Internal Error")) {
			return "INTERNAL_ERROR";
		}
		return "RUN_COMPLETED";
	}
}
