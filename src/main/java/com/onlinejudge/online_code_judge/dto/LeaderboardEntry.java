package com.onlinejudge.online_code_judge.dto;

public class LeaderboardEntry {

	private Long userId;
	private String username;
	private long problemsSolved;

	public LeaderboardEntry(Long userId, String username, long problemsSolved) {
		this.userId = userId;
		this.username = username;
		this.problemsSolved = problemsSolved;
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
}
