package org.example.filter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.config.RateLimitingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limiting Filter for Authentication Endpoints
 * Applies token bucket algorithm to prevent brute-force attacks
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    @Autowired
    private RateLimitingConfig rateLimitingConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String clientKey = rateLimitingConfig.getClientKey(request);

        // Apply rate limiting only to authentication endpoints
        if (path.equals("/api/v1/auth/login")) {
            if (!checkRateLimit(rateLimitingConfig.resolveLoginBucket(clientKey), response, clientKey, "login")) {
                return;
            }
        } else if (path.equals("/api/v1/auth/register")) {
            if (!checkRateLimit(rateLimitingConfig.resolveRegisterBucket(clientKey), response, clientKey, "register")) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkRateLimit(Bucket bucket, HttpServletResponse response, 
                                   String clientKey, String endpoint) throws IOException {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            
            log.warn("Rate limit exceeded for client: {} on endpoint: {} - Retry after: {}s", 
                     clientKey, endpoint, waitForRefill);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\": \"Too many requests\", \"message\": \"Rate limit exceeded. Try again in %d seconds\", \"retryAfter\": %d}",
                waitForRefill, waitForRefill
            ));
            return false;
        }
    }
}
