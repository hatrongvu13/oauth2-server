package com.htv.oauth2.util;

import java.util.Set;

public class StringUtil {

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Convert set to space-separated string
     */
    public static String joinScopes(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return "";
        }
        return String.join(" ", scopes);
    }

    /**
     * Convert space-separated string to set
     */
    public static Set<String> splitScopes(String scopes) {
        if (isEmpty(scopes)) {
            return Set.of();
        }
        return Set.of(scopes.trim().split("\\s+"));
    }

    /**
     * Mask sensitive data (for logging)
     */
    public static String maskSensitive(String data) {
        if (isEmpty(data)) {
            return "***";
        }
        if (data.length() <= 8) {
            return "***";
        }
        return data.substring(0, 4) + "***" + data.substring(data.length() - 4);
    }

    /**
     * Truncate string to max length
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
