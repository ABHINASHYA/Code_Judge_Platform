package com.onlinejudge.online_code_judge.controller;

import com.onlinejudge.online_code_judge.model.Problem;
import com.onlinejudge.online_code_judge.service.ProblemService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

	private final ProblemService problemService;

	public ProblemController(ProblemService problemService) {
		this.problemService = problemService;
	}

	// ADMIN API
	@PostMapping
	public Problem createProblem(@RequestBody Problem problem) {
		return problemService.createProblem(problem);
	}

	// USER API (PAGINATION)
	@GetMapping
	public Page<Problem> getProblems(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return problemService.getProblems(page, size);
	}

	// GET SINGLE PROBLEM
	@GetMapping("/{id}")
	public Problem getProblem(@PathVariable Long id) {
		return problemService.getProblemById(id);
	}

	@PutMapping("/{id}")
	public Problem updateProblem(@PathVariable Long id, @RequestBody Problem problem) {
		return problemService.updateProblem(id, problem);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, String> deleteProblem(@PathVariable Long id) {
		problemService.deleteProblem(id);
		return Map.of("message", "Problem deleted");
	}
}
