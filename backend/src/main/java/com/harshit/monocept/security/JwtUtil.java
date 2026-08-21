package com.harshit.monocept.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expiration}")
	private long jwtExpiration;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}

	public String generateToken(String email, String role) {
		return Jwts.builder().setSubject(email).claim("role", role).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	public String getEmailFromToken(String token) {
		return Jwts.parser().verifyWith((javax.crypto.SecretKey) getSigningKey()).build().parseSignedClaims(token)
				.getPayload().getSubject();
	}
	
	public io.jsonwebtoken.Claims getClaims(String token) {
		return Jwts.parser().verifyWith((javax.crypto.SecretKey) getSigningKey()).build().parseSignedClaims(token)
				.getPayload();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith((javax.crypto.SecretKey) getSigningKey()).build().parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public long getRemainingExpiry(String token) {
		Date expiration = Jwts.parser().verifyWith((javax.crypto.SecretKey) getSigningKey()).build()
				.parseSignedClaims(token).getPayload().getExpiration();
		return expiration.getTime() - System.currentTimeMillis();
	}

	public long getExpirationTime() {
		return jwtExpiration;
	}
}