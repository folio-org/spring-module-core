package org.folio.spring.tenant.service;

import java.util.Locale;
import java.util.regex.Pattern;
import org.folio.spring.tenant.config.FolioConfig;
import org.folio.spring.tenant.properties.BuildInfoProperties;
import org.springframework.stereotype.Service;

/**
 * Resolves the SQL schema name for a given tenant.
 *
 * <p>
 * The schema name is either taken directly from {@code folio.sql.schema} if
 * configured, or derived from the tenant name and build artifact, normalized to
 * lowercase with hyphens replaced by underscores.
 */
@Service
public class SchemaService {

  /**
   * Allow list pattern preventing SQL injection via schema names.
   * Permits only lowercase alphanumeric characters and underscores.
   */
  private static final Pattern SCHEMA_PATTERN = Pattern.compile("[a-z0-9_]+");

  private final BuildInfoProperties buildInfoProperties;
  private final FolioConfig folioConfig;

  /**
   * Constructs a {@code SchemaService} with the required configuration
   * dependencies.
   *
   * @param buildInfoProperties build metadata used to derive a default schema name.
   * @param folioConfig         tenant-specific Folio configuration.
   */
  public SchemaService(BuildInfoProperties buildInfoProperties, FolioConfig folioConfig) {
    this.buildInfoProperties = buildInfoProperties;
    this.folioConfig = folioConfig;
  }

  /**
   * Returns the SQL schema name for the given tenant.
   *
   * <p>
   * If {@code folio.sql.schema} is set and non-blank, that value is returned
   * as-is.
   * Otherwise the schema is derived as {@code <tenant>_<artifact>}, lowercased,
   * with hyphens replaced by underscores.
   *
   * @param tenant the tenant identifier.
   * @return the resolved schema name.
   * @throws IllegalArgumentException if the resolved schema name contains
   *                                  characters outside {@code [a-z0-9_]}.
   */
  public String getSchema(String tenant) {
    if (tenant == null || tenant.isBlank()) {
      throw new IllegalArgumentException("Tenant identifier must not be null or blank.");
    }

    final String configuredSchema = folioConfig.getSql().getSchema();
    final String trimmed = (configuredSchema == null) ? null : configuredSchema.trim();

    final String schemaName = (trimmed == null || trimmed.isEmpty())
      ? String.format("%s_%s", tenant, buildInfoProperties.getArtifact())
        .replace("-", "_")
        .toLowerCase(Locale.ROOT)
      : trimmed;

    if (!SCHEMA_PATTERN.matcher(schemaName).matches()) {
      throw new IllegalArgumentException("Illegal character in schema name: " + schemaName);
    }

    return schemaName;
  }

}
