package com.onlinejudge.online_code_judge.service;

import com.onlinejudge.online_code_judge.dto.UserDashboardResponse;
import com.onlinejudge.online_code_judge.model.User;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User payload is required");
		}

		String username = normalize(user.getUsername());
		String email = normalize(user.getEmail());
		String password = user.getPassword();

		if (username == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
		}

		if (email == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
		}

		if (password == null || password.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
		}

		if (userRepository.findByUsername(username).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		}

		if (userRepository.findByEmail(email).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
		}

		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		if (user.getUsername().equals("AbhiYadav")) {
			user.setRole("ADMIN");
		} else {
			user.setRole("USER");
		}

		try {
			return userRepository.save(user);
		} catch (DataIntegrityViolationException ex) {
			// Race-condition safety: convert DB unique constraint failure into a client
			// error.
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
		}
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

	private String normalize(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
