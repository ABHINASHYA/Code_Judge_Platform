package com.onlinejudge.online_code_judge.service;

import org.springframework.stereotype.Service;

import com.onlinejudge.online_code_judge.model.Language;

@Service
public class CodeExecutionService {

	private final Judge0Service judge0Service;

	public CodeExecutionService(Judge0Service judge0Service) {
		this.judge0Service = judge0Service;
	}

	public String executeCode(String code, String input, Language language) {
		return judge0Service.executeForJudge(code, input, language);
	}
}
