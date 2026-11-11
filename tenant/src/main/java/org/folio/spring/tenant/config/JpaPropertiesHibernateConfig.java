package org.folio.spring.tenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Provide a helper for getting standard JPA configurations that have no proper getting in JpaProperties.
 *
 * This specifically provides just the "spring.jpa.properties.hibernate" properties.
 * Anything already directly provided by JpaProperties should not be added here.
 *
 * @see org.springframework.boot.jpa.autoconfigure.JpaProperties
 */
@Component
@ConfigurationProperties(prefix = "spring.jpa.properties.hibernate")
public class JpaPropertiesHibernateConfig {

  private String defaultSchema;

  /**
   * Get the default schema value.
   *
   * This is derived from "spring.jpa.properties.hibernate.default_schema".
   *
   * @return The
   */
  public String getDefaultSchema() {

    return defaultSchema;
  }

  /**
   * Set default schema value.
   *
   * @param defaultSchema The new default schema value.
   */
  public void setDefaultSchema(String defaultSchema) {

    this.defaultSchema = defaultSchema;
  }

}
