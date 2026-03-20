package com.onlinejudge.online_code_judge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onlinejudge.online_code_judge.dto.SubmissionRequest;
import com.onlinejudge.online_code_judge.model.Submission;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.service.SubmissionService;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

	private final SubmissionService submissionService;
	private final SubmissionRepository submissionRepository;

	public SubmissionController(
			SubmissionService submissionService,
			SubmissionRepository submissionRepository) {

		this.submissionService = submissionService;
		this.submissionRepository = submissionRepository;
	}

	/**
	 * Submit code for a problem
	 */
	@PostMapping("/{problemId}")
	public Submission submitCode(
			@PathVariable Long problemId,
			@RequestBody SubmissionRequest request) {

		return submissionService.submitCode(
				problemId,
				request.getCode(),
				request.getLanguage());
	}

	/**
	 * Get submission status
	 */
	@GetMapping("/{submissionId}")
	public Submission getSubmissionStatus(@PathVariable Long submissionId) {

		return submissionRepository.findById(submissionId)
				.orElseThrow(() -> new RuntimeException("Submission not found"));
	}

	/**
	 * Get all submissions
	 */
	@GetMapping
	public List<Submission> getAllSubmissions() {
		return submissionRepository.findAll();
	}
}