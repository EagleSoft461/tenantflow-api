package org.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // 2. Metot adını ve içindeki değişken uyuşmazlığını (keyBytes) düzelttik
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Kullanıcı giriş yaptığında ona özel token üretir
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        // En kritik yer: Kiracı (Tenant) bilgisini token payload'una gömüyoruz!
        extraClaims.put("tenantId", user.getTenantId());
        extraClaims.put("role", user.getRole());
        extraClaims.put("userId", user.getId());

        long currentTime = System.currentTimeMillis();
        String token = Jwts.builder()
                .claims(extraClaims)
                .subject(user.getEmail())
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + jwtExpiration))
                .signWith(getSigningKey())
                .compact();

        log.info("JWT token generated for user: {} with expiration: {} minutes", 
                 user.getEmail(), jwtExpiration / 60000);
        return token;
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

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        // Rolün "role" veya "roles" adıyla kaydedilmiş olma ihtimaline karşı güvenli çekiyoruz
        Object roleObj = claims.get("role");
        if (roleObj == null) {
            roleObj = claims.get("roles");
        }
        return roleObj != null ? roleObj.toString() : null;
    }
}