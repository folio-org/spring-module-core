package org.folio.spring.tenant.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tenant")
public class TenantProperties implements Serializable {

  private static final long serialVersionUID = -4623270335202728322L;

  private String headerName = "X-Okapi-Tenant";

  private boolean forceTenant = true;

  private String defaultTenant = "public";

  private boolean initializeDefaultTenant = false;

  private boolean recreateDefaultTenant = false;

  private List<String> domainPackages = new ArrayList<>();

  private List<String> schemaScripts = new ArrayList<>();

  public String getHeaderName() {
    return headerName;
  }

  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  public boolean isForceTenant() {
    return forceTenant;
  }

  public void setForceTenant(boolean forceTenant) {
    this.forceTenant = forceTenant;
  }

  public String getDefaultTenant() {
    return defaultTenant;
  }

  public void setDefaultTenant(String defaultTenant) {
    this.defaultTenant = defaultTenant;
  }

  public boolean isInitializeDefaultTenant() {
    return initializeDefaultTenant;
  }

  public void setInitializeDefaultTenant(boolean initializeDefaultTenant) {
    this.initializeDefaultTenant = initializeDefaultTenant;
  }

  public boolean isRecreateDefaultTenant() {
    return recreateDefaultTenant;
  }

  public void setRecreateDefaultTenant(boolean recreateDefaultTenant) {
    this.recreateDefaultTenant = recreateDefaultTenant;
  }

  public List<String> getDomainPackages() {
    return domainPackages;
  }

  public void setDomainPackages(List<String> domainPackages) {
    this.domainPackages = domainPackages;
  }

  public List<String> getSchemaScripts() {
    return schemaScripts;
  }

  public void setSchemaScripts(List<String> schemaScripts) {
    this.schemaScripts = schemaScripts;
  }

}
