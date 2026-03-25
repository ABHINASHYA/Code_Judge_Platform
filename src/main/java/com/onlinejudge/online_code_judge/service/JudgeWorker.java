package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.model.Submission;
import com.onlinejudge.online_code_judge.model.TestCase;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.repository.TestCaseRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class JudgeWorker {

	private final SubmissionQueue submissionQueue;
	private final CodeExecutionService executionService;
	private final SubmissionRepository submissionRepository;
	private final TestCaseRepository testCaseRepository;

	// Number of parallel judge workers
	private static final int WORKER_COUNT = 4;

	public JudgeWorker(
			SubmissionQueue submissionQueue,
			CodeExecutionService executionService,
			SubmissionRepository submissionRepository,
			TestCaseRepository testCaseRepository) {

		this.submissionQueue = submissionQueue;
		this.executionService = executionService;
		this.submissionRepository = submissionRepository;
		this.testCaseRepository = testCaseRepository;
	}

	@PostConstruct
	public void startWorkers() {

		ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);

		for (int i = 0; i < WORKER_COUNT; i++) {

			executor.submit(() -> {

				while (true) {

					Submission submission = null;
					try {

						submission = submissionQueue.takeSubmission();

						List<TestCase> testCases = testCaseRepository.findByProblemId(submission.getProblemId());

						String verdict = "Accepted";
						long totalRuntimeMs = 0L;
						submission.setFailedInput(null);
						submission.setExpectedOutput(null);
						submission.setActualOutput(null);

						for (TestCase tc : testCases) {
							long startNanos = System.nanoTime();

							String output = executionService.executeCode(
									submission.getCode(),
									tc.getInputData(),
									submission.getLanguage());
							long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
							totalRuntimeMs += Math.max(elapsedMs, 0L);

							if (output.startsWith("Compilation Error")
									|| output.startsWith("Runtime Error")
									|| output.startsWith("Time Limit Exceeded")
									|| output.startsWith("Internal Error")) {

								verdict = output;
								submission.setFailedInput(clipText(tc.getInputData()));
								submission.setExpectedOutput(clipText(tc.getExpectedOutput()));
								submission.setActualOutput(clipText(output));
								break;
							}

							if (!output.trim().equals(tc.getExpectedOutput().trim())) {
								verdict = "Wrong Answer";
								submission.setFailedInput(clipText(tc.getInputData()));
								submission.setExpectedOutput(clipText(tc.getExpectedOutput()));
								submission.setActualOutput(clipText(output));
								break;
							}
						}

						submission.setVerdict(verdict);
						submission.setStatus(mapVerdictToStatus(verdict));
						submission.setRuntime(totalRuntimeMs);

						submissionRepository.save(submission);

					} catch (Exception e) {
						e.printStackTrace();
						if (submission != null) {
							submission.setVerdict("Internal Error");
							submission.setStatus("INTERNAL_ERROR");
							submission.setActualOutput(clipText(e.getMessage()));
							submissionRepository.save(submission);
						}
					}

				}

			});

		}

	}

	private String mapVerdictToStatus(String verdict) {
		if (verdict == null) {
			return "INTERNAL_ERROR";
		}
		if ("Accepted".equalsIgnoreCase(verdict)) {
			return "ACCEPTED";
		}
		if ("Wrong Answer".equalsIgnoreCase(verdict)) {
			return "WRONG_ANSWER";
		}
		if (verdict.startsWith("Time Limit Exceeded")) {
			return "TLE";
		}
		if (verdict.startsWith("Runtime Error")) {
			return "RUNTIME_ERROR";
		}
		if (verdict.startsWith("Compilation Error")) {
			return "COMPILE_ERROR";
		}
		if (verdict.startsWith("Internal Error")) {
			return "INTERNAL_ERROR";
		}
		return "INTERNAL_ERROR";
	}

	private String clipText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		int maxLen = 3000;
		if (trimmed.length() > maxLen) {
			return trimmed.substring(0, maxLen) + "...";
		}
		return trimmed;
	}
}
