package org.folio.spring.tenant.service;

import java.util.Locale;
import java.util.regex.Pattern;
import org.folio.spring.tenant.config.FolioConfig;
import org.folio.spring.tenant.properties.BuildInfoProperties;
import org.springframework.stereotype.Service;

@Service
public class SchemaService {

  // allow list of characters prevents SQL injection
  private static final Pattern SCHEMA_REGEXP = Pattern.compile("[a-z0-9_]+");

  private BuildInfoProperties buildInfoProperties;

  private FolioConfig folioConfig;

  /**
   * Initialize class.
   *
   * @param buildInfoProperties
   */
  public SchemaService(BuildInfoProperties buildInfoProperties, FolioConfig folioConfig) {

    this.buildInfoProperties = buildInfoProperties;
    this.folioConfig = folioConfig;
  }

  public String getSchema(String tenant) {

    final String folioBaseSchema = folioConfig.getSql().getSchema();

    final String baseSchema = (folioBaseSchema == null || folioBaseSchema.trim().isEmpty())
      ? buildInfoProperties.getArtifact()
      : folioBaseSchema;

    final String schemaName = String.format("%s_%s", tenant, baseSchema).replace("-", "_").toLowerCase(Locale.ROOT);

    if (!SCHEMA_REGEXP.matcher(schemaName).matches()) {
      throw new IllegalArgumentException("Illegal character in schema name: " + baseSchema);
    }

    return schemaName;
  }

}
