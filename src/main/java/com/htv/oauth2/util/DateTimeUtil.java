package com.htv.oauth2.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class DateTimeUtil {

    /**
     * Check if instant is expired
     */
    public static boolean isExpired(Instant instant) {
        return instant != null && instant.isBefore(Instant.now());
    }

    /**
     * Check if instant is valid (not expired)
     */
    public static boolean isValid(Instant instant) {
        return instant != null && instant.isAfter(Instant.now());
    }

    /**
     * Calculate expiration time
     */
    public static Instant expiresAt(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }

    /**
     * Calculate time until expiration
     */
    public static long secondsUntilExpiration(Instant expiresAt) {
        if (expiresAt == null) {
            return 0;
        }
        long seconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Math.max(0, seconds);
    }

    /**
     * Format duration in human-readable format
     */
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
