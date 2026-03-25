package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.dto.LeaderboardEntry;
import com.onlinejudge.online_code_judge.model.Submission;
import com.onlinejudge.online_code_judge.model.User;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeaderboardService {

	private final SubmissionRepository submissionRepository;
	private final UserRepository userRepository;

	public LeaderboardService(SubmissionRepository submissionRepository,
			UserRepository userRepository) {
		this.submissionRepository = submissionRepository;
		this.userRepository = userRepository;
	}

	public List<LeaderboardEntry> getLeaderboard() {

		List<Submission> submissions = submissionRepository.findAll();

		Map<Long, Set<Long>> solvedProblems = new HashMap<>();

		for (Submission submission : submissions) {

			if (!isAccepted(submission))
				continue;
			if (submission.getUserId() == null || submission.getProblemId() == null)
				continue;

			solvedProblems
					.computeIfAbsent(submission.getUserId(), k -> new HashSet<>())
					.add(submission.getProblemId());
		}

		List<LeaderboardEntry> leaderboard = new ArrayList<>();

		for (Long userId : solvedProblems.keySet()) {

			User user = userRepository.findById(userId).orElse(null);

			if (user == null)
				continue;

			leaderboard.add(
					new LeaderboardEntry(
							userId,
							user.getUsername(),
							solvedProblems.get(userId).size(),
							submissionRepository.countByUserId(userId)));
		}

		leaderboard.sort((a, b) -> {
			int solvedCompare = Long.compare(b.getProblemsSolved(), a.getProblemsSolved());
			if (solvedCompare != 0) {
				return solvedCompare;
			}
			return Long.compare(a.getSubmissionCount(), b.getSubmissionCount());
		});

		return leaderboard;
	}

	private boolean isAccepted(Submission submission) {
		if (submission == null) {
			return false;
		}
		String status = submission.getStatus() == null ? "" : submission.getStatus().trim();
		String verdict = submission.getVerdict() == null ? "" : submission.getVerdict().trim();
		return "ACCEPTED".equalsIgnoreCase(status) || "Accepted".equalsIgnoreCase(verdict);
	}
}
