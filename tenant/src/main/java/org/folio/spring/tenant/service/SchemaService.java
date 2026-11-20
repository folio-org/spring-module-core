package org.folio.spring.tenant.service;

import java.util.Locale;
import java.util.regex.Pattern;
import org.folio.spring.tenant.config.JpaPropertiesHibernateConfig;
import org.folio.spring.tenant.config.TenantConfig;
import org.springframework.stereotype.Service;

@Service
public class SchemaService {

  // Allow list of characters prevents SQL injection.
  private static final Pattern SCHEMA_REGEXP = Pattern.compile("[a-z0-9_]+");

  private final JpaPropertiesHibernateConfig jpaPropertiesHibernateConfig;

  private final TenantConfig tenantConfig;

  /**
   * Initialize class.
   *
   * @param buildInfoProperties
   */
  public SchemaService(JpaPropertiesHibernateConfig jpaPropertiesHibernateConfig, TenantConfig tenantConfig) {

    this.jpaPropertiesHibernateConfig = jpaPropertiesHibernateConfig;
    this.tenantConfig = tenantConfig;
  }

  /**
   * Provide the database schema formatted for use by a given tenant.
   *
   * This generation relies on the `spring.jpa.properties.hibernate.default_schema` property handled by `JpaConfig`, using the provided value as a base for the schema name.
   * The tenant value is then prepended to the base schema.
   *
   * When no tenant is provided, the format will instead utilize the "default" tenant handled by `TenantProperties` (`tenant.default-tenant`).
   *
   * @param tenant The tenant.
   *
   * @return The formatted schema associated with the provided tenant. 
   */
  public String getSchema(String tenant) {

    if (tenant == null || tenant.trim().isEmpty()) {
      tenant = tenantConfig.getDefaultTenant();
    }

    final String baseSchema = jpaPropertiesHibernateConfig.getDefaultSchema();
    
    if (baseSchema == null || baseSchema.trim().isEmpty()) {
      throw new IllegalArgumentException("The base schema is not and must be defined, such as via DB_SCHEMA.");
    }

    final String schemaName = String.format("%s_%s", tenant, baseSchema).replace("-", "_").toLowerCase(Locale.ROOT);

    if (!SCHEMA_REGEXP.matcher(schemaName).matches()) {
      throw new IllegalArgumentException("Illegal character in schema name: " + schemaName);
    }

    return schemaName;
  }

}
