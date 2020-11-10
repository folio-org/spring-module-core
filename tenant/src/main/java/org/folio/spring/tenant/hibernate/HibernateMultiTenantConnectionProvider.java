package org.folio.spring.tenant.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.folio.spring.tenant.properties.Tenant;
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
  private Tenant tenantProperties;

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
          try (Statement statement = connection.createStatement()) {
            statement.execute("USE " + tenant);
          }
          break;
        case "postgres":
          connection.setSchema(tenant);
          break;
        default:
          throw new HibernateException("Unknown datasource platform [" + platform + "]");
      }
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
          try (Statement statement = connection.createStatement()) {
            statement.execute("USE " + tenantProperties.getDefaultTenant());
          }
          break;
        case "postgres":
          connection.setSchema(tenantProperties.getDefaultTenant());
          break;
        default:
          throw new HibernateException("Unknown datasource platform [" + platform + "]");
      }
    } catch (SQLException e) {
      throw new HibernateException(
          "Could not alter JDBC connection to use schema [" + tenantProperties.getDefaultTenant() + "]", e);
    } finally {
      connection.close();
    }
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
