package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.context.TenantContext;
import org.example.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

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
                    // 1. Kiracı ID'sini bağlama yerleştir
                    String tenantId = jwtService.extractTenantId(jwt);
                    TenantContext.setCurrentTenant(tenantId);
                    String role = null;

                    try {
                        String[] chunks = jwt.split("\\.");
                        if (chunks.length > 1) {
                            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
                            if (payload.contains("\"role\":\"")) {
                                role = payload.split("\"role\":\"")[1].split("\"")[0];
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Payload okuma hatası: " + e.getMessage());
                    }

                    // Eğer rol bulunamadıysa patlamasın, varsayılan STAFF olsun
                    if (role == null || role.trim().isEmpty()) {
                        role = "ROLE_STAFF";
                    }

                    // Rol "ROLE_" ile başlamıyorsa Spring formatına uyduruyoruz
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role.toUpperCase();
                    }

                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                    // 3. Spring Security'yi rollerle besle!
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