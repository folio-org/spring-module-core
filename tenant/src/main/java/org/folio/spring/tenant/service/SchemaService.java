package org.folio.spring.tenant.service;

import java.util.Locale;
import java.util.regex.Pattern;
import org.folio.spring.tenant.config.FolioConfig;
import org.folio.spring.tenant.properties.BuildInfoProperties;
import org.folio.spring.tenant.properties.TenantProperties;
import org.springframework.stereotype.Service;

@Service
public class SchemaService {

  // allow list of characters prevents SQL injection
  private static final Pattern SCHEMA_REGEXP = Pattern.compile("[a-z0-9_]+");

  private final BuildInfoProperties buildInfoProperties;

  private final FolioConfig folioConfig;

  private final TenantProperties tenantProperties;

  /**
   * Initialize class.
   *
   * @param buildInfoProperties
   */
  public SchemaService(BuildInfoProperties buildInfoProperties, FolioConfig folioConfig, TenantProperties tenantProperties) {

    this.buildInfoProperties = buildInfoProperties;
    this.folioConfig = folioConfig;
    this.tenantProperties = tenantProperties;
  }

  /**
   * Provide the database schema formatted for use by a given tenant.
   * 
   * This generation relies on the `folio.sql.schema` property handled by `FolioConfig`, using the provided value as a base for the schema name.
   * The tenant value is then prepended to the base schema.
   * 
   * When no schema is provided, the format will default to the project artifact handled by `BuildInfoProperties`.
   * 
   * When no tenant is provided, the format will instead utilize the "default" tenant handled by `TenantProperties` (`tenant.default-tenant`).
   * 
   * @param tenant The tenant.
   * 
   * @return The formatted schema associated with the provided tenant. 
   */
  public String getSchema(String tenant) {
    
    if (tenant == null || tenant.trim().isEmpty()) {
      tenant = tenantProperties.getDefaultTenant();
    }

    final String folioBaseSchema = folioConfig.getSql().getSchema();

    final String baseSchema = (folioBaseSchema == null || folioBaseSchema.trim().isEmpty())
      ? buildInfoProperties.getArtifact()
      : folioBaseSchema;

    final String schemaName = String.format("%s_%s", tenant, baseSchema).replace("-", "_").toLowerCase(Locale.ROOT);

    if (!SCHEMA_REGEXP.matcher(schemaName).matches()) {
      throw new IllegalArgumentException("Illegal character in schema name: " + schemaName);
    }

    return schemaName;
  }

}
