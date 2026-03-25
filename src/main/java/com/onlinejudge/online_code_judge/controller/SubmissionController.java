package com.onlinejudge.online_code_judge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.onlinejudge.online_code_judge.dto.SubmissionRequest;
import com.onlinejudge.online_code_judge.model.Submission;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.repository.UserRepository;
import com.onlinejudge.online_code_judge.service.SubmissionService;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

	private final SubmissionService submissionService;
	private final SubmissionRepository submissionRepository;
	private final UserRepository userRepository;

	public SubmissionController(
			SubmissionService submissionService,
			SubmissionRepository submissionRepository,
			UserRepository userRepository) {

		this.submissionService = submissionService;
		this.submissionRepository = submissionRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Submit code for a problem
	 */
	@PostMapping("/{problemId}")
	public Submission submitCode(
			@PathVariable Long problemId,
			@RequestBody SubmissionRequest request,
			Authentication authentication) {

		Long userId = resolveCurrentUserId(authentication);

		return submissionService.submitCode(
				userId,
				problemId,
				request.getCode(),
				request.getLanguage());
	}

	/**
	 * Get submission status
	 */
	@GetMapping("/user")
	public List<Submission> getCurrentUserSubmissions(Authentication authentication) {
		Long userId = resolveCurrentUserId(authentication);
		return submissionRepository.findByUserId(userId);
	}

	/**
	 * Get submission status
	 */
	@GetMapping("/{submissionId}")
	public Submission getSubmissionStatus(
			@PathVariable Long submissionId,
			Authentication authentication) {

		Long userId = resolveCurrentUserId(authentication);
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

		if (submission.getUserId() != null && !submission.getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
		}

		return submission;
	}

	/**
	 * Get all submissions
	 */
	@GetMapping
	public List<Submission> getAllSubmissions() {
		return submissionRepository.findAll();
	}

	private Long resolveCurrentUserId(Authentication authentication) {
		String principal = authentication == null ? null : authentication.getName();
		if (principal == null || principal.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
		}

		return userRepository.findByUsername(principal)
				.or(() -> userRepository.findByEmail(principal))
				.map(user -> user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
	}
}
