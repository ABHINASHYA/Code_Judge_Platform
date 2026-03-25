package com.onlinejudge.online_code_judge.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.onlinejudge.online_code_judge.model.Language;
import com.onlinejudge.online_code_judge.model.Submission;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.repository.ProblemRepository;

@Service
public class SubmissionService {

	private final SubmissionRepository submissionRepository;
	private final SubmissionQueue submissionQueue;
	private final ProblemRepository problemRepository;

	public SubmissionService(
			SubmissionRepository submissionRepository,
			SubmissionQueue submissionQueue,
			ProblemRepository problemRepository) {

		this.submissionRepository = submissionRepository;
		this.submissionQueue = submissionQueue;
		this.problemRepository = problemRepository;
	}

	public Submission submitCode(Long userId, Long problemId, String code, Language language) {
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
		}
		if (problemId == null || !problemRepository.existsById(problemId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem not found");
		}
		if (code == null || code.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required");
		}
		if (language == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Language is required");
		}

		Submission submission = new Submission();

		submission.setUserId(userId);
		submission.setProblemId(problemId);
		submission.setCode(code);
		submission.setLanguage(language);

		submission.setVerdict("PENDING");
		submission.setStatus("PENDING");

		submission = submissionRepository.save(submission);

		submissionQueue.addSubmission(submission);

		return submission;
	}
}
