package org.folio.spring.tenant.service;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.folio.spring.tenant.config.JpaPropertiesHibernateConfig;
import org.folio.spring.tenant.config.SpringDataSourceConfig;
import org.folio.spring.tenant.interfaces.TenantDatabaseProvisioner;
import org.folio.spring.tenant.properties.BuildInfoProperties;
import org.folio.spring.tenant.properties.TenantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Service;

/**
 * Service for managing schemas.
 */
@Service
public class SchemaService {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaService.class);

  /**
   * Regular expression for restricting characters to ASCII word characters only.
   */
  public static final Pattern SCHEMA_REGEXP = Pattern.compile("\\w+");

  private final BuildInfoProperties buildInfoProperties;

  private final EntityManagerFactory entityManagerFactory;

  private final JpaPropertiesHibernateConfig jpaPropertiesHibernateConfig;

  private final SpringDataSourceConfig springDatasourceConfig;

  private final TenantProperties tenantProperties;

  private final ConcurrentHashMap<String, Boolean> provisioned;

  private final List<TenantDatabaseProvisioner> provisioners;

  /**
   * Initializer.
   *
   * @param buildInfoProperties          The build information properties.
   * @param entityManagerFactory         The entity manager factory.
   * @param jpaPropertiesHibernateConfig The JPA properties Hibernate configuration.
   * @param springDatasourceConfig       The Spring data source configuration.
   * @param tenantProperties             The tenant properties.
   */
  public SchemaService(
    BuildInfoProperties buildInfoProperties, EntityManagerFactory entityManagerFactory,
    JpaPropertiesHibernateConfig jpaPropertiesHibernateConfig, SpringDataSourceConfig springDatasourceConfig,
    TenantProperties tenantProperties
  ) {

    this.buildInfoProperties = buildInfoProperties;
    this.entityManagerFactory = entityManagerFactory;
    this.jpaPropertiesHibernateConfig = jpaPropertiesHibernateConfig;
    this.springDatasourceConfig = springDatasourceConfig;
    this.tenantProperties = tenantProperties;

    provisioned = new ConcurrentHashMap<>();

    provisioners = SpringFactoriesLoader
      .forDefaultResourceLocation(getClass().getClassLoader())
      .load(
        TenantDatabaseProvisioner.class,
        SpringFactoriesLoader.ArgumentResolver.of(EntityManagerFactory.class, this.entityManagerFactory)
      );
  }

  /**
   * Ensure that the given schema is properly provisioned.
   *
   * This calls all of the implementations of TenantDatabasProvisioner.
   *
   * @param schema The schema name.
   * @param tenant The tenant name.
   */
  public void ensureProvisionedSchema(String schema, String tenant) {

    if (provisioners.isEmpty()) {
      return;
    }

    provisioned.computeIfAbsent(schema, key -> {
      try (
        final Connection connection = DriverManager.getConnection(
          springDatasourceConfig.getUrl(),
          springDatasourceConfig.getUsername(),
          springDatasourceConfig.getPassword()
        );
      ) {
        ensureProvisionedSchema(schema, tenant, connection);

        return true;
      } catch (SQLException e) {
        throw new RuntimeException(String.format("Failed to provision a schema '%s' and tenant '%s'.", schema, tenant), e);
      }
    });
  }

  /**
   * Ensure that the given schema is properly provisioned.
   *
   * This calls all of the implementations of TenantDatabasProvisioner.
   *
   * @param schema     The schema name.
   * @param tenant     The tenant name.
   * @param connection The connection to use.
   */
  public void ensureProvisionedSchema(String schema, String tenant, Connection connection) {

    LOG.debug("Ensuring provisioned schema '{}' for tenant '{}', total provisioners is {}.", schema, tenant, provisioners.size());

    if (provisioners.isEmpty()) {
      return;
    }

    final Iterator<TenantDatabaseProvisioner> iter = provisioners.iterator();

    provisioned.computeIfAbsent(schema, key -> {
      try {
        while (iter.hasNext()) {
          iter.next()
            .provisionSchema(schema, tenant, connection);
        }

        return true;
      } catch (SQLException e) {
        throw new RuntimeException(String.format("Failed to provision a schema '%s' and tenant '%s'.", schema, tenant), e);
      }
    });
  }

  /**
   * Ensure that the given tenant is properly provisioned.
   *
   * This constructs the schema and then calls all of the implementations of TenantDatabasProvisioner.
   *
   * @param tenant     The tenant name.
   * @param connection The connection to use.
   */
  public void ensureProvisionedTenant(String tenant) {

    final String schema = getSchema(tenant);

    if (schema == null) {
      LOG.warn("Schema is NULL for tenant '{}' while ensuring tenant provisioning.", tenant);
    } else {
      ensureProvisionedSchema(schema, tenant);
    }
  }

  /**
   * Provide the database schema formatted for use by a given tenant.
   *
   * This generation relies on the `spring.jpa.properties.hibernate.default_schema` property handled by `JpaConfig`, using the provided value as a base for the schema name.
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

    final String folioBaseSchema = jpaPropertiesHibernateConfig.getDefaultSchema();

    final String baseSchema = (folioBaseSchema == null || folioBaseSchema.trim().isEmpty())
      ? buildInfoProperties.getArtifact()
      : folioBaseSchema;

    final String schemaName = String.format("%s_%s", tenant, baseSchema).replace("-", "_").toLowerCase(Locale.ROOT);

    validateSchemaName(schemaName);

    return schemaName;
  }

  /**
   * Validate that the given schema name is safe and valid.
   *
   * @param name The schema name to verify.
   *
   * @throws IllegalArgumentException On invalid schema name.
   */
  public void validateSchemaName(String name) {

    if (!SCHEMA_REGEXP.matcher(name).matches()) {
      throw new IllegalArgumentException("Illegal character in schema name: " + name);
    }
  }

}
