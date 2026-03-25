package com.onlinejudge.online_code_judge.security;

import com.onlinejudge.online_code_judge.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {

		String path = request.getServletPath();

		return path.startsWith("/api/users/register")
				|| path.startsWith("/api/auth");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		// If no token → skip authentication
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);

		if (jwtUtil.validateToken(token)) {

			String username = jwtUtil.extractUsername(token);
			String role = userRepository.findByUsername(username)
					.or(() -> userRepository.findByEmail(username))
					.map(user -> user.getRole())
					.orElseGet(() -> jwtUtil.extractRole(token));
			role = normalizeRole(role);

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					username,
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + role)));

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	private String normalizeRole(String role) {
		String resolvedRole = role == null ? "USER" : role.trim();
		if (resolvedRole.startsWith("ROLE_")) {
			resolvedRole = resolvedRole.substring(5);
		}
		if (resolvedRole.isBlank()) {
			return "USER";
		}
		return resolvedRole.toUpperCase(Locale.ROOT);
	}
}
