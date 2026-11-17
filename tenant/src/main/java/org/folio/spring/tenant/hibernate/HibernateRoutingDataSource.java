package org.folio.spring.tenant.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.folio.spring.tenant.service.SchemaService;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Override the routing data source to inject tenant information.
 */
public class HibernateRoutingDataSource extends AbstractRoutingDataSource {

  private static final Logger logger = LoggerFactory.getLogger(HibernateRoutingDataSource.class);

  private final DataSource dataSource;

  private final SchemaService schemaService;

  /**
   * Initializer.
   *
   * @param dataSource The default data source.
   */
  public HibernateRoutingDataSource(DataSource dataSource, SchemaService schemaService) {

    this.dataSource = dataSource;
    this.schemaService = schemaService;

    final Map<Object, Object> targets = new HashMap<>();
    targets.put("default", dataSource);

    setTargetDataSources(targets);
    setDefaultTargetDataSource(dataSource);
  }

  @Override
  protected @Nullable Object determineCurrentLookupKey() {

    return ThreadLocalStorage.getTenant();
  }

  @Override
  public Connection getConnection() throws SQLException {

    final Connection connection = dataSource.getConnection();
    final String tenant = ThreadLocalStorage.getTenant();

    if (tenant != null) {
      final String schema = schemaService.getSchema(tenant);

      logger.debug("Using schema '{}' with tenant of '{}'.", schema, tenant);

      connection.setSchema(schema);
      try (Statement statement = connection.createStatement()) {
        statement.execute("SET search_path TO " + schema + ", public");
      }
    }

    return connection;
  }

}
