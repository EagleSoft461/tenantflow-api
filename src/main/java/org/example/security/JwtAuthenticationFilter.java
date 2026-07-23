package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.context.TenantContext;
import org.example.service.JwtService;
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

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractEmail(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.isTokenValid(jwt, userEmail)) {
                    String extractedRole = jwtService.extractRole(jwt);
                    System.out.println(">>> Filtreye Yakalanan Kullanıcı: " + userEmail + " | Rolü: " + extractedRole);
                    // 1. Tenant ID'yi güvenli bir şekilde al (Master admin'de olmayabilir, patlamasını engelliyoruz)
                    try {
                        String tenantId = jwtService.extractTenantId(jwt);
                        if (tenantId != null) {
                            TenantContext.setCurrentTenant(tenantId);
                        }
                    } catch (Exception e) {
                        // Master token'larında tenantId olmayabilir, bu normaldir.
                    }

                    // 2. Rolü JwtService üzerinden tertemiz çekiyoruz
                    String role = null;
                    try {
                        role = jwtService.extractRole(jwt);
                    } catch (Exception e) {
                        System.out.println("Rol okuma hatası: " + e.getMessage());
                    }

                    if (role == null || role.trim().isEmpty()) {
                        role = "ROLE_STAFF";
                    }

                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role.toUpperCase();
                    }

                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                    // 3. Spring Security'ye yetkileri teslim et!
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail, null, authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.out.println("JWT Doğrulama Hatası: " + e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}