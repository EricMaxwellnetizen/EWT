package com.htc.enter.util;

import com.htc.enter.model.User;

public final class DocumentPasswordUtil {

    private static final String PASSWORD_SUFFIX = "123";
    private static final String DEFAULT_PASSWORD = "default123";

    private DocumentPasswordUtil() {
    }

    public static String resolvePassword(User primary, User fallback) {
        String username = extractUsername(primary);
        if (username == null) {
            username = extractUsername(fallback);
        }
        if (username == null) {
            return DEFAULT_PASSWORD;
        }
        return username + PASSWORD_SUFFIX;
    }

    private static String extractUsername(User user) {
        if (user == null) {
            return null;
        }
        String username = user.getUsername();
        if (username == null || username.isBlank()) {
            return null;
        }
        return username;
    }
}
