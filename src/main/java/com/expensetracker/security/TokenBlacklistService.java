package com.expensetracker.security;

import com.expensetracker.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisService redisService;

    public void blacklist(String token, long expirySeconds) {
        redisService.set(
                "jwt:blacklist:" + token,
                "true",
                Duration.ofSeconds(expirySeconds)
        );
    }

    public boolean isBlacklisted(String token) {
        return redisService.exists("jwt:blacklist:" + token);
    }
}
