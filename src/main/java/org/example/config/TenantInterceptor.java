package org.example.config;

import org.example.context.TenantContext;
import org.example.service.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtService jwtService; // Token'ı çözmek için JwtService enjekte edildi

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");
        String tenantId = null;

        // Eğer istekte mühürlü bir Bearer Token varsa direkt onun içinden oku (En Güvenli Yol!)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                tenantId = jwtService.extractTenantId(token);
            } catch (Exception e) {
                System.out.println("Error parsing JWT token for tenant isolation: " + e.getMessage());
            }
        }

        // Eğer token yoksa (register/login gibi halka açık yerler için) manuel header'a bakmaya devam etsin
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = request.getHeader("X-Tenant-ID");
        }

        // Elimizde geçerli bir kiracı bilgisi varsa, veritabanına mühür bas!
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setCurrentTenant(tenantId);

            // Hibernate'e sızıp o anki SQL sorgularına gizlice "WHERE tenant_id = ..." ekletiyoruz
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        } else {
            System.out.println("Warning: Secure Tenant context could not be established for URI: " + request.getRequestURI());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // İstek bittiği an ThreadLocal hafızasını boşaltıp bellek sızıntılarını (Memory Leak) önlüyoruz
        TenantContext.clear();
    }
}