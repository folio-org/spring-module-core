package org.folio.spring.tenant.service;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Inject tenant on all Spring related HTTP requests.
 *
 * This gets applied after Spring's own filter via `@Order`.
 */
@Component
@Order(1)
public class TenantFilter implements Filter {

  private final TenantProperties tenantProperties;

  /**
   * Initializer.
   *
   * @param tenantProperties The tenant properties.
   */
  TenantFilter(TenantProperties tenantProperties) {
    super();

    this.tenantProperties = tenantProperties;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {

    final HttpServletRequest req = (HttpServletRequest) request;
    final String tenant = req.getHeader(tenantProperties.getHeaderName());

    if (tenant != null && !tenant.isBlank()) {
      ThreadLocalStorage.setTenant(tenant);
    }

    try {
      chain.doFilter(request, response);
    } finally {

      // Must clear at the end because thread pools re-use the thread.
      ThreadLocalStorage.unload();
    }
  }

}
