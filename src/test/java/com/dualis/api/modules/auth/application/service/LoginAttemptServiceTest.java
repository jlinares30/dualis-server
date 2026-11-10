package com.dualis.api.modules.auth.application.service;

import com.dualis.api.exception.TooManyRequestsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    @Test
    @DisplayName("Blocks an account after 5 failures and unblocks it after the window")
    void blocksAndUnblocks() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-10T12:00:00Z"));
        Clock clock = new Clock() {
            public ZoneOffset getZone() { return ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId zone) { return this; }
            public Instant instant() { return now.get(); }
        };
        LoginAttemptService service = new LoginAttemptService(5, Duration.ofMinutes(15), clock);

        for (int i = 0; i < 4; i++) service.recordFailure("Jorge@Example.com");
        assertThatCode(() -> service.checkAllowed("jorge@example.com")).doesNotThrowAnyException();

        service.recordFailure("jorge@example.com");
        assertThatThrownBy(() -> service.checkAllowed("jorge@example.com")).isInstanceOf(TooManyRequestsException.class);
        assertThatCode(() -> service.checkAllowed("maria@example.com")).doesNotThrowAnyException();

        now.set(now.get().plus(Duration.ofMinutes(16)));
        assertThatCode(() -> service.checkAllowed("jorge@example.com")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A successful login clears previous failures")
    void successResets() {
        LoginAttemptService service = new LoginAttemptService(2, Duration.ofMinutes(15), Clock.systemUTC());
        service.recordFailure("jorge@example.com");
        service.recordSuccess("jorge@example.com");
        service.recordFailure("jorge@example.com");

        assertThatCode(() -> service.checkAllowed("jorge@example.com")).doesNotThrowAnyException();
    }
}
