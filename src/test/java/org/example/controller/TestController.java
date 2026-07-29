package org.example.controller;

import org.example.exception.DuplicateResourceException;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.UnauthorizedTenantAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/not-found")
    public void throwNotFound() {
        throw new ResourceNotFoundException("Aranan kaynak bulunamadı");
    }

    @GetMapping("/duplicate")
    public void throwDuplicate() {
        throw new DuplicateResourceException("Bu kayıt zaten mevcut");
    }

    @GetMapping("/unauthorized")
    public void throwUnauthorized() {
        throw new UnauthorizedTenantAccessException("Bu kiracıya erişim izniniz yok");
    }
}