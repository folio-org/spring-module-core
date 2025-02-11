package org.folio.spring.tenant.controller;

import static org.folio.spring.test.mock.MockMvcConstant.APP_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.APP_STREAM;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.MT_APP_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.MT_TEXT_PLAIN;
import static org.folio.spring.test.mock.MockMvcConstant.NO_PARAM;
import static org.folio.spring.test.mock.MockMvcConstant.NULL_STR;
import static org.folio.spring.test.mock.MockMvcConstant.OKAPI_HEAD_NO_URL;
import static org.folio.spring.test.mock.MockMvcConstant.PLAIN_BODY;
import static org.folio.spring.test.mock.MockMvcConstant.STAR;
import static org.folio.spring.test.mock.MockMvcConstant.SUCCESS;
import static org.folio.spring.test.mock.MockMvcConstant.TEXT_OTHER;
import static org.folio.spring.test.mock.MockMvcConstant.TEXT_PLAIN;
import static org.folio.spring.test.mock.MockMvcConstant.TEXT_STAR;
import static org.folio.spring.test.mock.MockMvcReflection.GET;
import static org.folio.spring.test.mock.MockMvcReflection.PATCH;
import static org.folio.spring.test.mock.MockMvcReflection.PUT;
import static org.folio.spring.test.mock.MockMvcRequest.appendBody;
import static org.folio.spring.test.mock.MockMvcRequest.appendHeaders;
import static org.folio.spring.test.mock.MockMvcRequest.buildArguments1;
import static org.folio.spring.test.mock.MockMvcRequest.buildArguments2;
import static org.folio.spring.test.mock.MockMvcRequest.invokeRequestBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.folio.spring.tenant.hibernate.HibernateSchemaService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

  private static final String PATH = "/_/tenant";

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private HibernateSchemaService hibernateSchemaService;

  @ParameterizedTest
  @MethodSource("provideHeadersBodyStatusForCreate")
  void createPostWorksTest(HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    lenient().doNothing().when(hibernateSchemaService).createTenant(anyString());

    MockHttpServletRequestBuilder request = appendHeaders(post(PATH), headers, contentType, accept);

    MvcResult result = mvc.perform(appendBody(request, body))
      .andDo(log()).andExpect(status().is(status)).andReturn();

    if (status == 201) {
      MediaType responseType = MediaType.parseMediaType(result.getResponse().getContentType());
 
      assertTrue(mediaType.isCompatibleWith(responseType));
      assertEquals(SUCCESS, result.getResponse().getContentAsString());
    }
  }

  @ParameterizedTest
  @MethodSource("provideGetPatchPut")
  void createNonPostFailsTest(Method method, HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    mvc.perform(invokeRequestBuilder(PATH, method, headers, contentType, accept, parameters, body))
    .andDo(log()).andExpect(status().is(status));
  }

  @ParameterizedTest
  @MethodSource("provideHeadersBodyStatusForDelete")
  void deletePostWorksTest(HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    lenient().doNothing().when(hibernateSchemaService).createTenant(anyString());

    MockHttpServletRequestBuilder request = appendHeaders(delete(PATH), headers, contentType, accept);

    mvc.perform(appendBody(request, body))
      .andDo(log()).andExpect(status().is(status));
  }

  /**
   * Helper function for parameterized test providing GET, PATCH, and PUT.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - Method The (reflection) request method.
   *     - HttpHeaders headers.
   *     - String contentType (Content-Type request).
   *     - String accept (ask for this Content-Type on response).
   *       String mediaType (response Content-Type).
   *     - MultiValueMap<String, String> parameters.
   *     - String body (request body).
   *     - int status (response HTTP status code).
   *
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  private static Stream<Arguments> provideGetPatchPut() throws NoSuchMethodException, SecurityException {
    String[] contentTypes = { APP_STREAM, TEXT_PLAIN, TEXT_OTHER };
    String[] bodys = { JSON_OBJECT, PLAIN_BODY, TEXT_PLAIN };
    String[] accepts = { APP_JSON, TEXT_OTHER, TEXT_PLAIN, NULL_STR, TEXT_STAR, STAR };
    MediaType[] mediaTypes = { MT_APP_JSON, MT_TEXT_PLAIN };
    Object[] params = { NO_PARAM };

    Stream<Arguments> stream1 = buildArguments2(GET, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    Stream<Arguments> stream2 = buildArguments2(PATCH, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    Stream<Arguments> stream = Stream.concat(stream1, stream2);

    stream1 = buildArguments2(PUT, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    return Stream.concat(stream, stream1);
  }

  /**
   * Helper function for parameterized test providing tests with headers, body, and status for create end points.
   *
   * This is intended to be used for when the correct HTTP method is being used in the request.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - HttpHeaders headers.
   *     - String contentType (Content-Type request).
   *     - String accept (ask for this Content-Type on response).
   *       String mediaType (response Content-Type).
   *     - MultiValueMap<String, String> parameters.
   *     - String body (request body).
   *     - int status (response HTTP status code).
   *
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  private static Stream<Arguments> provideHeadersBodyStatusForCreate() throws NoSuchMethodException, SecurityException {
    Stream<Arguments> stream1 = Stream.of(
      Arguments.of(OKAPI_HEAD_NO_URL, APP_JSON, APP_JSON,   MT_TEXT_PLAIN, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD_NO_URL, APP_JSON, TEXT_OTHER, MT_TEXT_PLAIN, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD_NO_URL, APP_JSON, TEXT_PLAIN, MT_TEXT_PLAIN, NO_PARAM, JSON_OBJECT, 201),
      Arguments.of(OKAPI_HEAD_NO_URL, APP_JSON, NULL_STR,   MT_TEXT_PLAIN, NO_PARAM, JSON_OBJECT, 201),
      Arguments.of(OKAPI_HEAD_NO_URL, APP_JSON, TEXT_STAR,  MT_TEXT_PLAIN, NO_PARAM, JSON_OBJECT, 201),
      Arguments.of(OKAPI_HEAD_NO_URL, APP_JSON, STAR,       MT_TEXT_PLAIN, NO_PARAM, JSON_OBJECT, 201)
    );

    String[] contentTypes = { APP_STREAM, TEXT_PLAIN, TEXT_OTHER };
    String[] bodys = { JSON_OBJECT, PLAIN_BODY, TEXT_PLAIN };
    String[] accepts = { APP_JSON, TEXT_OTHER, TEXT_PLAIN, NULL_STR, TEXT_STAR, STAR };
    MediaType[] mediaTypes = { MT_APP_JSON, MT_TEXT_PLAIN };
    Object[] params = { NO_PARAM };

    Stream<Arguments> stream2 = buildArguments1(OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 415);
    return Stream.concat(stream1, stream2);
  }

  /**
   * Helper function for parameterized test providing tests with headers, body, and status for delete end points.
   *
   * This is intended to be used for when the correct HTTP method is being used in the request.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - HttpHeaders headers.
   *     - String contentType (Content-Type request).
   *     - String accept (ask for this Content-Type on response).
   *       String mediaType (response Content-Type).
   *     - MultiValueMap<String, String> parameters.
   *     - String body (request body).
   *     - int status (response HTTP status code).
   *
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  private static Stream<Arguments> provideHeadersBodyStatusForDelete() throws NoSuchMethodException, SecurityException {
    String[] contentTypes = { APP_STREAM, TEXT_PLAIN, TEXT_OTHER };
    String[] bodys = { JSON_OBJECT, PLAIN_BODY, TEXT_PLAIN };
    String[] acceptsInvalid = { APP_JSON, TEXT_OTHER };
    String[] acceptsValid = { TEXT_PLAIN, NULL_STR, TEXT_STAR, STAR };
    MediaType[] mediaTypes = { MT_APP_JSON, MT_TEXT_PLAIN };
    Object[] params = { NO_PARAM };

    Stream<Arguments> stream1 = buildArguments1(OKAPI_HEAD_NO_URL, contentTypes, acceptsInvalid, mediaTypes, params, bodys, 406);
    Stream<Arguments> stream2 = buildArguments1(OKAPI_HEAD_NO_URL, contentTypes, acceptsValid, mediaTypes, params, bodys, 204);

    return Stream.concat(stream1, stream2);
  }

}
