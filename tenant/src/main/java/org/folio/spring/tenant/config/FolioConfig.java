package org.folio.spring.tenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Provide tenant specific configuration.
 */
@Component
@ConfigurationProperties("folio")
public class FolioConfig {

  private FolioSqlConfig sql = new FolioSqlConfig();

  /**
   * Get the SQL configuration.
   *
   * @return The SQL configuration.
   */
  public FolioSqlConfig getSql() {
    return sql;
  }

  /**
   * Set the SQL configuration.
   *
   * @param sql The SQL configuration to set.
   */
  public void setSql(FolioSqlConfig sql) {
    this.sql = sql;
  }

  @ConfigurationProperties("folio.sql")
  public class FolioSqlConfig {

    private String schema;

    /**
     * Get the schema.
     *
     * @return The schema.
     */
    public String getSchema() {
      return schema;
    }

    /**
     * Set the schema.
     *
     * @param schema The schema to set.
     */
    public void setSchema(String schema) {
      this.schema = schema;
    }

  }

}
