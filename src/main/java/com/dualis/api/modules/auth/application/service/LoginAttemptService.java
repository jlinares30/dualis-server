package com.dualis.api.modules.auth.application.service;

import com.dualis.api.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits failed login attempts per account to slow down password guessing.
 * In-memory: enough for the single Render instance; counters reset on restart.
 */
@Service
public class LoginAttemptService {

    private final int maxFailures;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(
            @Value("${app.login.max-failures:5}") int maxFailures,
            @Value("${app.login.window-minutes:15}") long windowMinutes) {
        this(maxFailures, Duration.ofMinutes(windowMinutes), Clock.systemUTC());
    }

    LoginAttemptService(int maxFailures, Duration window, Clock clock) {
        this.maxFailures = maxFailures;
        this.window = window;
        this.clock = clock;
    }

    /** Throws TooManyRequestsException when the account is temporarily blocked. */
    public void checkAllowed(String email) {
        Deque<Instant> attempts = failures.get(key(email));
        if (attempts == null) {
            return;
        }
        synchronized (attempts) {
            prune(attempts);
            if (attempts.size() >= maxFailures) {
                long retryAfter = Math.max(1, Duration.between(clock.instant(), attempts.peekFirst().plus(window)).getSeconds());
                throw new TooManyRequestsException(
                        "Too many failed login attempts. Try again in " + Math.max(1, retryAfter / 60) + " minute(s).",
                        retryAfter);
            }
        }
    }

    public void recordFailure(String email) {
        Deque<Instant> attempts = failures.computeIfAbsent(key(email), k -> new ArrayDeque<>());
        synchronized (attempts) {
            prune(attempts);
            attempts.addLast(clock.instant());
        }
    }

    public void recordSuccess(String email) {
        failures.remove(key(email));
    }

    private void prune(Deque<Instant> attempts) {
        Instant limit = clock.instant().minus(window);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(limit)) {
            attempts.pollFirst();
        }
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
