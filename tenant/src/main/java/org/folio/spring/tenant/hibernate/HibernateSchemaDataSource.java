package org.folio.spring.tenant.hibernate;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a custom schema data source that injects the search_path.
 */
public class HibernateSchemaDataSource implements DataSource {

  private static final Logger LOG = LoggerFactory.getLogger(HibernateSchemaDataSource.class);

  private final DataSource dataSource;

  private final HibernateSchemaService hibernateSchemaService;

  /**
   * Initializer.
   *
   * @param dataSource    The default data source.
   * @param schemaService The schema service.
   */
  public HibernateSchemaDataSource(DataSource dataSource, HibernateSchemaService hibernateSchemaService) {

    this.dataSource = dataSource;
    this.hibernateSchemaService = hibernateSchemaService;
  }

  @Override
  public Connection getConnection() throws SQLException {

    final Connection connection = dataSource.getConnection();
    final String tenant = ThreadLocalStorage.getTenant();

    if (tenant != null) {
      final String schema = hibernateSchemaService.getTenantSchema(tenant);

      LOG.debug("Using schema '{}' with tenant of '{}'.", schema, tenant);

      connection.setSchema(schema);

      try (Statement statement = connection.createStatement()) {
        statement.execute(String.format("SET search_path TO %s, public", schema));
      }
    }

    return connection;
  }

   @Override
    public Connection getConnection(String username, String password) throws SQLException {

     final Connection connection = dataSource.getConnection(username, password);
     final String tenant = ThreadLocalStorage.getTenant();

     if (tenant != null) {
       final String schema = hibernateSchemaService.getTenantSchema(tenant);

       LOG.debug("Using schema '{}' with tenant of '{}'.", schema, tenant);

       connection.setSchema(schema);

       try (Statement statement = connection.createStatement()) {
         statement.execute(String.format("SET search_path TO %s, public", schema));
       }
     }

     return connection;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
      return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
      dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
      dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
      return dataSource.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return dataSource.isWrapperFor(iface);
    }

}

