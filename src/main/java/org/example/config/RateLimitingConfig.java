package org.example.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Implementation using Token Bucket Algorithm
 * Protects authentication endpoints from brute-force attacks
 */
@Component
public class RateLimitingConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingConfig.class);
    
    // In-memory cache for rate limit buckets per IP
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    // Login: 5 attempts per minute per IP
    private static final int LOGIN_CAPACITY = 5;
    private static final Duration LOGIN_REFILL_DURATION = Duration.ofMinutes(1);
    
    // Register: 3 attempts per hour per IP
    private static final int REGISTER_CAPACITY = 3;
    private static final Duration REGISTER_REFILL_DURATION = Duration.ofHours(1);

    /**
     * Resolves bucket for login endpoint
     */
    public Bucket resolveLoginBucket(String key) {
        return cache.computeIfAbsent(key, k -> createLoginBucket());
    }

    /**
     * Resolves bucket for register endpoint
     */
    public Bucket resolveRegisterBucket(String key) {
        return cache.computeIfAbsent(key, k -> createRegisterBucket());
    }

    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(LOGIN_CAPACITY, 
            Refill.intervally(LOGIN_CAPACITY, LOGIN_REFILL_DURATION));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private Bucket createRegisterBucket() {
        Bandwidth limit = Bandwidth.classic(REGISTER_CAPACITY, 
            Refill.intervally(REGISTER_CAPACITY, REGISTER_REFILL_DURATION));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Extracts client identifier (IP address) from request
     */
    public String getClientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Clears rate limit cache (for testing or maintenance)
     */
    public void clearCache() {
        cache.clear();
        log.info("Rate limit cache cleared");
    }
}
