package org.example.service;

import org.example.dto.LoginRequest;
import org.example.dto.AuthResponse;
import org.example.dto.UserRegistrationRequestDTO;
import org.example.dto.UserResponseDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
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
    
    @Autowired
    private UserService userService;

    // 1. LOGIN ENDPOINT'İ
    public AuthResponse login(LoginRequest request) {
        // Veritabanında kullanıcıyı e-postasına göre arıyoruz
        User user = userRepository.findByEmail(request.getEmail())
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
    public AuthResponse register(UserRegistrationRequestDTO requestDTO) {
        // Kullanıcı e-postası zaten var mı kontrolü
        boolean exists = userRepository.findByEmail(requestDTO.getEmail()).isPresent();

        if (exists) {
            throw new RuntimeException("Hata: Bu e-posta adresi zaten kullanımda!");
        }

        // Şifreyi veritabanına gitmeden önce BCrypt ile mühürlüyoruz
        requestDTO.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

        UserResponseDTO savedUserDto = userService.saveUser(requestDTO);
        
        // Return token. We need User entity for generateToken, so let's fetch it or create a temporary one.
        // Or we can just get it from the repository to ensure we have the full entity.
        User userForToken = userRepository.findById(savedUserDto.getId())
                .orElseThrow(() -> new RuntimeException("Hata: Kaydedilen kullanıcı bulunamadı!"));

        String token = jwtService.generateToken(userForToken);
        return new AuthResponse(token);
    }
}