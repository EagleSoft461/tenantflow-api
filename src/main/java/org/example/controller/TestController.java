package org.example.controller;

import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // 🔥 Bu import önemli!
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private UserService userService;

    // SADECE ADMIN GÖREBİLİR: Dükkan sahiplerine özel endpoint
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUsersForAdmin(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        return userService.getAllUsersByTenant(tenantId);
    }

    //STAFF VE ÜSTÜ GÖREBİLİR: Personelin de erişebileceği endpoint
    @GetMapping("/staff/users")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public List<User> getUsersForStaff(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        return userService.getAllUsersByTenant(tenantId);
    }
}