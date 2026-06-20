package org.example.config;

import org.example.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // İstekten X-Tenant-ID header'ını oku
        String tenantId = request.getHeader(TENANT_HEADER);

        // Eğer istekte tenant_id varsa Context'e set et
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setCurrentTenant(tenantId);
        } else {
            // MVP aşamasında testi kolaylaştırmak için header yoksa hata fırlatabilir veya boş geçebiliriz.
            // Şimdilik isteklerin kırılmaması için loglayıp geçelim, ileride zorunlu yapacağız.
            System.out.println("Warning: X-Tenant-ID header not found in request!");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // İstek bittiğinde (Response döndüğünde) ThreadLocal'i mutlaka temizlemeliyiz.
        // Aksi takdirde Memory Leak (bellek sızıntısı) oluşur.
        TenantContext.clear();
    }
}
