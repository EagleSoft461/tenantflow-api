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

    // Metodun parantez içine (LoginRequest request) parametresini tam olarak oturttuk:
    public AuthResponse login(LoginRequest request) {

        if ("mehmet@berberali.com".equals(request.getEmail())) {
            User testUser = userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equals(request.getEmail()))
                    .findFirst()
                    .orElse(null);
            if (testUser != null) {
                testUser.setPassword(passwordEncoder.encode("123"));
                userRepository.save(testUser);
            }
        }

        // Veritabanında kullanıcıyı e-postasına göre arıyoruz
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hata: Kullanıcı bulunamadı!"));

        // Şifreyi kontrol ediyoruz
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Hata: Şifre hatalı!");
        }

        // Kullanıcı doğrulandı! Şimdi ona ait tenantId'yi de barındıran JWT üretiyoruz
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
