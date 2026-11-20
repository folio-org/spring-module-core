package org.folio.spring.tenant.hibernate;

import static org.springframework.core.io.support.SpringFactoriesLoader.forDefaultResourceLocation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.folio.spring.tenant.config.SpringDataSourceConfig;
import org.folio.spring.tenant.exception.TenantAlreadyExistsException;
import org.folio.spring.tenant.exception.TenantDoesNotExistsException;
import org.folio.spring.tenant.interfaces.TenantDatabaseProvisioner;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.service.SchemaService;
import org.folio.spring.tenant.service.SqlTemplateService;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader.ArgumentResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

/**
 * Hibernate schema service.
 */
//@Service("hibernateSchemaService")
@Service
public class HibernateSchemaService implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(HibernateSchemaService.class);

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
   * Name of the data source pool for provisioning tenant specific schemas.
   */
  public static final String POOL_NAME = "TenantSchemaProvisioning";

  /**
   * Hibernate query schema existence SQL query.
   *
   * This accepts a single argument for the query name.
   */
  public static final String QUERY_SCHEMA_EXISTS = "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)";

  private final ApplicationContext applicationContext;

  private final Map<String, String> defaultSettings;

  private final List<String> domainPackages;

  private final DataSourceProperties dataSourceProperties;

  private final HibernateProperties hibernateProperties;

  private final List<HibernateTenantInit> hibernateTenantInitializations;

  private final JpaProperties jpaProperties;

  private final Set<String> provisioned;

  private final List<TenantDatabaseProvisioner> provisioners;

  private final DataSource provisioningDataSource;

  private final ResourceLoader resourceLoader;

  private final ConcurrentHashMap<String, Object> schemaLocks;

  private final SchemaService schemaService;

  private final SpringDataSourceConfig springDatasourceConfig;

  private final SqlTemplateService sqlTemplateService;

  private final TenantProperties tenantProperties;

  /**
   * Initializer.
   *
   * @param applicationContext             The application context.
   * @param dataSourceProperties           The data source properties.
   * @param hibernateProperties            The Hibernate properties.
   * @param hibernateTenantInitializations The Hibernate tenant initializations.
   * @param jpaProperties                  The JPA properties.
   * @param resourceLoader                 The resource loader.
   * @param schemaService                  The schema service.
   * @param springDatasourceConfig         The spring data source properties.
   * @param sqlTemplateService             The SQL template service.
   * @param tenantProperties               The tenant properties.
   */
  public HibernateSchemaService(
    ApplicationContext applicationContext, DataSourceProperties dataSourceProperties, HibernateProperties hibernateProperties,
    List<HibernateTenantInit> hibernateTenantInitializations, JpaProperties jpaProperties, ResourceLoader resourceLoader,
    SchemaService schemaService, SpringDataSourceConfig springDatasourceConfig, SqlTemplateService sqlTemplateService, TenantProperties tenantProperties
  ) {

    this.applicationContext = applicationContext;
    this.dataSourceProperties = dataSourceProperties;
    this.hibernateProperties = hibernateProperties;
    this.jpaProperties = jpaProperties;
    this.hibernateTenantInitializations = hibernateTenantInitializations == null ? new ArrayList<>() : hibernateTenantInitializations;
    this.resourceLoader = resourceLoader;
    this.schemaService = schemaService;
    this.springDatasourceConfig = springDatasourceConfig;
    this.sqlTemplateService = sqlTemplateService;
    this.tenantProperties = tenantProperties;

    domainPackages = new ArrayList<String>();
    provisioned = new HashSet<>();
    provisioners = new ArrayList<>();
    schemaLocks = new ConcurrentHashMap<>();

    // Ensure that there is always a tenant, even if it is the default.
    ThreadLocalStorage.setTenant(tenantProperties.getDefaultTenant());
    defaultSettings = getSettings(ThreadLocalStorage.getTenant());

    this.provisioningDataSource = buildProvisioningDataSource();

    loadProvisioners();
  }

  @Override
  public void afterPropertiesSet() throws Exception {

    final String tenant = ThreadLocalStorage.getTenant();
    final String schema = schemaService.getSchema(tenant);

    LOG.debug("Performing after properties set action for domain packages '{}' with tenant '{}' and schema '{}'.", DOMAIN_PACKAGES, tenant, schema);

    domainPackages.add(DOMAIN_PACKAGES);

    for (String additionalDomainPackage : tenantProperties.getDomainPackages()) {
      domainPackages.add(additionalDomainPackage);
    }

    if (tenantProperties.isInitializeDefaultTenant()) {
      try (Connection connection = provisioningDataSource.getConnection()) {
        if (tenantProperties.isRecreateDefaultTenant()) {
          if (schemaExists(connection, schema)) {
            deleteTenant(tenant);
          }

          ensureProvisionedSchema(schema, tenant);
          //initializeSchema(connection, schema);
        } else if (!schemaExists(connection, schema)) {
          ensureProvisionedSchema(schema, tenant);
          //initializeSchema(connection, schema);
        }
      }
    }
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

    if (provisioned.contains(schema)) return;

    synchronized (schemaLocks.computeIfAbsent(schema, key -> new Object())) {

      if (provisioned.contains(schema)) {
        return;
      }

      LOG.debug("Ensuring provisioned schema '{}' for tenant '{}', total provisioners is {}.", schema, tenant, provisioners.size());

      final Iterator<TenantDatabaseProvisioner> iter = provisioners.iterator();

      try {
        while (iter.hasNext()) {
          iter.next()
            .provisionSchema(schema, tenant);
        }
      } catch (SQLException e) {
        throw new RuntimeException(String.format("Failed to provision a schema '%s' and tenant '%s'.", schema, tenant), e);
      }

      provisioned.add(schema);
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
    final String schema = schemaService.getSchema(tenant);

    try (final Connection connection = provisioningDataSource.getConnection()) {
      if (schemaExists(connection, schema)) {
        throw new TenantAlreadyExistsException(String.format("Tenant '%s' already exists.", tenant));
      }

      ensureProvisionedSchema(schema, tenant);
      //initializeSchema(connection, schema);
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

    final String schema = schemaService.getSchema(tenant);

    try (final Connection connection = provisioningDataSource.getConnection()) {
      if (!schemaExists(connection, schema)) {
        throw new TenantDoesNotExistsException(String.format("Schema '%s' for tenant '%s' does not exist.", schema, tenant));
      }

      dropSchema(connection, schema);
    }
  }

  /**
   * Clean up provisioning service, properly closing pooled connections.
   */
  @PreDestroy
  public void preDestroy() {

    if (provisioningDataSource.getClass().isInstance(HikariDataSource.class)) {
      ((HikariDataSource) provisioningDataSource).close();
    }

    ThreadLocalStorage.unload();
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

    try (final Connection connection = provisioningDataSource.getConnection()) {
      return schemaExists(connection, tenant);
    }
  }

  /**
   * Initialize the schema.
   *
   * @param connection The connection.
   * @param schema     The schema.
   *
   * @throws SQLException On error.
   * @throws IOException  On error.
   */
  protected void initializeSchema(Connection connection, String schema) throws SQLException, IOException {

    LOG.debug("Initializing schema '{}'.", schema);

    createSchema(connection, schema);
    createTables(schema);
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
   * @param schema The schema.
   */
  protected void createTables(String schema) {
    final MetadataImplementor metadata = buildMetadata(schema);

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
   * @param tenant     The tenant.
   *
   * @return TRUE if schema exists, FALSE otherwise.
   *
   * @throws SQLException On error.
   */
  protected boolean schemaExists(Connection connection, String tenant) throws SQLException {

    try (final PreparedStatement statement = connection.prepareStatement(QUERY_SCHEMA_EXISTS)) {
      statement.setString(1, schemaService.getSchema(tenant));

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

    schemaService.validateSchemaName(name);

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
   * @param schema The schema.
   *
   * @return The meta data implementor.
   */
  protected MetadataImplementor buildMetadata(String schema) {
    final Map<String, Object> settings = Map.copyOf(defaultSettings);
    final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
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


  /**
   * Populate the provisioners after everything is loaded.
   */
  private void loadProvisioners() {

    provisioners.clear();

    final List<TenantDatabaseProvisioner> classes = forDefaultResourceLocation(getClass().getClassLoader())
      .load(
        TenantDatabaseProvisioner.class,
        ArgumentResolver
          .of(ApplicationContext.class, applicationContext)
          .and(DataSource.class, provisioningDataSource)
          .and(SchemaService.class, schemaService)
      );

    provisioners.addAll(classes);

    final String names = classes.stream()
      .map(tdp -> tdp.getClass().getSimpleName())
      .collect(Collectors.joining(", "));

    LOG.debug("Loaded {} privisioners: {}.", provisioners.size(), names);
  }

  /**
   * Build the provisoning data source.
   *
   * @return The built data source.
   */
  private DataSource buildProvisioningDataSource() {

    // Pool size is exactly 2, one for the schema creation and one for the auto-schema creation.
    final HikariConfig config = new HikariConfig();
    config.setDriverClassName(defaultSettings.get(CONNECTION_DRIVER_CLASS));
    config.setJdbcUrl(springDatasourceConfig.getUrl());
    config.setMaximumPoolSize(2);
    config.setPassword(springDatasourceConfig.getPassword());
    config.setPoolName(POOL_NAME);
    config.setUsername(springDatasourceConfig.getUsername());

    return new HikariDataSource(config);
  }

}
