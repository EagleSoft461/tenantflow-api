package org.example.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = httpRequest.getHeader(TENANT_HEADER);

        if (tenantId != null && !tenantId.trim().isEmpty()) {
            // Header bulunduysa context'e mühürlüyoruz
            TenantContext.setCurrentTenant(tenantId);
        } else {
            // Şimdilik test süreçlerini baltalamamak için log basalım, fırlatmayalım
            System.out.println("Warning: X-Tenant-ID header not found in request: " + httpRequest.getRequestURI());
        }

        try {
            // İsteğin yoluna devam etmesini sağlıyoruz
            chain.doFilter(request, response);
        } finally {
            // İstek bittiği an ThreadLocal alanını temizliyoruz!!!
            TenantContext.clear();
        }
    }

}
