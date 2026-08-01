package org.example.controller;

import org.example.context.TenantContext;
import org.example.entity.User;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private UserService userService;

    // SADECE ADMIN GÖREBİLİR: Dükkan sahiplerine özel endpoint
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUsersForAdmin() {
        // 🔒 GÜVENLIK: JWT'den gelen tenant ID kullanılıyor, header'a güvenilmiyor!
        String tenantId = TenantContext.getCurrentTenantOrThrow();
        log.info("Admin user list requested for tenant: {}", tenantId);
        return userService.getAllUsersByTenant(tenantId);
    }

    // STAFF VE ÜSTÜ GÖREBİLİR: Personelin de erişebileceği endpoint
    @GetMapping("/staff/users")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public List<User> getUsersForStaff() {
        // 🔒 GÜVENLIK: JWT'den gelen tenant ID kullanılıyor, header'a güvenilmiyor!
        String tenantId = TenantContext.getCurrentTenantOrThrow();
        log.info("Staff user list requested for tenant: {}", tenantId);
        return userService.getAllUsersByTenant(tenantId);
    }
}