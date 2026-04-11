package com.onlinejudge.online_code_judge.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;
	private final List<String> corsAllowedOriginPatterns;

	public SecurityConfig(
			JwtAuthenticationFilter jwtFilter,
			@Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*,https://*.up.railway.app,https://*.railway.app,https://*.vercel.app}") String allowedOriginsCsv) {
		this.jwtFilter = jwtFilter;
		this.corsAllowedOriginPatterns = java.util.Arrays.stream(allowedOriginsCsv.split(","))
				.map(String::trim)
				.filter(value -> !value.isBlank())
				.toList();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// Public APIs
						.requestMatchers(
								"/api/auth/**",
								"/ping",
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

						// User APIs
						.anyRequest().authenticated())
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> writeJsonError(
								response,
								HttpStatus.UNAUTHORIZED.value(),
								"Unauthorized"))
						.accessDeniedHandler((request, response, accessDeniedException) -> writeJsonError(
								response,
								HttpStatus.FORBIDDEN.value(),
								"Forbidden")))

				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.requestCache(cache -> cache.disable())
				.logout(logout -> logout.disable());

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(corsAllowedOriginPatterns);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private void writeJsonError(HttpServletResponse response, int status, String message) throws java.io.IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + message + "\"}");
	}
}
