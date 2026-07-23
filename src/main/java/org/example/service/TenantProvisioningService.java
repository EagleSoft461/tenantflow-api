package org.example.service;

import org.example.entity.AuditLog;
import org.example.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class TenantProvisioningService {

    private final DataSource dataSource;
    private final AuditLogRepository auditLogRepository;

    public TenantProvisioningService(DataSource dataSource, AuditLogRepository auditLogRepository) {
        this.dataSource = dataSource;
        this.auditLogRepository = auditLogRepository;
    }

    public void provisionTenant(String tenantId){
        if (tenantId == null || !tenantId.matches("^[a-z0-9]{3,20}$")) {
            throw new IllegalArgumentException("Güvenlik İhlali veya Geçersiz Tenant ID!");
        }

        String schemaName = "schema_" + tenantId;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()){

            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            saveAudit("CREATE_TENANT", tenantId, "SUCCESS");

        } catch (Exception e) {
            saveAudit("CREATE_TENANT", tenantId, "FAILED");
            throw new RuntimeException("Tenant şeması oluşturulurken hata meydana geldi: " + tenantId, e);
        }
    }

    public List<String> getAllTenants() {
        List<String> tenants = new ArrayList<>();
        String query = "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'schema_%'";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             var resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String fullSchemaName = resultSet.getString("schema_name");
                tenants.add(fullSchemaName.replace("schema_", ""));
            }
        } catch (Exception e) {
            throw new RuntimeException("Tenant listesi alınırken hata oluştu!", e);
        }
        return tenants;
    }

    public void deleteTenant(String tenantId) {
        if (tenantId == null || !tenantId.matches("^[a-z0-9]{3,20}$")) {
            throw new IllegalArgumentException("Geçersiz Tenant ID!");
        }

        String schemaName = "schema_" + tenantId;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()){

            statement.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
            saveAudit("DELETE_TENANT", tenantId, "SUCCESS");

        } catch (Exception e) {
            saveAudit("DELETE_TENANT", tenantId, "FAILED");
            throw new RuntimeException("Tenant şeması silinirken hata meydana geldi: " + tenantId, e);
        }
    }

    private void saveAudit(String action, String tenantId, String status) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setTenantId(tenantId);
        log.setPerformedBy("ADMIN");
        log.setStatus(status);
        auditLogRepository.save(log);
    }
}