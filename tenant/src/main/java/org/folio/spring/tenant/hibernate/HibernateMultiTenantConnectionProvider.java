package org.folio.spring.tenant.hibernate;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.folio.spring.tenant.config.TenantConfig;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HibernateMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

  private static final long serialVersionUID = 5748544845255283079L;

  @Value("${spring.datasource.platform:h2}")
  private String platform;

  @Autowired
  private TenantConfig tenantConfig;

  @Autowired
  private DataSource dataSource;

  @Override
  public Connection getAnyConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public Connection getConnection(String tenant) throws SQLException {
    final Connection connection = getAnyConnection();
    try {

      switch (platform) {
      case "h2":
        connection.createStatement().execute("USE " + tenant);
        break;
      case "postgres":
        connection.setSchema(tenant);
        break;
      default:
        throw new HibernateException("Unknown datasource platform [" + platform + "]");
      }

      // H2
      // connection.createStatement().execute("USE " + tenant);
      // connection.createStatement().execute("SET SCHEMA " + tenant);

      // PostgreSql
      // connection.setSchema(tenant);
      // connection.createStatement().execute("SET SCHEMA '" + tenant + "';");
    } catch (SQLException e) {
      throw new HibernateException("Could not alter JDBC connection to use schema [" + tenant + "]", e);
    }
    return connection;
  }

  @Override
  public void releaseConnection(String tenant, Connection connection) throws SQLException {
    try {
      switch (platform) {
      case "h2":
        connection.createStatement().execute("USE " + tenantConfig.getDefaultTenant());
        break;
      case "postgres":
        connection.setSchema(tenantConfig.getDefaultTenant());
        break;
      default:
        throw new HibernateException("Unknown datasource platform [" + platform + "]");
      }

      // H2
      // connection.createStatement().execute("USE " + DEFAULT_TENANT);
      // connection.createStatement().execute("SET SCHEMA " + DEFAULT_TENANT);

      // PostgreSql
      // connection.setSchema(DEFAULT_TENANT);
      // connection.createStatement().execute("SET SCHEMA '" + DEFAULT_TENANT + "';");
    } catch (SQLException e) {
      throw new HibernateException("Could not alter JDBC connection to use schema [" + tenantConfig.getDefaultTenant() + "]", e);
    }
    connection.close();
  }

  @Override
  @SuppressWarnings("rawtypes")
  public boolean isUnwrappableAs(Class unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    return null;
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return true;
  }

}
