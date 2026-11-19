package org.folio.spring.tenant.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.folio.spring.tenant.service.SchemaProvisionerService;
import org.folio.spring.tenant.service.SchemaService;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A Hibernate connection provider with multi-tenant support.
 */
@Component
public class HibernateMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

  private static final Logger LOG = LoggerFactory.getLogger(HibernateMultiTenantConnectionProvider.class);

  private static final long serialVersionUID = 5748544845255283079L;

  @Value("${spring.sql.init.platform:postgres}")
  private String platform;

  private final transient DataSource dataSource;

  private final transient SchemaService schemaService;

  // Must use ObjectProvider because this class must be fully configured before the EntityManagerFactory required by SchemaProvisionerService may exist.
  private final transient ObjectProvider<SchemaProvisionerService> schemaProvisionerServiceObject;

  /**
   * Initializer.
   *
   * @param dataSource                     The data source.
   * @param schemaService                  The schema service.
   * @param schemaProvisionerServiceObject An object provider for the schema provisioner service.
   */
  public HibernateMultiTenantConnectionProvider(
    DataSource dataSource, SchemaService schemaService, ObjectProvider<SchemaProvisionerService> schemaProvisionerServiceObject
  ) {

    super();

    this.dataSource = dataSource;
    this.schemaService = schemaService;
    this.schemaProvisionerServiceObject = schemaProvisionerServiceObject;
  }

  @Override
  public Connection getAnyConnection() throws SQLException {

    final String tenant = ThreadLocalStorage.getTenant();

    LOG.debug("Requesting **ANY** connection, with thread local storage tenant of '{}'.", tenant);

    /*
    final Connection connection = dataSource.getConnection();

    alterSchema(tenant, connection);

    return connection;
    */

    return dataSource.getConnection();
  }

  @Override
  public Connection getConnection(String tenant) throws SQLException {

    final Connection connection = dataSource.getConnection();

    LOG.debug("Requesting connection for tenant '{}' with schema '{}'.", tenant, connection.getSchema());

    alterSchema(tenant, connection);

    return connection;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public boolean isUnwrappableAs(Class unwrapType) {
    return false;
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {

    final String tenant = ThreadLocalStorage.getTenant();

    LOG.debug("Releasing connection for **ANY**, with thread local storage tenant of '{}' and schema '{}'.", tenant, connection.getSchema());

    connection.close();
  }

  @Override
  public void releaseConnection(String tenant, Connection connection) throws SQLException {

    LOG.debug("Releasing connection for tenant '{}' with schema '{}'.", tenant, connection.getSchema());

    try (final Statement statement = connection.createStatement()) {
      statement.execute("RESET search_path");
    }

    connection.close();
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    return null;
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return true;
  }

  /**
   * Alter the schema for the given tenant, selecting schema and performing provisioning as needed.
   *
   * @param tenant     Connecting tenant.
   * @param connection Connection to establish tenant schema connection on.
   *
   * @throws SQLException On SQL error.
   */
  @SuppressWarnings("java:S2077") // SonarQube false positive, the query is protected by getSchema() in SchemaService and cannot produce SQL escapes.
  private void alterSchema(String tenant, Connection connection) {

    final String schema = schemaService.getSchema(tenant);

    try {
      // Do nothing when schema NULL or blank.
      if (schema == null || schema.isBlank()) {
        return;
      }

      schemaProvisionerServiceObject
        .getObject()
        .ensureProvisionedSchema(schema, tenant);

      // Do not change schema if already set.
      if (schema.equalsIgnoreCase(connection.getSchema())) {
        return;
      }

      // Drivers that do not support this are ignored, making it safe to do this here.
      connection.setSchema(schema);

      switch (platform) {
        case "h2":
          try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("USE %s", schema));
          }
          break;
        case "postgres":
          try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("SET search_path TO %s, public", schema));
          }
  
          break;
        default:
          throw new HibernateException(String.format("Unknown datasource platform '%s' for schema '%s' and tenant '%s'.", platform, schema, tenant));
      }

      LOG.debug("Updated connection for tenant '{}', schema is now '{}' for platform '{}'.", tenant, connection.getSchema(), platform);
    } catch (SQLException e) {
      throw new HibernateException(String.format("Could not alter JDBC connection to use schema '%s' for tenant '%s' on platform '%s'", schema, tenant, platform), e);
    }
  }

}
