package com.enterprise.apimonetization.security;

import com.enterprise.apimonetization.dto.ApiKeyDetails;
import com.enterprise.apimonetization.service.ApiKeyService;
import com.enterprise.apimonetization.service.UsageTrackingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterFilter.class);

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private UsageTrackingService usageTrackingService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only apply to simulated gateway endpoints: /gw/api/{apiId}/...
        if (!path.startsWith("/gw/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Validate API Key
        String apiKeyVal = request.getHeader("X-API-KEY");
        if (apiKeyVal == null || apiKeyVal.trim().isEmpty()) {
            apiKeyVal = request.getParameter("apiKey");
        }
        if (apiKeyVal == null || apiKeyVal.trim().isEmpty()) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing API Key. Include 'X-API-KEY' header.");
            return;
        }

        ApiKeyDetails keyDetails;
        try {
            keyDetails = apiKeyService.validateKey(apiKeyVal);
        } catch (Exception e) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Unauthorized: " + e.getMessage());
            return;
        }

        // 2. Parse apiId from path: /gw/api/{apiId}/...
        Long apiId = parseApiIdFromPath(path);
        if (apiId == null) {
            writeErrorResponse(response, HttpStatus.BAD_REQUEST,
                    "Invalid gateway path. Use /gw/api/{apiId}/...");
            return;
        }

        // 3. Monthly Quota Check (DB fallback if Redis is down)
        try {
            if (usageTrackingService.isQuotaExceeded(keyDetails.getAppId(), apiId)) {
                writeErrorResponse(response, HttpStatus.FORBIDDEN,
                        "Monthly quota exceeded. Please upgrade your subscription tier.");
                return;
            }
        } catch (Exception e) {
            logger.warn("Quota check failed, allowing request through: {}", e.getMessage());
        }

        // 4. Redis Rate Limit (sliding window per second) — skipped if Redis unavailable
        boolean rateLimitExceeded = false;
        try {
            long currentSecond = Instant.now().getEpochSecond();
            String limitKey = "rate_limit:" + keyDetails.getAppId() + ":" + apiId + ":" + currentSecond;
            Long count = redisTemplate.opsForValue().increment(limitKey);
            if (count != null && count == 1) {
                redisTemplate.expire(limitKey, 2, TimeUnit.SECONDS);
            }
            if (count != null && count > keyDetails.getRateLimitPerSecond()) {
                rateLimitExceeded = true;
            }
        } catch (Exception e) {
            logger.warn("Redis unavailable for rate limiting, skipping rate limit check: {}", e.getMessage());
        }

        if (rateLimitExceeded) {
            writeErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS,
                    "Too Many Requests: Rate limit exceeded for plan " + keyDetails.getTier());
            return;
        }

        // 5. Execute and track timing
        long startTime = System.currentTimeMillis();
        int statusCode = HttpStatus.OK.value();
        try {
            filterChain.doFilter(request, response);
            statusCode = response.getStatus();
        } catch (Exception e) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            // Log usage asynchronously (Kafka or DB fallback)
            try {
                usageTrackingService.logUsage(keyDetails.getAppId(), apiId, path, duration, statusCode);
            } catch (Exception e) {
                logger.warn("Usage logging failed: {}", e.getMessage());
            }
        }
    }

    private Long parseApiIdFromPath(String path) {
        try {
            // Expected: /gw/api/{apiId}/...
            String[] segments = path.split("/");
            if (segments.length >= 4 && "api".equals(segments[2])) {
                return Long.parseLong(segments[3]);
            }
        } catch (NumberFormatException e) {
            logger.warn("Could not parse apiId from path: {}", path);
        }
        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                status.value(), status.getReasonPhrase(), message);
        response.getWriter().write(body);
    }
}
