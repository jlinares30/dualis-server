package com.dualis.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String VALID_SECRET = "test-secret-that-is-long-enough-for-hs256-0123456789";

    @Test
    @DisplayName("Fails fast when the JWT secret is not configured")
    void missingSecret_FailsFast() {
        assertThatThrownBy(() -> new JwtTokenProvider("", 3600000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_JWT_SECRET");
    }

    @Test
    @DisplayName("Fails fast when the JWT secret is too short")
    void shortSecret_FailsFast() {
        assertThatThrownBy(() -> new JwtTokenProvider("short", 3600000))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tokens signed with another secret are rejected")
    void tokenFromOtherSecret_IsRejected() {
        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, 3600000);
        JwtTokenProvider attacker = new JwtTokenProvider(VALID_SECRET + "-attacker", 3600000);

        String legit = provider.generateToken("jorge@example.com", "ROLE_USER");
        String forged = attacker.generateToken("jorge@example.com", "ROLE_USER");

        assertThat(provider.validateToken(legit)).isTrue();
        assertThat(provider.getEmailFromToken(legit)).isEqualTo("jorge@example.com");
        assertThat(provider.validateToken(forged)).isFalse();
    }
}
