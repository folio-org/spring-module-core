package org.folio.spring.tenant.config;

import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TenantConfigTest {

  private TenantConfig tenantConfig;

  private List<String> domainPackages;

  private List<String> schemaScripts;

  @BeforeEach
  void beforeEach() {
    tenantConfig = new TenantConfig();
    domainPackages = new ArrayList<>();
    schemaScripts = new ArrayList<>();

    domainPackages.add(VALUE);
    schemaScripts.add(VALUE);
  }

  @Test
  void getHeaderNameWorksTest() {
    setField(tenantConfig, "headerName", VALUE);

    assertEquals(VALUE, tenantConfig.getHeaderName());
  }

  @Test
  void setHeaderNameWorksTest() {
    setField(tenantConfig, "headerName", null);

    tenantConfig.setHeaderName(VALUE);
    assertEquals(VALUE, getField(tenantConfig, "headerName"));
  }

  @Test
  void isForceTenantWorksTest() {
    setField(tenantConfig, "forceTenant", true);

    assertEquals(true, tenantConfig.isForceTenant());
  }

  @Test
  void setForceTenantWorksTest() {
    setField(tenantConfig, "forceTenant", false);

    tenantConfig.setForceTenant(true);
    assertEquals(true, getField(tenantConfig, "forceTenant"));
  }

  @Test
  void getDefaultTenantWorksTest() {
    setField(tenantConfig, "defaultTenant", VALUE);

    assertEquals(VALUE, tenantConfig.getDefaultTenant());
  }

  @Test
  void setDefaultTenantWorksTest() {
    setField(tenantConfig, "defaultTenant", null);

    tenantConfig.setDefaultTenant(VALUE);
    assertEquals(VALUE, getField(tenantConfig, "defaultTenant"));
  }

  @Test
  void isInitializeDefaultTenantWorksTest() {
    setField(tenantConfig, "initializeDefaultTenant", true);

    assertEquals(true, tenantConfig.isInitializeDefaultTenant());
  }

  @Test
  void setInitializeDefaultTenantWorksTest() {
    setField(tenantConfig, "initializeDefaultTenant", false);

    tenantConfig.setInitializeDefaultTenant(true);
    assertEquals(true, getField(tenantConfig, "initializeDefaultTenant"));
  }

  @Test
  void isRecreateDefaultTenantWorksTest() {
    setField(tenantConfig, "recreateDefaultTenant", true);

    assertEquals(true, tenantConfig.isRecreateDefaultTenant());
  }

  @Test
  void setRecreateDefaultTenantWorksTest() {
    setField(tenantConfig, "recreateDefaultTenant", false);

    tenantConfig.setRecreateDefaultTenant(true);
    assertEquals(true, getField(tenantConfig, "recreateDefaultTenant"));
  }

  @Test
  void getDomainPackagesWorksTest() {
    setField(tenantConfig, "domainPackages", domainPackages);

    assertEquals(domainPackages, tenantConfig.getDomainPackages());
  }

  @Test
  void setDomainPackagesWorksTest() {
    setField(tenantConfig, "domainPackages", null);

    tenantConfig.setDomainPackages(domainPackages);
    assertEquals(domainPackages, getField(tenantConfig, "domainPackages"));
  }

  @Test
  void getSchemaScriptsWorksTest() {
    setField(tenantConfig, "schemaScripts", schemaScripts);

    assertEquals(schemaScripts, tenantConfig.getSchemaScripts());
  }

  @Test
  void setSchemaScriptsWorksTest() {
    setField(tenantConfig, "schemaScripts", null);

    tenantConfig.setSchemaScripts(schemaScripts);
    assertEquals(schemaScripts, getField(tenantConfig, "schemaScripts"));
  }

}
