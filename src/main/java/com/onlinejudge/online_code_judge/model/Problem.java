package com.onlinejudge.online_code_judge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "problems")
public class Problem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String title;

	@Column(length = 5000)
	private String description;

	private String difficulty; // EASY, MEDIUM, HARD

	// Avoid recursive/lazy serialization for entity responses.
	@JsonIgnore
	@OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
	private List<TestCase> testCases;

	public Problem() {
	}

	// Getters & Setters
	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public List<TestCase> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<TestCase> testCases) {
		this.testCases = testCases;
	}
}
