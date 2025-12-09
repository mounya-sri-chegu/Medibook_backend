package com.medibook.medibook_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "bW9ueWFfcmFuZG9tX3NlY3JldF9rZXlfbWVkaWJvb2tfYXBwXzIwMjU=";

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token with userId as subject and role as claim
     */
    public String generateToken(Long userId, String role) {
        Map<String, Object> claims = Map.of("role", role);
        long now = System.currentTimeMillis();
        long expiryMillis = now + 86400000L; // 24 hours

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryMillis))
                .signWith(this.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract userId from token
     */
    public Long extractUserId(String token) {
        String subject = this.extractClaim(token, Claims::getSubject);
        return Long.valueOf(subject);
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return this.extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Validate token expiration
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}
