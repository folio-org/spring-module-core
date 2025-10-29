package org.folio.spring.web.resolver;

import static org.folio.spring.test.mock.MockMvcConstant.ACCESS_TOKEN_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.stream.Stream;
import org.folio.spring.web.utility.AnnotationUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class TokenHeaderResolverTest {

  private static final String ACCESS_TOKEN = "access_token";

  private static final Cookie ACCESS_COOKIE = new Cookie(ACCESS_TOKEN_NAME, ACCESS_TOKEN);

  private static final String OKAPI_TOKEN = "okapi_token";

  @Mock
  private MethodParameter parameter;

  @Mock
  private NativeWebRequest nativeWebRequest;

  @Mock
  private HttpServletRequest httpServletRequest;

  private TokenHeaderResolver tokenHeaderResolver;

  @BeforeEach
  void beforeEach() {
    tokenHeaderResolver = new TokenHeaderResolver(ACCESS_TOKEN_NAME, OKAPI_TOKEN);
  }

  @Test
  void supportsParameterReturnsFalseTest() {
    try (MockedStatic<AnnotationUtility> utility = Mockito.mockStatic(AnnotationUtility.class)) {
      utility.when(() -> AnnotationUtility.findMethodAnnotation(any(), any())).thenReturn(null);

      boolean result = tokenHeaderResolver.supportsParameter(parameter);

      assertEquals(false, result);
    }
  }

  @Test
  void supportsParameterReturnsTrueTest() {
    try (MockedStatic<AnnotationUtility> utility = Mockito.mockStatic(AnnotationUtility.class)) {
      utility.when(() -> AnnotationUtility.findMethodAnnotation(any(), any())).thenReturn(new FakeAnnotation());

      boolean result = tokenHeaderResolver.supportsParameter(parameter);

      assertEquals(true, result);
    }
  }

  @ParameterizedTest
  @MethodSource("provideRequestTokens")
  void resolveArgumentWorksTest(Cookie cookie) throws Exception {
    when(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);

    if (cookie == null) {
      when(nativeWebRequest.getHeader(anyString())).thenReturn(OKAPI_TOKEN);
      when(httpServletRequest.getCookies()).thenReturn(null);
    } else {
      when(httpServletRequest.getCookies()).thenReturn(new Cookie[] { cookie });
    }

    Object result = tokenHeaderResolver.resolveArgument(null, null, nativeWebRequest, null);

    assertEquals(cookie == null ? OKAPI_TOKEN : ACCESS_TOKEN, result);
  }

  @Test
  void resolveArgumentWorksWithNullNativeRequestTest() throws Exception {
    when(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(null);
    when(nativeWebRequest.getHeader(anyString())).thenReturn(OKAPI_TOKEN);

    Object result = tokenHeaderResolver.resolveArgument(null, null, nativeWebRequest, null);

    assertEquals(OKAPI_TOKEN, result);
  }

  /**
   * Helper function for parameterized test providing request tokens.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - Cookie cookie (the cookie returned by the HttpServletRequest).
   */
  private static Stream<Arguments> provideRequestTokens() {
    return Stream.of(
      Arguments.of((Cookie) null),
      Arguments.of(ACCESS_COOKIE)
    );
  }

  private class FakeAnnotation implements Annotation {
    @Override
    public Class<? extends Annotation> annotationType() {
      return FakeAnnotation.class;
  }};

}
