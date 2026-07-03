package org.example.controller;

import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader; // Bunu ekledik
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<User> getUsers(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        // Eğer header verilmişse direkt o dükkanın verilerini filtrele
        return userService.getAllUsersByTenant(tenantId);
    }
}