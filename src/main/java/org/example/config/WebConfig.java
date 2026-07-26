package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // API'deki tüm uç noktaları kapsar
                        // Geliştirme ortamı için izin verilen adresler (Frontend)
                        .allowedOrigins("http://localhost:3000", "http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // İzin verilen HTTP metodları
                        .allowedHeaders("*") // Tüm başlıklara (Authorization, Content-Type vs.) izin ver
                        .allowCredentials(true) // Çerezlerin ve JWT token'ların geçişine izin ver
                        .maxAge(3600); // Tarayıcının bu CORS kurallarını 1 saat (3600 saniye) önbellekte tutmasını sağla
            }
        };
    }
}