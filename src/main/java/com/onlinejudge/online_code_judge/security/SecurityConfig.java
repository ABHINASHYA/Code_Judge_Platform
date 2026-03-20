package com.onlinejudge.online_code_judge.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				.csrf(csrf -> csrf.disable())

				.authorizeHttpRequests(auth -> auth

						// Public APIs
						.requestMatchers(
								"/api/auth/**",
								"/api/users/register")
						.permitAll()

						// ADMIN APIs
						.requestMatchers(
								"/api/problems",
								"/api/testcases")
						.hasRole("ADMIN")

						// User APIs
						.anyRequest().authenticated())

				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable());

		return http.build();
	}
}