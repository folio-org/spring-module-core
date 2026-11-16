package org.folio.spring.tenant.hibernate;

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
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.folio.spring.tenant.exception.TenantAlreadyExistsException;
import org.folio.spring.tenant.exception.TenantDoesNotExistsException;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.service.SchemaService;
import org.folio.spring.tenant.service.SqlTemplateService;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

@Service("hibernateSchemaService")
public class HibernateSchemaService implements InitializingBean {

  protected static final String CONNECTION_DRIVER_CLASS = "connection.driver_class";
  protected static final String DIALECT = "dialect";
  protected static final String DATABASE_PLATFORM = "database-platform";
  protected static final String HIBERNATE_DEFAULT_SCHEMA = "hibernate.default_schema";
  protected static final String HIBERNATE_DDLAUTO = "hibernate.ddl-auto";
  protected static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";
  protected static final String HIBERNATE_CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
  protected static final String HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";
  protected static final String HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";
  protected static final String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
  protected static final String HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION = "hibernate.jdbc.lob.non_contextual_creation";
  protected static final String HIBERNATE_OPENINVIEW = "hibernate.open-in-view";
  protected static final String HIBERNATE_SHOWSQL = "hibernate.show-sql";
  protected static final String QUERY_SCHEMA_EXISTS = "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)";

  private final List<String> domainPackages = new ArrayList<String>();

  private final DataSourceProperties dataSourceProperties;

  private final HibernateProperties hibernateProperties;

  private final List<HibernateTenantInit> hibernateTenantInitializations;

  private final JpaProperties jpaProperties;

  private final ResourceLoader resourceLoader;

  private final SchemaService schemaService;

  private final SqlTemplateService sqlTemplateService;

  private final TenantProperties tenantProperties;

  /**
   * Initializer.
   *
   * @param dataSourceProperties           The data source properties.
   * @param hibernateProperties            The Hibernate properties.
   * @param hibernateTenantInitializations The Hibernate tenant initializations.
   * @param jpaProperties                  The JPA properties.
   * @param resourceLoader                 The resource loader.
   * @param schemaService                  The schema service.
   * @param sqlTemplateService             The SQL template service.
   * @param tenantProperties               The tenant properties.
   */
  HibernateSchemaService(
    DataSourceProperties dataSourceProperties, HibernateProperties hibernateProperties,
    List<HibernateTenantInit> hibernateTenantInitializations, JpaProperties jpaProperties, ResourceLoader resourceLoader,
    SchemaService schemaService, SqlTemplateService sqlTemplateService, TenantProperties tenantProperties
  ) {

    this.dataSourceProperties = dataSourceProperties;
    this.hibernateProperties = hibernateProperties;
    this.jpaProperties = jpaProperties;
    this.hibernateTenantInitializations = hibernateTenantInitializations == null ? new ArrayList<>() : hibernateTenantInitializations;
    this.resourceLoader = resourceLoader;
    this.schemaService = schemaService;
    this.sqlTemplateService = sqlTemplateService;
    this.tenantProperties = tenantProperties;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    domainPackages.add("org.folio.rest.model");

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
        throw new TenantAlreadyExistsException("Tenant already exists: " + tenant);
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
        throw new TenantDoesNotExistsException("Tenant does not exist: " + tenant);
      }

      dropSchema(connection, getSchema(settings));
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
    final String schema = getSchema(settings);

    createSchema(connection, schema);
    createTables(settings);
    createAdditionalSchema(connection, schema);
    initializeData(connection, schema);

    for (HibernateTenantInit hibernateTenantInitialization : hibernateTenantInitializations) {
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
    try (Statement statement = connection.createStatement()) {
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
    try (Statement statement = connection.createStatement()) {
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

    try (PreparedStatement statement = connection.prepareStatement(QUERY_SCHEMA_EXISTS)) {
      statement.setString(1, getSchema(settings));

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
    settings.put(HIBERNATE_DEFAULT_SCHEMA, schemaService.getSchema(tenant));
    settings.put(HIBERNATE_HBM2DDL_AUTO, ddlAuto);
    settings.put(HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION, jpaProperties.getProperties().getOrDefault(HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION, "true"));
    settings.put(HIBERNATE_OPENINVIEW, Boolean.TRUE.equals(jpaProperties.getOpenInView()) ? "true" : "false");
    settings.put(HIBERNATE_SHOWSQL, jpaProperties.isShowSql() ? "true" : "false");

    return settings;
  }

  /**
   * Extract the schema name from settings and verify it as valid.
   *
   * @param settings The settings to extract the name from.
   *
   * @return The schema name.
   * 
   * @throws IllegalArgumentException On invalid schema name.
   */
  protected String getSchema(Map<String, String> settings) {
    final String name = settings.get(HIBERNATE_DEFAULT_SCHEMA);

    schemaService.ensureSchema(name);

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
