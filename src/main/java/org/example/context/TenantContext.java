package org.example.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    
    // Tenant ID format validation pattern (alphanumeric, hyphens, 3-50 chars)
    private static final String TENANT_ID_PATTERN = "^[a-z0-9][a-z0-9-]{1,48}[a-z0-9]$";

    public static void setCurrentTenant(String tenantId){
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.warn("Attempted to set null or empty tenant ID");
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        
        if (!tenantId.matches(TENANT_ID_PATTERN)) {
            log.error("Invalid tenant ID format attempted: {}", tenantId);
            throw new IllegalArgumentException("Invalid tenant ID format. Must be 3-50 characters, lowercase alphanumeric with hyphens");
        }
        
        currentTenant.set(tenantId);
        log.debug("Tenant context set: {}", tenantId);
    }

    public static String getCurrentTenant(){
        return currentTenant.get();
    }
    
    public static String getCurrentTenantOrThrow(){
        String tenantId = currentTenant.get();
        if (tenantId == null) {
            log.error("Tenant context not found in current thread");
            throw new IllegalStateException("Tenant context not available");
        }
        return tenantId;
    }

    public static void clear(){
        String tenantId = currentTenant.get();
        if (tenantId != null) {
            log.debug("Clearing tenant context: {}", tenantId);
        }
        currentTenant.remove();
    }
}
