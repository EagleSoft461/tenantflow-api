package org.example.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.context.TenantContext;
import org.example.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractEmail(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.isTokenValid(jwt, userEmail)) {
                    
                    // 1. Tenant ID'yi güvenli bir şekilde al ve context'e yerleştir
                    try {
                        String tenantId = jwtService.extractTenantId(jwt);
                        if (tenantId != null && !tenantId.trim().isEmpty()) {
                            TenantContext.setCurrentTenant(tenantId);
                            log.debug("Tenant context set for user: {}", userEmail);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid tenant ID in JWT for user: {} - {}", userEmail, e.getMessage());
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\": \"Invalid tenant ID format\"}");
                        return;
                    } catch (Exception e) {
                        log.debug("No tenant ID in token for user: {} (might be master admin)", userEmail);
                    }

                    // 2. Rolü JwtService üzerinden güvenli çek
                    String role = null;
                    try {
                        role = jwtService.extractRole(jwt);
                    } catch (Exception e) {
                        log.warn("Failed to extract role from JWT for user: {} - {}", userEmail, e.getMessage());
                    }

                    // 3. Rol fallback ve standardizasyon
                    if (role == null || role.trim().isEmpty()) {
                        role = "ROLE_STAFF";
                        log.warn("No role found in JWT for user: {}, defaulting to ROLE_STAFF", userEmail);
                    }

                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role.toUpperCase();
                    }

                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                    // 4. Spring Security'ye yetkileri teslim et
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail, null, authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Authentication successful for user: {} with role: {}", userEmail, role);
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token attempt from: {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token expired\"}");
            return;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token attempt from: {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid token format\"}");
            return;
        } catch (SignatureException e) {
            log.error("JWT signature verification failed from: {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid token signature\"}");
            return;
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Authentication failed\"}");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}