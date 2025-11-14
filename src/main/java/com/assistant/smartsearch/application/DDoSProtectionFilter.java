package com.assistant.smartsearch.application;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DDoSProtectionFilter extends OncePerRequestFilter {

    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long BLACKLIST_DURATION = 300000; // 5 minutes

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIP = getClientIP(request);

        // Check if IP is blacklisted
        if (isBlacklisted(clientIP)) {
            log.warn("Blocked request from blacklisted IP: {}", clientIP);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Blocked due to suspicious activity\"}");
            return;
        }

        // Track request count
        trackRequest(clientIP);

        // Continue with the request
        filterChain.doFilter(request, response);

        // Clean up old entries periodically
        cleanupOldEntries();
    }

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

    private boolean isBlacklisted(String clientIP) {
        Long blacklistTime = blacklist.get(clientIP);
        if (blacklistTime != null) {
            if (System.currentTimeMillis() - blacklistTime < BLACKLIST_DURATION) {
                return true;
            } else {
                blacklist.remove(clientIP);
            }
        }
        return false;
    }

    private void trackRequest(String clientIP) {
        requestCounts.putIfAbsent(clientIP, new AtomicInteger(0));
        int count = requestCounts.get(clientIP).incrementAndGet();

        if (count > MAX_REQUESTS_PER_MINUTE) {
            log.warn("IP {} exceeded request limit with {} requests, blacklisting", clientIP, count);
            blacklist.put(clientIP, System.currentTimeMillis());
        }
    }

    private void cleanupOldEntries() {
        // Reset counts every minute (when map grows too large)
        if (requestCounts.size() > 1000) {
            log.info("Cleaning up request counts cache");
            requestCounts.clear();
        }
    }
}