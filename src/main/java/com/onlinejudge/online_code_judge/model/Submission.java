package com.onlinejudge.online_code_judge.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String code;

	@Column(length = 20)
	private String status;

	private String verdict;

	private Long runtime;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String failedInput;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String expectedOutput;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String actualOutput;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

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

	public Long getRuntime() {
		return runtime;
	}

	public void setRuntime(Long runtime) {
		this.runtime = runtime;
	}

	public String getFailedInput() {
		return failedInput;
	}

	public void setFailedInput(String failedInput) {
		this.failedInput = failedInput;
	}

	public String getExpectedOutput() {
		return expectedOutput;
	}

	public void setExpectedOutput(String expectedOutput) {
		this.expectedOutput = expectedOutput;
	}

	public String getActualOutput() {
		return actualOutput;
	}

	public void setActualOutput(String actualOutput) {
		this.actualOutput = actualOutput;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
