package com.onlinejudge.online_code_judge.dto;

public class LeaderboardEntry {

	private Long userId;
	private String username;
	private long problemsSolved;
	private long submissionCount;

	public LeaderboardEntry(Long userId, String username, long problemsSolved, long submissionCount) {
		this.userId = userId;
		this.username = username;
		this.problemsSolved = problemsSolved;
		this.submissionCount = submissionCount;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public long getProblemsSolved() {
		return problemsSolved;
	}

	public long getSolvedCount() {
		return problemsSolved;
	}

	public long getSubmissionCount() {
		return submissionCount;
	}
}
