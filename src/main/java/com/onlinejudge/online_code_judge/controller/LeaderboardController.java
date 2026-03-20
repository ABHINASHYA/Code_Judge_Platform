package com.onlinejudge.online_code_judge.controller;

import com.onlinejudge.online_code_judge.dto.LeaderboardEntry;
import com.onlinejudge.online_code_judge.service.LeaderboardService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

	private final LeaderboardService leaderboardService;

	public LeaderboardController(LeaderboardService leaderboardService) {
		this.leaderboardService = leaderboardService;
	}

	@GetMapping
	public List<LeaderboardEntry> leaderboard() {
		return leaderboardService.getLeaderboard();
	}
}
