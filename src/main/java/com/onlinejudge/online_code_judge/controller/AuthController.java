package com.onlinejudge.online_code_judge.controller;

import com.onlinejudge.online_code_judge.dto.LoginRequest;
import com.onlinejudge.online_code_judge.dto.LoginResponse;
import com.onlinejudge.online_code_judge.model.User;
import com.onlinejudge.online_code_judge.repository.UserRepository;
import com.onlinejudge.online_code_judge.security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	public AuthController(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtUtil jwtUtil) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request) {

		String identity = firstNonBlank(request.getUsername(), request.getEmail());
		if (identity == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or email is required");
		}
		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
		}

		User user = userRepository.findByUsername(identity)
				.or(() -> userRepository.findByEmail(identity))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}

		String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
		LoginResponse.UserSummary userSummary = new LoginResponse.UserSummary(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole());

		return new LoginResponse(token, userSummary);
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value.trim();
			}
		}
		return null;
	}
}
