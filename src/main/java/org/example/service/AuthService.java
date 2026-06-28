package org.example.service;

import org.example.dto.LoginRequest;
import org.example.dto.AuthResponse;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. LOGIN ENDPOINT'İ
    public AuthResponse login(LoginRequest request) {
        // Veritabanında kullanıcıyı e-postasına göre arıyoruz
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hata: Kullanıcı bulunamadı!"));

        // Şifreyi kontrol ediyoruz
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Hata: Şifre hatalı!");
        }

        // Kullanıcı doğrulandı, JWT token üretiliyor
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    // 2. REGISTER ENDPOINT'İ
    public AuthResponse register(User user) {
        // Kullanıcı e-postası zaten var mı kontrolü
        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));

        if (exists) {
            throw new RuntimeException("Hata: Bu e-posta adresi zaten kullanımda!");
        }

        // Şifreyi veritabanına gitmeden önce BCrypt ile mühürlüyoruz
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);
        return new AuthResponse(token);
    }
}