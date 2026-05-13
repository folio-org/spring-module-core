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
import org.springframework.beans.factory.annotation.Autowired;
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

  private final static String CONNECTION_DRIVER_CLASS = "connection.driver_class";
  private final static String DIALECT = "dialect";
  private final static String DATABASE_PLATFORM = "database-platform";
  private final static String HIBERNATE_DEFAULT_SCHEMA = "hibernate.default_schema";
  private final static String HIBERNATE_DDLAUTO = "hibernate.ddl-auto";
  private final static String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";
  private final static String HIBERNATE_CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
  private final static String HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";
  private final static String HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";
  private final static String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
  private final static String HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION = "hibernate.jdbc.lob.non_contextual_creation";
  private final static String HIBERNATE_OPENINVIEW = "hibernate.open-in-view";
  private final static String HIBERNATE_SHOWSQL = "hibernate.show-sql";

  private final List<String> domainPackages = new ArrayList<String>();

  @Autowired
  private SchemaService schemaService;

  @Autowired
  private TenantProperties tenantProperties;

  @Autowired
  private SqlTemplateService sqlTemplateService;

  @Autowired
  private DataSourceProperties dataSourceProperties;

  @Autowired
  private JpaProperties jpaProperties;

  @Autowired
  private HibernateProperties hibernateProperties;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired(required = false)
  private List<HibernateTenantInit> hibernateTenantInitializations = new ArrayList<>();

  @Override
  public void afterPropertiesSet() throws Exception {
    domainPackages.add("org.folio.rest.model");
    for (String additionalDomainPackage : tenantProperties.getDomainPackages()) {
      domainPackages.add(additionalDomainPackage);
    }
    if (tenantProperties.isInitializeDefaultTenant()) {
      String tenant = tenantProperties.getDefaultTenant();
      Map<String, String> settings = getSettings(tenant);

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

  public void createTenant(String tenant) throws SQLException, IOException {
    Map<String, String> settings = getSettings(tenant);
    try (Connection connection = getConnection(settings)) {
      if (schemaExists(connection, settings)) {
        throw new TenantAlreadyExistsException("Tenant already exists: " + tenant);
      }
      initializeSchema(connection, settings);
    }
  }

  public void deleteTenant(String tenant) throws SQLException {
    Map<String, String> settings = getSettings(tenant);
    try (Connection connection = getConnection(settings)) {
      if (!schemaExists(connection, settings)) {
        throw new TenantDoesNotExistsException("Tenant does not exist: " + tenant);
      }
      dropSchema(connection, getSchema(settings));
    }
  }

  public boolean schemaExists(String tenant) throws SQLException {
    Map<String, String> settings = getSettings(tenant);
    try (Connection connection = getConnection(settings)) {
      return schemaExists(connection, settings);
    }
  }

  private void initializeSchema(Connection connection, Map<String, String> settings) throws SQLException, IOException {
    String schema = getSchema(settings);
    createSchema(connection, schema);
    createTables(settings);
    createAdditionalSchema(connection, schema);
    initializeData(connection, schema);
    for (HibernateTenantInit hibernateTenantInitialization : hibernateTenantInitializations) {
      hibernateTenantInitialization.initialize(connection, schema);
    }
  }

  private void createSchema(Connection connection, String schema) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(String.format("CREATE SCHEMA IF NOT EXISTS %s;", schema));
    }
  }

  private void createTables(Map<String, String> settings) {
    MetadataImplementor metadata = buildMetadata(settings);
    SchemaExport schemaExport = new SchemaExport();
    schemaExport.create(EnumSet.of(TargetType.DATABASE), metadata);
  }

  private void initializeData(Connection connection, String schema) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sqlTemplateService.templateImportSql(schema));
    }
  }

  private void createAdditionalSchema(Connection connection, String schema) throws SQLException, IOException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sqlTemplateService.templateSelectSchemaSql(schema));
      for (String path : tenantProperties.getSchemaScripts()) {
        Resource resource = resourceLoader.getResource(path);
        File script;
        if (resource.getURI().getScheme().equals("jar")) {
          script = File.createTempFile("schema", ".sql");
          script.deleteOnExit();
          IOUtils.copy(resource.getInputStream(), new FileOutputStream(script));
        } else {
          script = resource.getFile();
        }
        statement.execute(FileUtils.readFileToString(script, StandardCharsets.UTF_8));
      }
    }
  }

  private void dropSchema(Connection connection, String schema) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(String.format("DROP SCHEMA IF EXISTS %s CASCADE;", schema));
    }
  }

  private boolean schemaExists(Connection connection, Map<String, String> settings) throws SQLException {
    String sql = "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, getSchema(settings));
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getBoolean(1);
      }
      return false;
    }
  }

  private Map<String, String> getSettings(String tenant) {
    String ddlAuto = hibernateProperties.getDdlAuto();
    if (ddlAuto == null) {
      ddlAuto = "none";
    }

    Map<String, String> settings = new HashMap<String, String>();
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
    settings.put(HIBERNATE_OPENINVIEW, jpaProperties.getOpenInView() ? "true" : "false");
    settings.put(HIBERNATE_SHOWSQL, jpaProperties.isShowSql() ? "true" : "false");

    return settings;
  }

  private String getSchema(Map<String, String> settings) {
    return settings.get(HIBERNATE_DEFAULT_SCHEMA);
  }

  private Connection getConnection(Map<String, String> settings) throws SQLException {
    String jdbcUrl = settings.get(HIBERNATE_CONNECTION_URL);
    String username = settings.get(HIBERNATE_CONNECTION_USERNAME);
    String password = settings.get(HIBERNATE_CONNECTION_PASSWORD);
    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  private MetadataImplementor buildMetadata(Map<String, String> settings) {
    Map<String, Object> settings2 = Map.copyOf(settings);
    StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings2).build();
    MetadataSources sources = addEntities(new MetadataSources(registry));
    return (MetadataImplementor) sources.getMetadataBuilder().build();
  }

  private MetadataSources addEntities(MetadataSources sources) {
    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
    for (String domainPackage : domainPackages) {
      for (BeanDefinition beanDefinition : scanner.findCandidateComponents(domainPackage)) {
        sources.addAnnotatedClassName(beanDefinition.getBeanClassName());
      }
    }
    return sources;
  }

}
