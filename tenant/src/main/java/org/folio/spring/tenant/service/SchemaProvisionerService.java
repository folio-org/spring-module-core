package org.folio.spring.tenant.service;

import static org.springframework.core.io.support.SpringFactoriesLoader.forDefaultResourceLocation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.folio.spring.tenant.config.SpringDataSourceConfig;
import org.folio.spring.tenant.interfaces.TenantDatabaseProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.support.SpringFactoriesLoader.ArgumentResolver;
import org.springframework.stereotype.Service;

/**
 * Service for provisioning schemas per tenant.
 */
@Service
public class SchemaProvisionerService {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaProvisionerService.class);

  private final ApplicationContext applicationContext;

  private final DataSource provisioningDataSource;

  private final Set<String> provisioned;

  private final List<TenantDatabaseProvisioner> provisioners;

  private final ConcurrentHashMap<String, Object> schemaLocks;

  private final SchemaService schemaService;

  private final SpringDataSourceConfig springDatasourceConfig;

  /**
   * Initializer.
   *
   * @param applicationContext     The application context.
   * @param schemaService          The schema service.
   * @param springDatasourceConfig The Spring data source configuration.
   */
  public SchemaProvisionerService(ApplicationContext applicationContext, SchemaService schemaService, SpringDataSourceConfig springDatasourceConfig) {

    this.applicationContext = applicationContext;
    this.schemaService = schemaService;
    this.springDatasourceConfig = springDatasourceConfig;

    // Pool size is exactly 2, one for the schema creation and one for the auto-schema creation.
    final HikariConfig config = new HikariConfig();
    config.setJdbcUrl(this.springDatasourceConfig.getUrl());
    config.setUsername(this.springDatasourceConfig.getUsername());
    config.setPassword(this.springDatasourceConfig.getPassword());
    config.setMaximumPoolSize(2);
    config.setPoolName("WorkflowTenantProvisioning");

    this.provisioningDataSource = new HikariDataSource(config);

    provisioned = new HashSet<>();
    provisioners = new ArrayList<>();
    schemaLocks = new ConcurrentHashMap<>();
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
            .of(ApplicationContext.class, applicationContext)
            .and(DataSource.class, provisioningDataSource)
            .and(SchemaService.class, schemaService)
        )
    );

    LOG.debug("Loaded {} privisioners.", provisioners.size());
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

    if (provisioned.contains(schema)) return;

    synchronized (schemaLocks.computeIfAbsent(schema, key -> new Object())) {

      if (provisioned.contains(schema)) {
        return;
      }

      LOG.debug("Ensuring provisioned schema '{}' for tenant '{}', total provisioners is {}.", schema, tenant, provisioners.size());

      final Iterator<TenantDatabaseProvisioner> iter = provisioners.iterator();

      try {
        while (iter.hasNext()) {
          iter.next()
            .provisionSchema(schema, tenant);
        }
      } catch (SQLException e) {
        throw new RuntimeException(String.format("Failed to provision a schema '%s' and tenant '%s'.", schema, tenant), e);
      }

      provisioned.add(schema);
    }
  }

  /**
   * Clean up provisioning service, properly closing pooled connections.
   */
  @PreDestroy
  public void preDestroy() {

    if (provisioningDataSource.getClass().isInstance(HikariDataSource.class)) {
      ((HikariDataSource) provisioningDataSource).close();
    }
  }

}
