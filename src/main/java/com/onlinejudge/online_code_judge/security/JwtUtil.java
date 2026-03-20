package com.onlinejudge.online_code_judge.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

	private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

	private final long EXPIRATION_TIME = 86400000; // 24 hours

	public String generateToken(String username, String string) {

		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SECRET_KEY)
				.compact();
	}

	public String extractUsername(String token) {

		return Jwts.parserBuilder()
				.setSigningKey(SECRET_KEY)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	public boolean validateToken(String token) {

		try {
			Jwts.parserBuilder()
					.setSigningKey(SECRET_KEY)
					.build()
					.parseClaimsJws(token);
			return true;

		} catch (JwtException e) {
			return false;
		}
	}

	public String generateToken1(String username, String role) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'generateToken'");
	}
}
