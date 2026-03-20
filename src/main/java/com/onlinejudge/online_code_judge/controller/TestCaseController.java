package com.onlinejudge.online_code_judge.controller;

import com.onlinejudge.online_code_judge.model.TestCase;
import com.onlinejudge.online_code_judge.service.TestCaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testcases")
public class TestCaseController {

	private final TestCaseService testCaseService;

	public TestCaseController(TestCaseService testCaseService) {
		this.testCaseService = testCaseService;
	}

	@PostMapping("/{problemId}")
	public TestCase addTestCase(
			@PathVariable Long problemId,
			@RequestBody TestCase testCase) {

		return testCaseService.addTestCase(problemId, testCase);
	}

	@GetMapping("/{problemId}")
	public List<TestCase> getTestCases(@PathVariable Long problemId) {
		return testCaseService.getTestCasesByProblem(problemId);
	}
}
