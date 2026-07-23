package org.example.controller;

import org.example.service.TenantProvisioningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/tenants")
public class TenantController {

    private final TenantProvisioningService provisioningService;

    // Constructor Injection
    public TenantController(TenantProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @PostMapping("/register/{tenantId}")
    public ResponseEntity<String> registerTenant(@PathVariable String tenantId) {
        // Servisimizi çağırıp otomatik işlemleri başlatıyoruz
        provisioningService.provisionTenant(tenantId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("✅ Tenant '" + tenantId + "' başarıyla oluşturuldu ve tabloları kuruldu!");
    }

    @GetMapping
    public ResponseEntity<List<String>> getAllTenants() {
        List<String> tenants = provisioningService.getAllTenants();
        return ResponseEntity.ok(tenants);
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<String> deleteTenant(@PathVariable String tenantId) {
        provisioningService.deleteTenant(tenantId);
        return ResponseEntity.ok("🗑️ Tenant '" + tenantId + "' ve şeması başarıyla silindi!");
    }
}
