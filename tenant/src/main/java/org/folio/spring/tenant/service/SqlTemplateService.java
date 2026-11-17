package org.folio.spring.tenant.service;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Provide SQL template service.
 */
@Service
public class SqlTemplateService {

  private static final String SCHEMA_IMPORT_INITIAL = "import/initial";
  private static final String SCHEMA_SELECT = "schema/select";
  private static final String SCHEMA = "schema";
  private static final String DOT = ".";

  @Value("${spring.sql.init.platform:postgres}")
  private String platform;

  private final SpringTemplateEngine templateEngine;

  /**
   * Initializer.
   *
   * @param templateEngine The template engine.
   */
  public SqlTemplateService(SpringTemplateEngine templateEngine) {

    this.templateEngine = templateEngine;
  }

  public String templateImportSql(String schema) {
    return templateEngine.process(SCHEMA_IMPORT_INITIAL + DOT + platform, createContext(SCHEMA, schema));
  }

  public String templateSelectSchemaSql(String schema) {
    return templateEngine.process(SCHEMA_SELECT + DOT + platform, createContext(SCHEMA, schema));
  }

  public String templateInitSql(String prefix, String name, Object model) {
    return templateEngine.process(prefix + DOT + platform, createContext(name, model));
  }

  public Context createContext(String modelName, Object model) {
    Context ctx = new Context(Locale.getDefault());
    ctx.setVariable(modelName, model);
    return ctx;
  }

}
