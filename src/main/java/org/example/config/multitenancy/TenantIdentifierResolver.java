package org.example.config.multitenancy;

import org.example.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    // Varsayılan tenant/şema adı
    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        // TenantContext'ten o anki tenant bilgisini çekiyoruz
        String currentTenant = TenantContext.getCurrentTenant();
        return (currentTenant != null && !currentTenant.isEmpty()) ? currentTenant : DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}