package com.onlinejudge.online_code_judge.controller;

import com.onlinejudge.online_code_judge.dto.UserDashboardResponse;
import com.onlinejudge.online_code_judge.model.User;
import com.onlinejudge.online_code_judge.repository.SubmissionRepository;
import com.onlinejudge.online_code_judge.service.UserService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final SubmissionRepository submissionRepository;

	public UserController(UserService userService,
			SubmissionRepository submissionRepository) {
		this.userService = userService;
		this.submissionRepository = submissionRepository;
	}

	@PostMapping("/register")
	public User register(@RequestBody User user) {
		return userService.registerUser(user);
	}

	@GetMapping("/{userId}/dashboard")
	public UserDashboardResponse dashboard(@PathVariable Long userId) {
		return userService.getDashboard(userId);
	}

	@GetMapping("/{userId}/submissions")
	public Object submissions(@PathVariable Long userId) {
		return submissionRepository.findByUserId(userId);
	}
}