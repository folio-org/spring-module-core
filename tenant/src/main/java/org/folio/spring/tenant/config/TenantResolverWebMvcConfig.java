package org.folio.spring.tenant.config;

import java.util.List;

import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.resolver.TenantHeaderResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Tenant configuration.
 */
@Configuration
public class TenantResolverWebMvcConfig implements WebMvcConfigurer {

  @Autowired
  private TenantProperties tenantProperties;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new TenantHeaderResolver(tenantProperties.getHeaderName()));
  }

}
