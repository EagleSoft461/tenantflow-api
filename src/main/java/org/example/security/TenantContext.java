package org.example.security;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId){
        currentTenant.set(tenantId);
    }

    public static String getCurrentTenant(){
        return currentTenant.get();
    }

    // ⚠️ Bellek sızıntılarını (Memory Leak) önlemek için istek bittiğinde temizlik şarttır!
    public static void clear() {
        currentTenant.remove();
    }
}
