package org.folio.spring.tenant.hibernate;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import org.folio.spring.tenant.config.TenantConfig;
import org.folio.spring.tenant.exception.NoTenantHeaderException;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@DependsOn("hibernateSchemaService")
public class HibernateTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

  private final TenantConfig tenantProperties;

  public HibernateTenantIdentifierResolver(TenantConfig tenantProperties) {
    this.tenantProperties = tenantProperties;
  }

  @PreDestroy
  public void unload() {
    ThreadLocalStorage.unload();
  }

  @Override
  public String resolveCurrentTenantIdentifier() {

    final HttpServletRequest request = getRequestFromContext();

    String tenant = ThreadLocalStorage.getTenant();

    if (request == null) {
      ThreadLocalStorage.getTenant();
    } else {
      tenant = request.getHeader(tenantProperties.getHeaderName());

      if (tenantProperties.isForceTenant()) {
        throw new NoTenantHeaderException("No tenant header on request!");
      }
    }

    if (tenant == null || tenant.isBlank()) {
      tenant = tenantProperties.getDefaultTenant();
    }

    return tenant;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }

  private HttpServletRequest getRequestFromContext() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes != null) {
      return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
    return null;
  }

}
