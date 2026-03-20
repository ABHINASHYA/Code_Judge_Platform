package com.onlinejudge.online_code_judge.dto;

import com.onlinejudge.online_code_judge.model.Language;

public class SubmissionRequest {

	private String code;
	private Language language;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}
}
