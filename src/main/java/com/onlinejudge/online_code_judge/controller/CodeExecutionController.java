package com.onlinejudge.online_code_judge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.onlinejudge.online_code_judge.dto.CodeRequest;
import com.onlinejudge.online_code_judge.dto.CodeResponse;
import com.onlinejudge.online_code_judge.service.Judge0Service;

@RestController
@RequestMapping("/api/code-execution")
public class CodeExecutionController {

	private final Judge0Service judge0Service;

	public CodeExecutionController(Judge0Service judge0Service) {
		this.judge0Service = judge0Service;
	}

	@PostMapping
	public CodeResponse execute(@RequestBody CodeRequest request) {
		validate(request);
		return judge0Service.execute(request);
	}

	private void validate(CodeRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}
		if (request.getCode() == null || request.getCode().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required");
		}
		if (request.getLanguage() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Language is required");
		}
	}
}
