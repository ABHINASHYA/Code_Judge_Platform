package com.onlinejudge.online_code_judge.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

@Component
public class JwtUtil {

	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
	private static final int MIN_KEY_BYTES = 32;
	private static final long DEFAULT_EXPIRATION_MS = 86_400_000L;

	@Value("${jwt.secret:online-judge-secret-key-for-jwt-token-signing-2026-very-long}")
	private String jwtSecret;

	@Value("${jwt.expiration-ms:86400000}")
	private long expirationMs;

	private SecretKey signingKey;

	@PostConstruct
	public void init() {
		this.signingKey = buildSigningKey(jwtSecret);

		if (expirationMs <= 0) {
			log.warn("Invalid jwt.expiration-ms='{}'. Falling back to {} ms", expirationMs, DEFAULT_EXPIRATION_MS);
			expirationMs = DEFAULT_EXPIRATION_MS;
		}

		log.info("JWT initialized with HS256 key ({} bytes), expiration={} ms", signingKey.getEncoded().length, expirationMs);
	}

	public String generateToken(String username, String role) {
		String normalizedRole = normalizeRole(role, "USER");

		return Jwts.builder()
				.setSubject(username)
				.addClaims(Map.of("role", normalizedRole))
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expirationMs))
				.signWith(signingKey)
				.compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public String extractRole(String token) {
		String role = parseClaims(token).get("role", String.class);
		return normalizeRole(role, "USER");
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			log.debug("Invalid JWT token: {}", ex.getMessage());
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(signingKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private SecretKey buildSigningKey(String secret) {
		String resolvedSecret = secret == null ? "" : secret.trim();
		if (resolvedSecret.isBlank()) {
			throw new IllegalStateException("JWT secret must not be blank");
		}

		byte[] keyBytes = decodeIfBase64(resolvedSecret);
		if (keyBytes.length < MIN_KEY_BYTES) {
			log.warn(
					"JWT secret is too short ({} bytes). Deriving a 256-bit key using SHA-256. Set JWT_SECRET to at least 32 raw bytes (or base64 equivalent).",
					keyBytes.length);
			keyBytes = sha256(keyBytes);
		}

		return Keys.hmacShaKeyFor(keyBytes);
	}

	private byte[] decodeIfBase64(String value) {
		try {
			byte[] decoded = Base64.getDecoder().decode(value);
			if (decoded.length >= MIN_KEY_BYTES) {
				return decoded;
			}
		} catch (IllegalArgumentException ignored) {
			// Not base64; fall back to raw text bytes.
		}
		return value.getBytes(StandardCharsets.UTF_8);
	}

	private byte[] sha256(byte[] input) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(input);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
		}
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
