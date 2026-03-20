package com.onlinejudge.online_code_judge.dto;

public class UserDashboardResponse {

	private long totalSubmissions;
	private long acceptedSubmissions;
	private long problemsSolved;

	public UserDashboardResponse(long totalSubmissions,
			long acceptedSubmissions,
			long problemsSolved) {
		this.totalSubmissions = totalSubmissions;
		this.acceptedSubmissions = acceptedSubmissions;
		this.problemsSolved = problemsSolved;
	}

	public long getTotalSubmissions() {
		return totalSubmissions;
	}

	public long getAcceptedSubmissions() {
		return acceptedSubmissions;
	}

	public long getProblemsSolved() {
		return problemsSolved;
	}
}