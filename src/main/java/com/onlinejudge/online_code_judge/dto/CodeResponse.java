package com.onlinejudge.online_code_judge.dto;

public class CodeResponse {

	private String output;
	private String status;

	public CodeResponse() {
	}

	public CodeResponse(String output, String status) {
		this.output = output;
		this.status = status;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
