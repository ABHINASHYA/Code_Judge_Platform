package com.onlinejudge.online_code_judge.dto;

public class LoginResponse {

	private final String token;
	private final UserSummary user;

	public LoginResponse(String token, UserSummary user) {
		this.token = token;
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public UserSummary getUser() {
		return user;
	}

	public static class UserSummary {
		private final Long id;
		private final String username;
		private final String email;
		private final String role;

		public UserSummary(Long id, String username, String email, String role) {
			this.id = id;
			this.username = username;
			this.email = email;
			this.role = role;
		}

		public Long getId() {
			return id;
		}

		public String getUsername() {
			return username;
		}

		public String getEmail() {
			return email;
		}

		public String getRole() {
			return role;
		}
	}
}
