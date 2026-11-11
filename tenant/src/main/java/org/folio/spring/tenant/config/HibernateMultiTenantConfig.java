package org.folio.spring.tenant.config;

import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER;
import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.cfg.MappingSettings;
import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
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

    final Map<String, Object> properties = new HashMap<>();
    properties.putAll(jpaProperties.getProperties());
    properties.put(MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
    properties.put(MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);

    // Remove the auto-prepended schema name and allow for the search_path to always take precedence.
    // For example, if `default_schema: apple` then a query might be `SELECT id from apple.fruits`.
    // If `default_schema` is not defined, then the query might instead be `SELECT id from fruits`.
    // The second query would rely solely on the `search_path` or equivalent.
    properties.remove(MappingSettings.DEFAULT_SCHEMA);

    final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("org.folio.rest");
    em.setJpaVendorAdapter(jpaVendorAdapter());
    em.setJpaPropertyMap(properties);

    return em;
  }

  @Bean
  HibernatePropertiesCustomizer multiTenantCustomizer(
    CurrentTenantIdentifierResolver<?> resolver,
    MultiTenantConnectionProvider<?> connectionProvider)
  {

    return hibernateProperties -> {
      hibernateProperties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
      hibernateProperties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
    };
  }

}
