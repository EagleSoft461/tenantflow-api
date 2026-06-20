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

    // Metodun parantez içine (LoginRequest request) parametresini tam olarak oturttuk:
    public AuthResponse login(LoginRequest request) {

        // Veritabanında kullanıcıyı e-postasına göre arıyoruz
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hata: Kullanıcı bulunamadı!"));

        // Şifreyi kontrol ediyoruz
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Hata: Şifre hatalı!");
        }

        // Kullanıcı doğrulandı! Şimdi ona ait tenantId'yi de barındıran JWT üretiyoruz
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
