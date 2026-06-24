package org.example.controller;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public List<User> getUsers() {
        // HİLELİ DOKUNUŞ: Mehmet'i bul ve şifresini orijinal Java BCrypt ile ez!
        userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("mehmet@berberali.com"))
                .findFirst()
                .ifPresent(user -> {
                    user.setPassword(passwordEncoder.encode("123"));
                    userRepository.save(user);
                });

        return userRepository.findAll();
    }
}