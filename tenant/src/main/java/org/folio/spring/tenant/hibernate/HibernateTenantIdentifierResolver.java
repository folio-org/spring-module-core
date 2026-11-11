package org.folio.spring.tenant.hibernate;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import org.folio.spring.tenant.exception.NoTenantHeaderException;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@DependsOn("hibernateSchemaService")
public class HibernateTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

  @Autowired
  private TenantProperties tenantProperties;

  @PreDestroy
  public void unload() {
    ThreadLocalStorage.unload();
  }

  @Override
  public String resolveCurrentTenantIdentifier() {
    final HttpServletRequest request = getRequestFromContext();

    if (request == null) {
      final String tenant = ThreadLocalStorage.getTenant();

      if (tenant != null) {
        return tenant;
      }
    } else {
      final String tenant = request.getHeader(tenantProperties.getHeaderName());

      if (tenant != null) {
        return tenant;
      }

      if (tenantProperties.isForceTenant()) {
        throw new NoTenantHeaderException("No tenant header on request!");
      }
    }

    return tenantProperties.getDefaultTenant();
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
