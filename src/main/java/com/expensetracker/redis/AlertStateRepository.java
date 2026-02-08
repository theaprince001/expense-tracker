package com.expensetracker.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AlertStateRepository {

    private final StringRedisTemplate redisTemplate;

    private String key(Long userId, String periodKey) {
        return "alert_state:" + userId + ":" + periodKey;
    }

    public Integer getLastAlertLevel(Long userId, String periodKey) {
        String v = redisTemplate.opsForValue().get(key(userId, periodKey));
        return v == null ? null : Integer.parseInt(v);
    }

    public void saveAlertLevel(Long userId, String periodKey, int level) {
        redisTemplate.opsForValue().set(key(userId, periodKey), String.valueOf(level));
    }

    public void clear(Long userId, String periodKey) {
        redisTemplate.delete(key(userId, periodKey));
    }
}
