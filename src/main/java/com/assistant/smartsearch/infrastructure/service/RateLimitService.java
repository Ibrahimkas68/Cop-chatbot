package com.assistant.smartsearch.infrastructure.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimiterRegistry rateLimiterRegistry;

    /**
     * Check if the request is allowed based on rate limiting rules
     * @param request The HTTP request
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean allowRequest(HttpServletRequest request) {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("default");
        // acquirePermission() returns true if a permit was obtained within the configured timeout
        boolean permitted = rateLimiter.acquirePermission();
        if (!permitted) {
            log.warn("Rate limit exceeded for client IP: {}", getClientIP(request));
        }
        return permitted;
    }

    /**
     * Generate a unique identifier for the client based on IP and User-Agent
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String ip = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String identifier = ip + ":" + (userAgent != null ? userAgent.hashCode() : "unknown");
        // Sanitize for rate limiter name (only alphanumeric and specific chars)
        return identifier.replaceAll("[^a-zA-Z0-9:-]", "");
    }

    /**
     * Extract the real client IP address from the request
     */
    private String getClientIP(HttpServletRequest request) {
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

        // Handle multiple IPs (take first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}