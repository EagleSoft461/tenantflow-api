package org.example.config.multitenancy;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@SuppressWarnings("unused")
public class HibernateConfig {

    private final JpaProperties jpaProperties;
    private final DataSource dataSource;

    @Autowired
    public HibernateConfig(JpaProperties jpaProperties, DataSource dataSource) {
        this.jpaProperties = jpaProperties;
        this.dataSource = dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            TenantConnectionProvider connectionProvider,
            TenantSchemaResolver tenantResolver) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);

        // Entity paket yolunu kendi projenin yapısına göre buraya yazmalısın (Örn: org.example.entity veya model)
        em.setPackagesToScan("org.example.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());

        // Hibernate 6 Multi-Tenancy Ayarlarını Bağlıyoruz
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);

        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return txManager;
    }
}
