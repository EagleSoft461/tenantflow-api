package org.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.entity.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String SECRET_KEY = "bu-bizim-cok-gizli-ve-guvenli-jwt-anahtarimiz-1234567890";

    // 2. Metot adını ve içindeki değişken uyuşmazlığını (keyBytes) düzelttik
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Kullanıcı giriş yaptığında ona özel 24 saatlik token üretir
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        // En kritik yer: Kiracı (Tenant) bilgisini token payload'una gömüyoruz!
        extraClaims.put("tenantId", user.getTenantId());
        extraClaims.put("role", user.getRole());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Saatlik Ömür
                .signWith(getSigningKey())
                .compact();
    }

    // Token'ın içinden email bilgisini söker
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Token'ın içinden gömdüğümüz tenantId bilgisini söker
    public String extractTenantId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("tenantId", String.class);
    }

    // Token'ın süresi dolmuş mu kontrol eder
    public boolean isTokenValid(String token, String userEmail) {
        final String email = extractEmail(token);
        return (email.equals(userEmail) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}