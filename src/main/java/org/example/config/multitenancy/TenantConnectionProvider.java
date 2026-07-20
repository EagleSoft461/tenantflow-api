package org.example.config.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@SuppressWarnings("unused")
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public TenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        // null kontrolünü kaldırıp doğrudan kapatıyoruz, çünkü zaten null gelemez
        connection.close();
    }

    @Override
    public Connection getConnection(@NonNull String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        try {
            connection.createStatement().execute("ALTER ROLE CURRENT_USER SET search_path TO " + tenantIdentifier + ", public;");
        } catch (SQLException e) {
            // Hata durumunda da null kontrolü olmadan güvenli kapatma yapıyoruz
            releaseAnyConnection(connection);
            throw new SQLException("Tenant şemasına yönlendirme başarısız oldu: " + tenantIdentifier, e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(@NonNull String tenantIdentifier, @NonNull Connection connection) throws SQLException {
        try {
            // Bağlantı havuza geri dönmeden önce güvenli sıfırlama yapıyoruz
            connection.createStatement().execute("ALTER ROLE CURRENT_USER SET search_path TO public;");
        } catch (SQLException e) {
            throw new SQLException("Şema sıfırlama işlemi başarısız oldu", e);
        } finally {
            releaseAnyConnection(connection);
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(@NonNull Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(@NonNull Class<T> unwrapType) {
        return null;
    }
}
