package org.folio.spring.tenant.hibernate;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import org.folio.spring.tenant.exception.NoTenantHeaderException;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Construct or return the tenant as needed while resolving the tenant identifier for Hibernate.
 */
@Component
@DependsOn("hibernateSchemaService")
public class HibernateTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

  private final TenantProperties tenantProperties;

  /**
   * Initializer.
   *
   * @param tenantProperties The tenant properties.
   */
  public HibernateTenantIdentifierResolver(TenantProperties tenantProperties) {
    this.tenantProperties = tenantProperties;
  }

  /**
   * Perform clean up before deleting.
   */
  @PreDestroy
  public void unload() {
    ThreadLocalStorage.unload();
  }

  @Override
  public String resolveCurrentTenantIdentifier() {
    final HttpServletRequest request = getRequestFromContext();

    final String tenant = request == null
      ? ThreadLocalStorage.getTenant()
      : request.getHeader(tenantProperties.getHeaderName());

    if (tenant != null) {
      return tenant;
    }

    // The "force tenant" setting probably should be renamed to "require tenant".
    // This actually requires tenant when header is provided and if there is no tenant provided, then throw an error.
    if (request != null && tenantProperties.isForceTenant()) {
      throw new NoTenantHeaderException("No tenant header on request!");
    }

    return tenantProperties.getDefaultTenant();
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }

  /**
   * Extract HTTP Request from context if it exists.
   *
   * @return The HTTP Request or NULL if it does not exist.
   */
  private HttpServletRequest getRequestFromContext() {

    final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

    return requestAttributes == null
      ? null
      : ((ServletRequestAttributes) requestAttributes).getRequest();
  }

}
