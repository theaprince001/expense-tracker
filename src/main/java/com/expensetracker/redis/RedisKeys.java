package com.expensetracker.redis;

public final class RedisKeys {

    private RedisKeys() {}

    public static String budgetAlertKey(Long userId, String period) {
        return "budget-alert:" + userId + ":" + period;
    }

    public static String websocketSessionKey(Long userId) {
        return "ws-session:" + userId;
    }

    public static String tokenBlacklistKey(String token) {
        return "jwt-blacklist:" + token;
    }
}
