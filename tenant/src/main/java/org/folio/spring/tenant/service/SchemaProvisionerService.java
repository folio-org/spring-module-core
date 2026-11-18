package org.folio.spring.tenant.service;

import static org.springframework.core.io.support.SpringFactoriesLoader.ArgumentResolver;
import static org.springframework.core.io.support.SpringFactoriesLoader.forDefaultResourceLocation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.folio.spring.tenant.config.SpringDataSourceConfig;
import org.folio.spring.tenant.interfaces.TenantDatabaseProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for provisioning schemas per tenant.
 */
@Service
public class SchemaProvisionerService {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaProvisionerService.class);

  private final ApplicationContext applicationContext;

  private final DataSource provisioningDataSource;

  private final ConcurrentHashMap<String, Boolean> provisioned;

  private final List<TenantDatabaseProvisioner> provisioners;

  private final SpringDataSourceConfig springDatasourceConfig;

  /**
   * Initializer.
   *
   * @param applicationContext     The application context.
   * @param springDatasourceConfig The Spring data source configuration.
   */
  public SchemaProvisionerService(ApplicationContext applicationContext, SpringDataSourceConfig springDatasourceConfig) {

    this.applicationContext = applicationContext;
    this.springDatasourceConfig = springDatasourceConfig;

    provisioned = new ConcurrentHashMap<>();
    provisioners = new ArrayList<>();

    final HikariConfig config = new HikariConfig();
    config.setJdbcUrl(this.springDatasourceConfig.getUrl());
    config.setUsername(this.springDatasourceConfig.getUsername());
    config.setPassword(this.springDatasourceConfig.getPassword());
    config.setMaximumPoolSize(1);
    config.setPoolName(SchemaProvisionerService.class.getSimpleName());

    this.provisioningDataSource = new HikariDataSource(config);
  }

  /**
   * Populate the provisioners after everything is loaded.
   *
   * This is necessary here to avoid circular dependencies with the entity manager.
   * This cannot be performed during the class initialization.
   */
  @EventListener(ContextRefreshedEvent.class)
  public void contextRefreshedEventInit() {

    provisioners.clear();

    provisioners.addAll(
      forDefaultResourceLocation(getClass().getClassLoader())
        .load(
          TenantDatabaseProvisioner.class,
          ArgumentResolver
            .of(ApplicationContext.class, this.applicationContext)
            .and(DataSource.class, provisioningDataSource)
        )
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

    if (provisioners.isEmpty()) {
      return;
    }

    final Iterator<TenantDatabaseProvisioner> iter = provisioners.iterator();

    provisioned.computeIfAbsent(schema, key -> {
      LOG.debug("Ensuring provisioned schema '{}' for tenant '{}', total provisioners is {}.", schema, tenant, provisioners.size());

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
