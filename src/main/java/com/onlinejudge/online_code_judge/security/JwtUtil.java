package com.onlinejudge.online_code_judge.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

@Component
public class JwtUtil {

	@Value("${jwt.secret:online-judge-secret-key-for-jwt-token-signing-2026-very-long}")
	private String jwtSecret;

	private final long EXPIRATION_TIME = 86400000; // 24 hours

	public String generateToken(String username, String role) {
		String normalizedRole = normalizeRole(role, "USER");

		return Jwts.builder()
				.setSubject(username)
				.addClaims(Map.of("role", normalizedRole))
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(getSigningKey())
				.compact();
	}

	public String extractUsername(String token) {

		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	public String extractRole(String token) {

		String role = Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.get("role", String.class);

		return normalizeRole(role, null);
	}

	public boolean validateToken(String token) {

		try {
			Jwts.parserBuilder()
					.setSigningKey(getSigningKey())
					.build()
					.parseClaimsJws(token);
			return true;

		} catch (JwtException e) {
			return false;
		}
	}

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	private String normalizeRole(String role, String fallbackRole) {
		String resolvedRole = role == null ? "" : role.trim();
		if (resolvedRole.startsWith("ROLE_")) {
			resolvedRole = resolvedRole.substring(5);
		}
		if (resolvedRole.isBlank()) {
			return fallbackRole;
		}
		return resolvedRole.toUpperCase(Locale.ROOT);
	}

}
