package com.onlinejudge.online_code_judge.repository;

import com.onlinejudge.online_code_judge.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

	List<Submission> findByUserId(Long userId);

	long countByUserId(Long userId);

	long countByUserIdAndVerdict(Long userId, String verdict);

}