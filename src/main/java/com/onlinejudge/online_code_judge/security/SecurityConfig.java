package com.onlinejudge.online_code_judge.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

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
				.cors(Customizer.withDefaults())

				.authorizeHttpRequests(auth -> auth

						// Public APIs
						.requestMatchers(
								"/api/auth/**",
								"/error",
								"/api/users/register")
						.permitAll()

						// ADMIN APIs
						.requestMatchers(HttpMethod.POST, "/api/problems", "/api/testcases/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/problems/**", "/api/testcases/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/problems/**", "/api/testcases/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.OPTIONS, "/**")
						.permitAll()

						// User APIs
						.anyRequest().authenticated())
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> response.sendError(
								HttpServletResponse.SC_UNAUTHORIZED,
								"Unauthorized"))
						.accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(
								HttpServletResponse.SC_FORBIDDEN,
								"Forbidden")))

				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable());

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of(
				"http://localhost:*",
				"http://127.0.0.1:*",
				"http://192.168.*:*",
				"http://10.*:*",
				"http://172.16.*:*",
				"http://172.17.*:*",
				"http://172.18.*:*",
				"http://172.19.*:*",
				"http://172.20.*:*",
				"http://172.21.*:*",
				"http://172.22.*:*",
				"http://172.23.*:*",
				"http://172.24.*:*",
				"http://172.25.*:*",
				"http://172.26.*:*",
				"http://172.27.*:*",
				"http://172.28.*:*",
				"http://172.29.*:*",
				"http://172.30.*:*",
				"http://172.31.*:*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
