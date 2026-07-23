package org.example.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    public AuditTableInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS public.audit_logs (" +
                    "id SERIAL PRIMARY KEY, " +
                    "action VARCHAR(255), " +
                    "tenant_id VARCHAR(255), " +
                    "performed_by VARCHAR(255), " +
                    "timestamp TIMESTAMP, " +
                    "status VARCHAR(50))";

            jdbcTemplate.execute(sql);
            System.out.println(">>> public.audit_logs tablosu başarıyla kontrol edildi / oluşturuldu!");
        } catch (Exception e) {
            System.out.println(">>> Audit tablo oluşturma hatası: " + e.getMessage());
        }
    }
}
