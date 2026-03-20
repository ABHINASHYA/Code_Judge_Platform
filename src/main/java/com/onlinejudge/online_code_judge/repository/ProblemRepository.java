package com.onlinejudge.online_code_judge.repository;

import com.onlinejudge.online_code_judge.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
}
