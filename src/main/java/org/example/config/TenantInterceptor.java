package org.example.config;

import org.example.context.TenantContext;
import org.example.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService; // Token'ı güvenli şekilde çözmek için kalıyor

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");
        String tenantId = null;

        // 1. JWT Token varsa kiracı kimliğini güvenli kaynaktan alıyoruz
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                tenantId = jwtService.extractTenantId(token);
            } catch (Exception e) {
                System.out.println("Error parsing JWT token for tenant isolation: " + e.getMessage());
            }
        }

        // 2. Token yoksa (Register/Login gibi) header kontrolü devrede
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = request.getHeader("X-Tenant-ID");
        }

        // v0.7.0 Dynamic Schema Routing:
        // Eski satır bazlı filtreleme kodlarını (Session, enableFilter) tamamen sildik!
        // Sadece Context'e kiracı adını veriyoruz. Yazdığın Resolver ve Provider bu ismi
        // yakalayıp veritabanında "SET search_path TO schema_..." komutunu yürütecek.
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            // Şema isimlendirmene göre burayı "schema_" + tenantId yapabilirsin
            TenantContext.setCurrentTenant("schema_" + tenantId);
        } else {
            TenantContext.setCurrentTenant("public");
        }

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) throws Exception {
        // İstek bittiği an bellek sızıntısını önlemek için ThreadLocal'i temizliyoruz
        TenantContext.clear();
    }
}