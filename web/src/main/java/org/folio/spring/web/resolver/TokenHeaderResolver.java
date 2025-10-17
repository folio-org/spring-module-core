package org.folio.spring.web.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.folio.spring.web.annotation.TokenHeader;
import org.folio.spring.web.utility.AnnotationUtility;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.WebUtils;

public final class TokenHeaderResolver implements HandlerMethodArgumentResolver {

  private final String accessCookieName;

  private final String tenantHeaderName;

  public TokenHeaderResolver(String accessCookieName, String tenantHeaderName) {
    this.accessCookieName = accessCookieName;
    this.tenantHeaderName = tenantHeaderName;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return AnnotationUtility.findMethodAnnotation(TokenHeader.class, parameter) != null;
  }

  // @formatter:off
  @Override
  public Object resolveArgument(
    MethodParameter parameter,
    ModelAndViewContainer mavContainer,
    NativeWebRequest webRequest,
    WebDataBinderFactory binderFactory
  ) throws Exception {
    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    Cookie accessCookie = request == null ? null : WebUtils.getCookie(request, accessCookieName);

    return accessCookie == null ? webRequest.getHeader(tenantHeaderName) : accessCookie.getValue();
  }
  // @formatter:on

}
