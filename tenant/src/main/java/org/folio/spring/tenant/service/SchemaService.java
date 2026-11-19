package org.folio.spring.tenant.service;

import jakarta.persistence.Entity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.folio.spring.tenant.config.JpaPropertiesHibernateConfig;
import org.folio.spring.tenant.exception.TenantAlreadyExistsException;
import org.folio.spring.tenant.exception.TenantDoesNotExistsException;
import org.folio.spring.tenant.hibernate.HibernateTenantInit;
import org.folio.spring.tenant.properties.BuildInfoProperties;
import org.folio.spring.tenant.properties.TenantProperties;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

/**
 * Service for managing schemas.
 */
@Service
public class SchemaService {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaService.class);

  /**
   * Connection driver class.
   */
  public static final String CONNECTION_DRIVER_CLASS = "connection.driver_class";

  /**
   * Dialect.
   */
  public static final String DIALECT = "dialect";

  /**
   * Domain packages class path.
   */
  public static final String DOMAIN_PACKAGES = "org.folio.rest.model";

  /**
   * Database platform.
   */
  public static final String DATABASE_PLATFORM = "database-platform";

  /**
   * Hibernate default schema.
   */
  public static final String HIBERNATE_DEFAULT_SCHEMA = "hibernate.default-schema";

  /**
   * Hibernate ddl-auto.
   */
  public static final String HIBERNATE_DDLAUTO = "hibernate.ddl-auto";

  /**
   * Hibernation connection URL.
   */
  public static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";

  /**
   * Hibernate connection driver class.
   */
  public static final String HIBERNATE_CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";

  /**
   * Hibernate connection user name.
   */
  public static final String HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";

  /**
   * Hibernate connection pass.
   */
  public static final String HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";

  /**
   * Hibernate hbm2ddl auto.
   */
  public static final String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";

  /**
   * Hibernate JDBC LONBB non-contextual creation.
   */
  public static final String HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION = "hibernate.jdbc.lob.non_contextual_creation";

  /**
   * Hibernate open in view.
   */
  public static final String HIBERNATE_OPENINVIEW = "hibernate.open-in-view";

  /**
   * Hibernate show SQL.
   */
  public static final String HIBERNATE_SHOWSQL = "hibernate.show-sql";

  /**
   * Hibernate query schema existence SQL query.
   *
   * This accepts a single argument for the query name.
   */
  public static final String QUERY_SCHEMA_EXISTS = "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)";

  /**
   * Regular expression for restricting characters to ASCII word characters only.
   */
  public static final Pattern SCHEMA_REGEXP = Pattern.compile("\\w+");

  private final BuildInfoProperties buildInfoProperties;

  private final JpaPropertiesHibernateConfig jpaPropertiesHibernateConfig;

  private final TenantProperties tenantProperties;

  private final List<String> domainPackages = new ArrayList<String>();

  private final DataSourceProperties dataSourceProperties;

  private final HibernateProperties hibernateProperties;

  private final List<HibernateTenantInit> hibernateTenantInitializations;

  private final JpaProperties jpaProperties;

  private final ResourceLoader resourceLoader;

  private final SqlTemplateService sqlTemplateService;

  /**
   * Initializer.
   *
   * @param dataSourceProperties           The data source properties.
   * @param hibernateProperties            The Hibernate properties.
   * @param hibernateTenantInitializations The Hibernate tenant initializations.
   * @param jpaProperties                  The JPA properties.
   * @param resourceLoader                 The resource loader.
   * @param buildInfoProperties          The build information properties.
   * @param jpaPropertiesHibernateConfig The JPA properties Hibernate configuration.
   * @param springDatasourceConfig       The Spring data source configuration.
   * @param tenantProperties             The tenant properties.
   */
  public SchemaService(
    DataSourceProperties dataSourceProperties, HibernateProperties hibernateProperties,
    List<HibernateTenantInit> hibernateTenantInitializations, JpaProperties jpaProperties, ResourceLoader resourceLoader,
    SqlTemplateService sqlTemplateService, TenantProperties tenantProperties,
    BuildInfoProperties buildInfoProperties, JpaPropertiesHibernateConfig jpaPropertiesHibernateConfig
  ) {

    this.dataSourceProperties = dataSourceProperties;
    this.hibernateProperties = hibernateProperties;
    this.jpaProperties = jpaProperties;
    this.hibernateTenantInitializations = hibernateTenantInitializations == null ? new ArrayList<>() : hibernateTenantInitializations;
    this.resourceLoader = resourceLoader;
    this.buildInfoProperties = buildInfoProperties;
    this.jpaPropertiesHibernateConfig = jpaPropertiesHibernateConfig;
    this.sqlTemplateService = sqlTemplateService;
    this.tenantProperties = tenantProperties;
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

  public void afterPropertiesSet() throws Exception {
    LOG.debug("Performing after properties set action for domain packages '{}'.", DOMAIN_PACKAGES);

    domainPackages.add(DOMAIN_PACKAGES);

    for (String additionalDomainPackage : tenantProperties.getDomainPackages()) {
      domainPackages.add(additionalDomainPackage);
    }

    if (tenantProperties.isInitializeDefaultTenant()) {
      final String tenant = tenantProperties.getDefaultTenant();
      final Map<String, String> settings = getSettings(tenant);

      try (Connection connection = getConnection(settings)) {
        if (tenantProperties.isRecreateDefaultTenant()) {
          if (schemaExists(connection, settings)) {
            deleteTenant(tenant);
          }

          initializeSchema(connection, settings);
        } else if (!schemaExists(connection, settings)) {
          initializeSchema(connection, settings);
        }
      }
    }
  }

  /**
   * Create the schema by tenant.
   *
   * @param tenant The tenant name.
   *
   * @throws SQLException On error.
   * @throws IOException  On error.
   */
  public void createTenant(String tenant) throws SQLException, IOException {
    final Map<String, String> settings = getSettings(tenant);

    try (final Connection connection = getConnection(settings)) {
      if (schemaExists(connection, settings)) {
        throw new TenantAlreadyExistsException(String.format("Tenant '%s' already exists.", tenant));
      }

      initializeSchema(connection, settings);
    }
  }

  /**
   * Delete the schema by tenant.
   *
   * @param tenant The tenant name.
   *
   * @throws SQLException On error.
   */
  public void deleteTenant(String tenant) throws SQLException {
    final Map<String, String> settings = getSettings(tenant);

    try (final Connection connection = getConnection(settings)) {
      if (!schemaExists(connection, settings)) {
        throw new TenantDoesNotExistsException(String.format("Tenant '%s' does not exist.", tenant));
      }

      dropSchema(connection, getDefaultSchema(settings));
    }
  }

  /**
   * Verify that the schema exists by tenant.
   *
   * @param tenant The tenant name.
   *
   * @return TRUE if exists, FALSE otherwise.
   *
   * @throws SQLException On error.
   */
  public boolean schemaExists(String tenant) throws SQLException {
    final Map<String, String> settings = getSettings(tenant);

    try (final Connection connection = getConnection(settings)) {
      return schemaExists(connection, settings);
    }
  }

  /**
   * Initialize the schema.
   *
   * @param connection The connection.
   * @param settings   The data source settings.
   *
   * @throws SQLException On error.
   * @throws IOException  On error.
   */
  protected void initializeSchema(Connection connection, Map<String, String> settings) throws SQLException, IOException {
    final String schema = getDefaultSchema(settings);

    LOG.debug("Initializing schema '{}'.", schema);

    createSchema(connection, schema);
    createTables(settings);
    createAdditionalSchema(connection, schema);
    initializeData(connection, schema);

    for (final HibernateTenantInit hibernateTenantInitialization : hibernateTenantInitializations) {
      hibernateTenantInitialization.initialize(connection, schema);
    }
  }

  /**
   * Create the schema.
   *
   * @param connection The connection.
   * @param schema     The schema name. Caller must sanitize/verify name.
   *
   * @throws SQLException On error.
   */
  protected void createSchema(Connection connection, String schema) throws SQLException {
    try (final Statement statement = connection.createStatement()) {
      statement.executeUpdate(String.format("CREATE SCHEMA IF NOT EXISTS %s;", schema));
    }
  }

  /**
   * Create tables.
   *
   * @param settings The settings.
   */
  protected void createTables(Map<String, String> settings) {
    final MetadataImplementor metadata = buildMetadata(settings);

    final SchemaExport schemaExport = new SchemaExport();
    schemaExport.create(EnumSet.of(TargetType.DATABASE), metadata);
  }

  /**
   * Initialize the data.
   *
   * @param connection The connection.
   * @param schema     The schema name. Caller must sanitize/verify name.
   *
   * @throws SQLException On error.
   */
  protected void initializeData(Connection connection, String schema) throws SQLException {
    try (final Statement statement = connection.createStatement()) {
      statement.execute(sqlTemplateService.templateImportSql(schema));
    }
  }

  /**
   * Create additional schema data.
   *
   * @param connection The connection.
   * @param schema     The schema name. Caller must sanitize/verify name.
   *
   * @throws SQLException On error.
   * @throws IOException  On error.
   */
  protected void createAdditionalSchema(Connection connection, String schema) throws SQLException, IOException {
    try (final Statement statement = connection.createStatement()) {
      statement.execute(sqlTemplateService.templateSelectSchemaSql(schema));

      for (String path : tenantProperties.getSchemaScripts()) {
        final Resource resource = resourceLoader.getResource(path);
        final File script;
        if (resource.getURI().getScheme().equals("jar")) {
          script = File.createTempFile("schema", ".sql");
          script.deleteOnExit();
          IOUtils.copy(resource.getInputStream(), new FileOutputStream(script));
        } else {
          script = resource.getFile();
        }

        statement.execute(FileUtils.readFileToString(script, StandardCharsets.UTF_8));
      }

      statement.executeBatch();
    }
  }

  /**
   * Drop the schema.
   *
   * @param connection The connection.
   * @param schema     The schema name. Caller must sanitize/verify name.
   *
   * @throws SQLException On error.
   */
  protected void dropSchema(Connection connection, String schema) throws SQLException {
    try (final Statement statement = connection.createStatement()) {
      statement.executeUpdate(String.format("DROP SCHEMA IF EXISTS %s CASCADE;", schema));
    }
  }

  /**
   * Check if the schema exists.
   *
   * @param connection The connection.
   * @param settings   The settings.
   *
   * @return TRUE if schema exists, FALSE otherwise.
   *
   * @throws SQLException On error.
   */
  protected boolean schemaExists(Connection connection, Map<String, String> settings) throws SQLException {

    try (final PreparedStatement statement = connection.prepareStatement(QUERY_SCHEMA_EXISTS)) {
      statement.setString(1, getDefaultSchema(settings));

      final ResultSet resultSet = statement.executeQuery();

      return resultSet.next()
        ? resultSet.getBoolean(1)
        : Boolean.FALSE;
    }
  }

  /**
   * Get the settings.
   *
   * @param tenant The tenant name.
   *
   * @return The settings.
   */
  protected Map<String, String> getSettings(String tenant) {
    String ddlAuto = hibernateProperties.getDdlAuto();

    if (ddlAuto == null) {
      ddlAuto = "none";
    }

    final Map<String, String> settings = new HashMap<String, String>();
    settings.put(CONNECTION_DRIVER_CLASS, dataSourceProperties.getDriverClassName());
    settings.put(DATABASE_PLATFORM, jpaProperties.getDatabasePlatform());
    settings.put(DIALECT, jpaProperties.getDatabasePlatform());
    settings.put(HIBERNATE_CONNECTION_URL, dataSourceProperties.getUrl());
    settings.put(HIBERNATE_CONNECTION_DRIVER_CLASS, dataSourceProperties.getDriverClassName());
    settings.put(HIBERNATE_CONNECTION_USERNAME, dataSourceProperties.getUsername());
    settings.put(HIBERNATE_CONNECTION_PASSWORD, dataSourceProperties.getPassword());
    settings.put(HIBERNATE_DDLAUTO, ddlAuto);
    settings.put(HIBERNATE_DEFAULT_SCHEMA, getSchema(tenant));
    settings.put(HIBERNATE_HBM2DDL_AUTO, ddlAuto);
    settings.put(HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION, jpaProperties.getProperties().getOrDefault(HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION, "true"));
    settings.put(HIBERNATE_OPENINVIEW, Boolean.TRUE.equals(jpaProperties.getOpenInView()) ? "true" : "false");
    settings.put(HIBERNATE_SHOWSQL, jpaProperties.isShowSql() ? "true" : "false");

    return settings;
  }

  /**
   * Extract the default schema name (base schema name) from settings and verify it as valid.
   *
   * @param settings The settings to extract the name from.
   *
   * @return The schema name.
   * 
   * @throws IllegalArgumentException On invalid schema name.
   */
  protected String getDefaultSchema(Map<String, String> settings) {
    final String name = settings.get(HIBERNATE_DEFAULT_SCHEMA);

    validateSchemaName(name);

    return name;
  }

  /**
   * Get the connection.
   *
   * @param settings The settings.
   *
   * @return The connection.
   *
   * @throws SQLException On error.
   */
  protected Connection getConnection(Map<String, String> settings) throws SQLException {
    final String jdbcUrl = settings.get(HIBERNATE_CONNECTION_URL);
    final String username = settings.get(HIBERNATE_CONNECTION_USERNAME);
    final String password = settings.get(HIBERNATE_CONNECTION_PASSWORD);

    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  /**
   * Build the meta data.
   *
   * @param settings The settings.
   *
   * @return The meta data implementor.
   */
  protected MetadataImplementor buildMetadata(Map<String, String> settings) {
    final Map<String, Object> settings2 = Map.copyOf(settings);
    final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings2).build();
    final MetadataSources sources = addEntities(new MetadataSources(registry));

    return (MetadataImplementor) sources.getMetadataBuilder().build();
  }

  /**
   * Add entities.
   *
   * @param sources The sources to add entities to.
   *
   * @return The updated sources.
   */
  protected MetadataSources addEntities(MetadataSources sources) {
    final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

    for (final String domainPackage : domainPackages) {
      for (final BeanDefinition beanDefinition : scanner.findCandidateComponents(domainPackage)) {
        sources.addAnnotatedClassName(beanDefinition.getBeanClassName());
      }
    }

    return sources;
  }

}
