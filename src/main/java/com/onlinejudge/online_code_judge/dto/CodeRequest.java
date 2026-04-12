package com.onlinejudge.online_code_judge.dto;

import com.onlinejudge.online_code_judge.model.Language;

public class CodeRequest {

	private String code;
	private String input;
	private Language language;

	public CodeRequest() {
	}

	public CodeRequest(String code, String input, Language language) {
		this.code = code;
		this.input = input;
		this.language = language;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}
}
