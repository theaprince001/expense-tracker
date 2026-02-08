package com.expensetracker.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisService redisService;

    public boolean allowRequest(String key, int limit, Duration window) {
        String redisKey = "rate:" + key;
        try {
            String count = redisService.get(redisKey);

            if (count == null) {
                redisService.set(redisKey, "1", window);
                return true;
            }

            int current = Integer.parseInt(count);
            if (current >= limit) {
                return false;
            }

            redisService.set(redisKey, String.valueOf(current + 1), window);
            return true;

        } catch (Exception e) {
            return true;
        }
    }
}
