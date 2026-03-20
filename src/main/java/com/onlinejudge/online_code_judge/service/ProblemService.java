package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.model.Problem;
import com.onlinejudge.online_code_judge.repository.ProblemRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ProblemService {

	private final ProblemRepository problemRepository;

	public ProblemService(ProblemRepository problemRepository) {
		this.problemRepository = problemRepository;
	}

	public Problem createProblem(Problem problem) {
		return problemRepository.save(problem);
	}

	public Page<Problem> getProblems(int page, int size) {
		return problemRepository.findAll(PageRequest.of(page, size));
	}

	public Problem getProblemById(Long id) {
		return problemRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Problem not found"));
	}
}