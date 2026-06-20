package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF korumasını MVP aşamasında testleri kolaylaştırmak için kapatıyoruz
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Test endpoint'imize herkesin (şifresiz) erişmesine izin ver
                        .requestMatchers("/api/v1/test/**", "/api/v1/auth/**").permitAll()
                        // Geri kalan tüm istekler şifre istesin
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}