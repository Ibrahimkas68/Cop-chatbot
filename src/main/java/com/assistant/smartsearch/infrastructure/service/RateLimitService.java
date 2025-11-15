package com.assistant.smartsearch.infrastructure.service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String KEY_BUCKET_PREFIX = "rl:bucket:";
    private static final String KEY_COUNT_PREFIX = "rl:count:";
    private static final String KEY_BLACKLIST_PREFIX = "rl:blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${rate.limit.capacity:15}")
    private int capacity;

    @Value("${rate.limit.refillRate:15}")
    private int refillRate;

    @Value("${rate.limit.refillIntervalSeconds:60}")
    private int refillIntervalSeconds;

    @Value("${rate.limit.blacklist.threshold:100}")
    private int blacklistThreshold;

    @Value("${rate.limit.blacklist.ttlSeconds:300}")
    private int blacklistTtlSeconds;

    @Value("${rate.limit.counter.windowSeconds:60}")
    private int counterWindowSeconds;

    private final DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();

    @PostConstruct
    public void init() {
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limiter.lua")));
        redisScript.setResultType(Long.class);
    }

    // Public API used by the filter
    public boolean isBlacklisted(String clientIP) {
        try {
            String key = KEY_BLACKLIST_PREFIX + clientIP;
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Redis error while checking blacklist for {}: {}", clientIP, e.getMessage());
            // Fail-open for blacklist check: if Redis fails, do not block
            return false;
        }
    }

    public void recordRequestAndMaybeBlacklist(String clientIP) {
        try {
            String counterKey = KEY_COUNT_PREFIX + clientIP;
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            Long count = ops.increment(counterKey);
            if (count != null && count == 1L) {
                // set TTL on first increment
                redisTemplate.expire(counterKey, Duration.ofSeconds(counterWindowSeconds));
            }
            if (count != null && count >= blacklistThreshold) {
                String blKey = KEY_BLACKLIST_PREFIX + clientIP;
                ops.set(blKey, "1", Duration.ofSeconds(blacklistTtlSeconds));
                log.warn("Client {} has been blacklisted for {} seconds (count in window: {})",
                        clientIP, blacklistTtlSeconds, count);
            }
        } catch (Exception e) {
            log.error("Redis error while recording request for {}: {}", clientIP, e.getMessage());
        }
    }

    public boolean consumeToken(String clientIP) {
        try {
            Long tokens = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(KEY_BUCKET_PREFIX + clientIP),
                    String.valueOf(capacity),
                    String.valueOf(refillRate),
                    String.valueOf(refillIntervalSeconds)
            );
            return tokens != null && tokens > 0;
        } catch (Exception e) {
            // In case of Redis failure, fail-open to avoid taking down the service
            log.error("Redis error while consuming token for {}: {}", clientIP, e.getMessage());
            return true;
        }
    }

    // Convenience method retained for backward compatibility
    public boolean allowRequest(HttpServletRequest request) {
        String clientIP = getClientIP(request);
        if (isBlacklisted(clientIP)) {
            return false; // The filter should decide status code; here just block
        }
        boolean permitted = consumeToken(clientIP);
        if (!permitted) {
            log.warn("Rate limit exceeded for client IP: {}", clientIP);
        }
        return permitted;
    }

    public String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}