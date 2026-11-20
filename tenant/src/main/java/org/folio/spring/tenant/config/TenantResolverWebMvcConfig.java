package org.folio.spring.tenant.config;

import java.util.List;
import org.folio.spring.tenant.resolver.TenantHeaderResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Tenant configuration.
 */
@Configuration
public class TenantResolverWebMvcConfig implements WebMvcConfigurer {

  private final TenantConfig tenantConfig;

  /**
   * Initializer.
   *
   * @param tenantConfig The tenant configuration.
   */
  public TenantResolverWebMvcConfig(TenantConfig tenantConfig) {

    this.tenantConfig = tenantConfig;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new TenantHeaderResolver(tenantConfig.getHeaderName()));
  }

}
