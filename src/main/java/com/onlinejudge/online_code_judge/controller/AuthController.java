package com.onlinejudge.online_code_judge.controller;

import com.onlinejudge.online_code_judge.dto.LoginRequest;
import com.onlinejudge.online_code_judge.dto.LoginResponse;
import com.onlinejudge.online_code_judge.model.User;
import com.onlinejudge.online_code_judge.repository.UserRepository;
import com.onlinejudge.online_code_judge.security.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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

	@PostMapping(
			path = { "/login", "/login/" },
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public LoginResponse login(@RequestBody LoginRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}

		String identity = firstNonBlank(request.getUsername(), request.getEmail());
		if (identity == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or email is required");
		}
		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
		}

		User user = userRepository.findByUsername(identity)
				.or(() -> userRepository.findByEmail(identity))
				.orElseThrow(() -> {
					log.warn("Login failed for identity='{}': user not found", identity);
					return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
				});

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			log.warn("Login failed for identity='{}': invalid password", identity);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}

		String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
		log.info("Login success for user='{}' role='{}'", user.getUsername(), user.getRole());

		LoginResponse.UserSummary userSummary = new LoginResponse.UserSummary(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole());

		return new LoginResponse(token, userSummary);
	}

	@GetMapping(path = { "/ping", "/ping/" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> authPing() {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("status", "ok");
		response.put("service", "auth");
		response.put("timestamp", Instant.now().toString());
		return response;
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
