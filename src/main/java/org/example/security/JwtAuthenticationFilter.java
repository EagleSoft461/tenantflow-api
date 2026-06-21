package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.context.TenantContext;
import org.example.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. İstekteki "Authorization" başlığını (Header) oku
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Eğer token yoksa veya "Bearer " ile başlamıyorsa filtreyi geç, dokunma
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. "Bearer " kısmını sıyırıp sadece saf şifreli token'ı al
        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractEmail(jwt);

            // 4. Eğer email başarıyla çözüldüyse ve Spring Security henüz bu isteği onaylamadıysa
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.isTokenValid(jwt, userEmail)) {
                    // EN KRİTİK YER: Token'ın içinden tenantId'yi sök ve ThreadLocal kutusuna bas!
                    String tenantId = jwtService.extractTenantId(jwt);
                    TenantContext.setCurrentTenant(tenantId);

                    // Spring Security'ye "Bu kullanıcı güvenlidir, içeri alabilirsin" raporu veriyoruz
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail, null, Collections.emptyList()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token sahteyse veya süresi dolduysa sistemi patlatma, hata logu bas
            System.out.println("JWT Doğrulama Hatası: " + e.getMessage());
        }

        try {
            // 5. İstek yoluna devam etsin (Controller'a ulaşsın)
            filterChain.doFilter(request, response);
        } finally {
            // 6. İstek bittiğinde (Response dönerken) ThreadLocal havuzunu temizle (Bellek sızıntısını önler)
            TenantContext.clear();
        }
    }
}
