package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.model.Problem;
import com.onlinejudge.online_code_judge.model.TestCase;
import com.onlinejudge.online_code_judge.repository.ProblemRepository;
import com.onlinejudge.online_code_judge.repository.TestCaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestCaseService {

	private final TestCaseRepository testCaseRepository;
	private final ProblemRepository problemRepository;

	public TestCaseService(TestCaseRepository testCaseRepository,
			ProblemRepository problemRepository) {
		this.testCaseRepository = testCaseRepository;
		this.problemRepository = problemRepository;
	}

	public TestCase addTestCase(Long problemId, TestCase testCase) {

		Problem problem = problemRepository.findById(problemId)
				.orElseThrow(() -> new RuntimeException("Problem not found"));

		testCase.setProblem(problem);

		return testCaseRepository.save(testCase);
	}

	public List<TestCase> getTestCasesByProblem(Long problemId) {
		return testCaseRepository.findByProblemId(problemId);
	}
}