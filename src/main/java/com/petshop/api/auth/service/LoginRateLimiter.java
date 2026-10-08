package com.petshop.api.auth.service;

import com.petshop.api.auth.exception.AuthExceptions.TooManyAttemptsException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttles password guessing: at most 5 failed logins per e-mail and 20 per IP address in any
 * 15-minute window. Successful logins do not count. In memory — fine for a single API instance;
 * move to the database or Redis if the API is ever scaled out (roadmap Phase 5).
 */
@Component
public class LoginRateLimiter {

    static final int MAX_FAILURES_PER_EMAIL = 5;
    static final int MAX_FAILURES_PER_IP = 20;
    static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Throws TooManyAttemptsException if this e-mail or IP is currently blocked. */
    public void checkAllowed(String email, String ip) {
        Instant now = clock.instant();
        long wait = Math.max(
                secondsUntilAllowed(emailKey(email), MAX_FAILURES_PER_EMAIL, now),
                secondsUntilAllowed(ipKey(ip), MAX_FAILURES_PER_IP, now));
        if (wait > 0) {
            throw new TooManyAttemptsException(wait);
        }
    }

    public void recordFailure(String email, String ip) {
        Instant now = clock.instant();
        add(emailKey(email), now);
        add(ipKey(ip), now);
        if (failures.size() > CLEANUP_THRESHOLD) {
            failures.entrySet().removeIf(e -> {
                synchronized (e.getValue()) {
                    prune(e.getValue(), now);
                    return e.getValue().isEmpty();
                }
            });
        }
    }

    /** Forgets all failures. For tests, which share one limiter across the Spring context. */
    public void reset() {
        failures.clear();
    }

    /** A successful login clears the e-mail's failures (the IP's stay, against spraying). */
    public void recordSuccess(String email) {
        failures.remove(emailKey(email));
    }

    private long secondsUntilAllowed(String key, int max, Instant now) {
        Deque<Instant> times = failures.get(key);
        if (times == null) {
            return 0;
        }
        synchronized (times) {
            prune(times, now);
            if (times.size() < max) {
                return 0;
            }
            // Blocked until enough old failures leave the window.
            Instant unblockAt = times.stream().skip(times.size() - max).findFirst().orElse(now).plus(WINDOW);
            return Math.max(1, Duration.between(now, unblockAt).toSeconds());
        }
    }

    private void add(String key, Instant now) {
        Deque<Instant> times = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (times) {
            prune(times, now);
            times.addLast(now);
        }
    }

    private static void prune(Deque<Instant> times, Instant now) {
        Instant cutoff = now.minus(WINDOW);
        while (!times.isEmpty() && !times.peekFirst().isAfter(cutoff)) {
            times.pollFirst();
        }
    }

    private static String emailKey(String email) {
        return "email:" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }

    private static String ipKey(String ip) {
        return "ip:" + (ip == null ? "unknown" : ip);
    }
}
