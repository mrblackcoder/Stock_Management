package com.ims.stockmanagement.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to track and limit login attempts to prevent brute force attacks.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 15;

    // Store: username -> LoginAttempt
    private final Map<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Record a failed login attempt.
     */
    public void loginFailed(String username) {
        LoginAttempt attempt = attemptsCache.getOrDefault(
            username,
            new LoginAttempt()
        );

        attempt.incrementAttempts();
        attempt.setLastAttemptTime(LocalDateTime.now());

        attemptsCache.put(username, attempt);
    }

    /**
     * Clear attempts after successful login.
     */
    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
    }

    /**
     * Check if user is blocked due to too many failed attempts.
     */
    public boolean isBlocked(String username) {
        LoginAttempt attempt = attemptsCache.get(username);

        if (attempt == null) {
            return false;
        }

        // Check if lock time has expired
        if (attempt.getLastAttemptTime().plusMinutes(LOCK_TIME_MINUTES)
            .isBefore(LocalDateTime.now())) {
            // Lock time expired, reset attempts
            attemptsCache.remove(username);
            return false;
        }

        return attempt.getAttempts() >= MAX_ATTEMPTS;
    }

    /**
     * Get remaining attempts before account lock.
     */
    public int getRemainingAttempts(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - attempt.getAttempts());
    }

    /**
     * Get time until account unlock (in minutes).
     */
    public long getUnlockTimeMinutes(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt == null) {
            return 0;
        }

        LocalDateTime unlockTime = attempt.getLastAttemptTime()
            .plusMinutes(LOCK_TIME_MINUTES);
        LocalDateTime now = LocalDateTime.now();

        if (unlockTime.isAfter(now)) {
            return java.time.Duration.between(now, unlockTime).toMinutes();
        }

        return 0;
    }

    /**
     * Inner class to track login attempts.
     */
    private static class LoginAttempt {
        private int attempts = 0;
        private LocalDateTime lastAttemptTime = LocalDateTime.now();

        public void incrementAttempts() {
            this.attempts++;
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLastAttemptTime() {
            return lastAttemptTime;
        }

        public void setLastAttemptTime(LocalDateTime lastAttemptTime) {
            this.lastAttemptTime = lastAttemptTime;
        }
    }
}
