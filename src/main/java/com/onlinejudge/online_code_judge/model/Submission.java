package com.onlinejudge.online_code_judge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "submissions")
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId; // ✅ ADD THIS

	private Long problemId;

	@Enumerated(EnumType.STRING)
	private Language language;

	@Column(length = 10000)
	private String code;

	@Column(length = 20)
	private String status;

	private String verdict;

	public Submission() {
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() { // ✅ ADD GETTER
		return userId;
	}

	public void setUserId(Long userId) { // ✅ ADD SETTER
		this.userId = userId;
	}

	public Long getProblemId() {
		return problemId;
	}

	public void setProblemId(Long problemId) {
		this.problemId = problemId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getVerdict() {
		return verdict;
	}

	public void setVerdict(String verdict) {
		this.verdict = verdict;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}