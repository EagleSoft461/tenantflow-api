package org.example.config.multitenancy;

import org.example.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantSchemaResolver implements CurrentTenantIdentifierResolver<String>{

    private static  final String DEFAULT_SCHEMA = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getCurrentTenant();
        // Eğer Context boşsa veya belirtilmemişse varsayılan olarak public şemasına yönlendir
        if (tenantId == null || tenantId.isEmpty()) {
            return DEFAULT_SCHEMA;
    }
        // Şema isim çakışmalarını engellemek için ön ek ekliyoruz: schema_kuafor_mehmet
        return "schema_" + tenantId.replace("-", "_");
}

@Override
public boolean validateExistingCurrentSessions(){
        return true;
    }
}
