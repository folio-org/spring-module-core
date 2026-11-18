package org.folio.spring.tenant.service;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.folio.spring.tenant.config.SpringDataSourceConfig;
import org.folio.spring.tenant.interfaces.TenantDatabaseProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Service;

/**
 * Service for provisioning schemas per tenant.
 */
@Service
public class SchemaProvisionerService {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaProvisionerService.class);

  private final EntityManagerFactory entityManagerFactory;

  private final SpringDataSourceConfig springDatasourceConfig;

  private final ConcurrentHashMap<String, Boolean> provisioned;

  private final List<TenantDatabaseProvisioner> provisioners;

  /**
   * Initializer.
   *
   * @param entityManagerFactory   The entity manager factory.
   * @param springDatasourceConfig The Spring data source configuration.
   */
  public SchemaProvisionerService(EntityManagerFactory entityManagerFactory, SpringDataSourceConfig springDatasourceConfig) {

    this.entityManagerFactory = entityManagerFactory;
    this.springDatasourceConfig = springDatasourceConfig;

    provisioned = new ConcurrentHashMap<>();

    provisioners = SpringFactoriesLoader
      .forDefaultResourceLocation(getClass().getClassLoader())
      .load(
        TenantDatabaseProvisioner.class,
        SpringFactoriesLoader.ArgumentResolver.of(EntityManagerFactory.class, this.entityManagerFactory)
      );
  }

  /**
   * Ensure that the given schema is properly provisioned.
   *
   * This calls all of the implementations of TenantDatabasProvisioner.
   *
   * @param schema The schema name.
   * @param tenant The tenant name.
   */
  public void ensureProvisionedSchema(String schema, String tenant) {

    if (provisioners.isEmpty()) {
      return;
    }

    provisioned.computeIfAbsent(schema, key -> {
      try (
        final Connection connection = DriverManager.getConnection(
          springDatasourceConfig.getUrl(),
          springDatasourceConfig.getUsername(),
          springDatasourceConfig.getPassword()
        );
      ) {
        ensureProvisionedSchema(schema, tenant, connection);

        return true;
      } catch (SQLException e) {
        throw new RuntimeException(String.format("Failed to provision a schema '%s' and tenant '%s'.", schema, tenant), e);
      }
    });
  }

  /**
   * Ensure that the given schema is properly provisioned.
   *
   * This calls all of the implementations of TenantDatabasProvisioner.
   *
   * @param schema     The schema name.
   * @param tenant     The tenant name.
   * @param connection The connection to use.
   */
  public void ensureProvisionedSchema(String schema, String tenant, Connection connection) {

    LOG.debug("Ensuring provisioned schema '{}' for tenant '{}', total provisioners is {}.", schema, tenant, provisioners.size());

    if (provisioners.isEmpty()) {
      return;
    }

    final Iterator<TenantDatabaseProvisioner> iter = provisioners.iterator();

    provisioned.computeIfAbsent(schema, key -> {
      try {
        while (iter.hasNext()) {
          iter.next()
            .provisionSchema(schema, tenant, connection);
        }

        return true;
      } catch (SQLException e) {
        throw new RuntimeException(String.format("Failed to provision a schema '%s' and tenant '%s'.", schema, tenant), e);
      }
    });
  }

}
