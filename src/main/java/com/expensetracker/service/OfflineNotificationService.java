package com.expensetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfflineNotificationService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration TTL = Duration.ofHours(24);

    public void save(String email, String message) {
        String key = "offline_notifications:" + email;
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, TTL);
        log.info("Stored offline notification for {}", email);
    }

    public List<String> fetchAndClear(String email) {
        String key = "offline_notifications:" + email;
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
        redisTemplate.delete(key);
        return messages;
    }
}
