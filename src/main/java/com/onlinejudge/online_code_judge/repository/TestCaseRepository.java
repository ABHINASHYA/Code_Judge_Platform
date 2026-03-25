package com.onlinejudge.online_code_judge.repository;

import com.onlinejudge.online_code_judge.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
	List<TestCase> findByProblemId(Long problemId);

	void deleteByProblemId(Long problemId);
}
