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

					try {

						Submission submission = submissionQueue.takeSubmission();

						List<TestCase> testCases = testCaseRepository.findByProblemId(submission.getProblemId());

						String verdict = "Accepted";

						for (TestCase tc : testCases) {

							String output = executionService.executeCode(
									submission.getCode(),
									tc.getInputData(),
									submission.getLanguage());

							if (output.equals("Compilation Error")
									|| output.equals("Runtime Error")
									|| output.equals("Time Limit Exceeded")) {

								verdict = output;
								break;
							}

							if (!output.trim().equals(tc.getExpectedOutput().trim())) {
								verdict = "Wrong Answer";
								break;
							}
						}

						submission.setVerdict(verdict);
						submission.setStatus("Finished");

						submissionRepository.save(submission);

					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			});

		}

	}
}