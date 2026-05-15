package org.folio.spring.tenant.config;

import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER;
import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * Multi-tenant hibernate configuration.
 */
@Configuration
public class HibernateMultiTenantConfig {

  @Bean
  JpaVendorAdapter jpaVendorAdapter() {
    return new HibernateJpaVendorAdapter();
  }

  @Bean
  @Primary
  LocalContainerEntityManagerFactoryBean entityManagerFactory(
    DataSource dataSource,
    JpaProperties jpaProperties,
    @SuppressWarnings("rawtypes") MultiTenantConnectionProvider multiTenantConnectionProvider,
    @SuppressWarnings("rawtypes") CurrentTenantIdentifierResolver currentTenantIdentifierResolver
  ) {
    Map<String, Object> properties = new HashMap<>();
    properties.putAll(jpaProperties.getProperties());
    properties.put(MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
    properties.put(MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("org.folio.rest");
    em.setJpaVendorAdapter(jpaVendorAdapter());
    em.setJpaPropertyMap(properties);
    return em;
  }

}
