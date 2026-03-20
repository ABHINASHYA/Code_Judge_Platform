package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.dto.UserDashboardResponse;
import com.onlinejudge.online_code_judge.model.User;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final SubmissionRepository submissionRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository,
			SubmissionRepository submissionRepository,
			PasswordEncoder passwordEncoder) {

		this.userRepository = userRepository;
		this.submissionRepository = submissionRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User registerUser(User user) {

		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			throw new RuntimeException("Username already exists");
		}

		if (userRepository.findByEmail(user.getEmail()).isPresent()) {
			throw new RuntimeException("Email already exists");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole("USER");

		return userRepository.save(user);
	}

	public UserDashboardResponse getDashboard(Long userId) {

		long total = submissionRepository.countByUserId(userId);

		long accepted = submissionRepository.countByUserIdAndVerdict(userId, "Accepted");

		long solved = submissionRepository.findByUserId(userId)
				.stream()
				.filter(s -> "Accepted".equals(s.getVerdict()))
				.map(s -> s.getProblemId())
				.distinct()
				.count();

		return new UserDashboardResponse(total, accepted, solved);
	}
}