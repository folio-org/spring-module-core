package org.folio.spring.tenant.controller.advice;

import static org.folio.spring.test.mock.MockMvcConstant.APP_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.OKAPI_HEAD;
import static org.folio.spring.test.mock.MockMvcRequest.appendBody;
import static org.folio.spring.test.mock.MockMvcRequest.appendHeaders;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.folio.spring.tenant.controller.TenantController;
import org.folio.spring.tenant.exception.TenantAlreadyExistsException;
import org.folio.spring.tenant.exception.TenantDoesNotExistsException;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class TenantControllerAdviceTest {

  private static final String PATH = "/_/tenant";

  @Autowired
  private TenantControllerAdvice tenantControllerAdvice;

  @Autowired
  @Mock
  private TenantController tenantController;

  private MockMvc mvc;

  @BeforeEach
  void beforeEach() {
    mvc = MockMvcBuilders.standaloneSetup(tenantController)
      .setControllerAdvice(tenantControllerAdvice)
      .build();
  }

  @ParameterizedTest
  @MethodSource("provideExceptionsToMatch")
  void exceptionsThrownTest(Exception exception, String simpleName, int status) throws Exception {
    when(tenantController.create(any(), any(), anyString())).thenThrow(exception);

    MockHttpServletRequestBuilder request = appendHeaders(post(PATH), OKAPI_HEAD, APP_JSON, APP_JSON);

    MvcResult result = mvc.perform(appendBody(request, JSON_OBJECT))
      .andDo(log()).andExpect(status().is(status)).andReturn();

    Pattern pattern = Pattern.compile("\"type\":\"" + simpleName + "\"");
    Matcher matcher = pattern.matcher(result.getResponse().getContentAsString());
    assertTrue(matcher.find());
  }

  /**
   * Helper function for parameterized test providing the exceptions to be matched.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - Exception exception.
   *     - String simpleName (exception name to match).
   *     - int status (response HTTP status code for the exception).
   *
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  private static Stream<Arguments> provideExceptionsToMatch() throws NoSuchMethodException, SecurityException {
    return Stream.of(
      Arguments.of(new SQLException(),                   SQLException.class.getSimpleName(),                 500),
      Arguments.of(new IOException(),                    IOException.class.getSimpleName(),                  500),
      Arguments.of(new HibernateException(""),           HibernateException.class.getSimpleName(),           500),
      Arguments.of(new TenantAlreadyExistsException(""), TenantAlreadyExistsException.class.getSimpleName(), 204),
      Arguments.of(new TenantDoesNotExistsException(""), TenantDoesNotExistsException.class.getSimpleName(), 400)
    );
  }
}
