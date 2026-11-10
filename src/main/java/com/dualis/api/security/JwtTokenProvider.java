package com.dualis.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationInMs;

    private static final int MIN_SECRET_BYTES = 32;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:7200000}") long expirationInMs) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is not configured. Set a random secret of at least "
                            + MIN_SECRET_BYTES + " bytes (e.g. `openssl rand -base64 48`).");
        }
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is too short: it must be at least " + MIN_SECRET_BYTES + " bytes.");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationInMs = expirationInMs;
    }

    /** Claim with the instant (epoch seconds) the user logged in; kept when the token is refreshed. */
    public static final String SESSION_START_CLAIM = "sst";

    public String generateToken(String email, String role) {
        return generateToken(email, role, new Date());
    }

    /** Issues a token for a session that started at {@code sessionStart} (used by token refresh). */
    public String generateToken(String email, String role, Date sessionStart) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim(SESSION_START_CLAIM, sessionStart.getTime() / 1000)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /** When the session of this (valid) token started: the sst claim, or the issue date for older tokens. */
    public Date getSessionStart(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Object sst = claims.get(SESSION_START_CLAIM);
        if (sst instanceof Number n) {
            return new Date(n.longValue() * 1000);
        }
        return claims.getIssuedAt() != null ? claims.getIssuedAt() : new Date();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
