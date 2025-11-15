package com.assistant.smartsearch.application;

import com.assistant.smartsearch.infrastructure.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DDoSProtectionFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIP = rateLimitService.getClientIP(request);

        // Record the request in a rolling counter and blacklist if threshold exceeded
        rateLimitService.recordRequestAndMaybeBlacklist(clientIP);

        // 1) Blacklist check → 403
        if (rateLimitService.isBlacklisted(clientIP)) {
            log.warn("Client {} is blacklisted. Blocking request with 403.", clientIP);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Client blacklisted due to excessive requests\"}");
            return;
        }

        // 2) Token bucket check → 429
        boolean permitted = rateLimitService.consumeToken(clientIP);
        if (!permitted) {
            log.warn("Rate limit exceeded for client IP: {}", clientIP);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests\"}");
            return;
        }

        // 3) Continue filter chain
        filterChain.doFilter(request, response);
    }
}