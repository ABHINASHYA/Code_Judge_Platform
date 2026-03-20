package com.onlinejudge.online_code_judge.service;

import org.springframework.stereotype.Service;

import com.onlinejudge.online_code_judge.model.Language;
import com.onlinejudge.online_code_judge.model.Submission;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;

@Service
public class SubmissionService {

	private final SubmissionRepository submissionRepository;
	private final SubmissionQueue submissionQueue;

	public SubmissionService(
			SubmissionRepository submissionRepository,
			SubmissionQueue submissionQueue) {

		this.submissionRepository = submissionRepository;
		this.submissionQueue = submissionQueue;
	}

	public Submission submitCode(Long problemId, String code, Language language) {

		Submission submission = new Submission();

		submission.setProblemId(problemId);
		submission.setCode(code);
		submission.setLanguage(language);

		submission.setVerdict("Pending");
		submission.setStatus("Pending");

		submission = submissionRepository.save(submission);

		submissionQueue.addSubmission(submission);

		return submission;
	}
}