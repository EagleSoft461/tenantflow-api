package org.example.config;

import org.example.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API geliştirdiğimiz için CSRF'i kapatıyoruz
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Session tutma, her şey stateless token üzerinden yürüyecek
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll() // Login endpoint'ine herkes şifresiz girebilsin
                        .anyRequest().authenticated() // Diğer tüm endpoint'ler (Test dahil) artık token istiyor!
                )
                // Bizim yazdığımız akıllı filtreyi, standart kullanıcı adı/şifre filtresinden hemen önceye koyuyoruz:
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}