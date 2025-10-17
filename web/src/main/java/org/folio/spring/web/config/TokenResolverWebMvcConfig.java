package org.folio.spring.web.config;

import java.util.List;
import org.folio.spring.web.resolver.TokenHeaderResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TokenResolverWebMvcConfig implements WebMvcConfigurer {

  @Value("${okapi.auth.accessCookieName:folioAccessToken}")
  private String accessCookieName;

  @Value("${okapi.auth.tokenHeaderName:X-Okapi-Token}")
  private String tokenHeaderName;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new TokenHeaderResolver(accessCookieName, tokenHeaderName));
  }

}
