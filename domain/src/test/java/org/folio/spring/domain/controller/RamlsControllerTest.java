package org.folio.spring.domain.controller;

import static org.folio.spring.test.mock.MockMvcConstant.APP_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.APP_RAML;
import static org.folio.spring.test.mock.MockMvcConstant.APP_STAR;
import static org.folio.spring.test.mock.MockMvcConstant.APP_STREAM;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_ARRAY;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.MT_APP_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.MT_APP_RAML;
import static org.folio.spring.test.mock.MockMvcConstant.NO_HEAD;
import static org.folio.spring.test.mock.MockMvcConstant.NO_PARAM;
import static org.folio.spring.test.mock.MockMvcConstant.NULL_STR;
import static org.folio.spring.test.mock.MockMvcConstant.OKAPI_HEAD;
import static org.folio.spring.test.mock.MockMvcConstant.OKAPI_HEAD_NO_URL;
import static org.folio.spring.test.mock.MockMvcConstant.PATH_PARAM;
import static org.folio.spring.test.mock.MockMvcConstant.PLAIN_BODY;
import static org.folio.spring.test.mock.MockMvcConstant.STAR;
import static org.folio.spring.test.mock.MockMvcConstant.STRING_LIST;
import static org.folio.spring.test.mock.MockMvcConstant.STRING_LIST_AS_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.TEXT_PLAIN;
import static org.folio.spring.test.mock.MockMvcReflection.DELETE;
import static org.folio.spring.test.mock.MockMvcReflection.PATCH;
import static org.folio.spring.test.mock.MockMvcReflection.POST;
import static org.folio.spring.test.mock.MockMvcReflection.PUT;
import static org.folio.spring.test.mock.MockMvcRequest.appendBody;
import static org.folio.spring.test.mock.MockMvcRequest.appendHeaders;
import static org.folio.spring.test.mock.MockMvcRequest.appendParameters;
import static org.folio.spring.test.mock.MockMvcRequest.buildArguments1;
import static org.folio.spring.test.mock.MockMvcRequest.buildArguments2;
import static org.folio.spring.test.mock.MockMvcRequest.invokeRequestBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.folio.spring.domain.service.RamlsService;
import org.folio.spring.test.mock.MockMvcConstant;
import org.junit.jupiter.api.Test;
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
class RamlsControllerTest {

  private static final String PATH = "/_/ramls";

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private RamlsService ramlsService;

  @ParameterizedTest
  @MethodSource("provideHeadersBodyStatusForGet")
  void getWorksTest(HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    if (parameters.containsKey(MockMvcConstant.PATH)) {
      lenient().when(ramlsService.getRamlByPath(anyString(), anyString())).thenReturn(JSON_ARRAY);
    } else {
      lenient().when(ramlsService.getRamls()).thenReturn(STRING_LIST);
    }

    MockHttpServletRequestBuilder request = appendHeaders(get(PATH), headers, contentType, accept);
    request = appendParameters(request, parameters);

    MvcResult result = mvc.perform(appendBody(request, body))
      .andDo(log()).andExpect(status().is(status)).andReturn();

    if (status == 200) {
      MediaType responseType = MediaType.parseMediaType(result.getResponse().getContentType());

      assertTrue(mediaType.isCompatibleWith(responseType));

      if (parameters.containsKey(MockMvcConstant.PATH)) {
        assertEquals(JSON_ARRAY, result.getResponse().getContentAsString());
      }
      else {
        assertEquals(STRING_LIST_AS_JSON, result.getResponse().getContentAsString());
      }
    }
  }

  @Test
  void getThrowsSchemaIOExceptionTest() throws Exception {
    lenient().when(ramlsService.getRamls()).thenThrow(new IOException("mock exception", null));

    MockHttpServletRequestBuilder request = appendHeaders(get(PATH), OKAPI_HEAD, APP_JSON, APP_JSON);

    MvcResult result = mvc.perform(appendBody(request, JSON_OBJECT))
      .andDo(log()).andExpect(status().is(500)).andReturn();

    MediaType responseType = MediaType.parseMediaType(result.getResponse().getContentType());
    assertTrue(MT_APP_JSON.isCompatibleWith(responseType));

    Pattern pattern = Pattern.compile("\"type\":\"SchemaIOException\"");
    Matcher matcher = pattern.matcher(result.getResponse().getContentAsString());
    assertTrue(matcher.find());
  }

  @ParameterizedTest
  @MethodSource("provideDeletePatchPostPut")
  void createNonPostFailsTest(Method method, HttpHeaders headers, String contentType, String accept, MediaType mediaType, MultiValueMap<String, String> parameters, String body, int status) throws Exception {
    mvc.perform(invokeRequestBuilder(PATH, method, headers, contentType, accept, parameters, body))
      .andDo(log()).andExpect(status().is(status));
  }

  /**
   * Helper function for parameterized test providing DELETE, PATCH, POST, and PUT.
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
  private static Stream<Arguments> provideDeletePatchPostPut() throws NoSuchMethodException, SecurityException {
    String[] contentTypes = { APP_JSON, TEXT_PLAIN, APP_STREAM };
    String[] bodys = { JSON_OBJECT, PLAIN_BODY, JSON_OBJECT };
    String[] accepts = { APP_RAML, APP_JSON, TEXT_PLAIN, APP_STREAM, NULL_STR, APP_STAR, STAR };
    MediaType[] mediaTypes = { MT_APP_JSON, MT_APP_RAML };
    Object[] params = { NO_PARAM, PATH_PARAM };

    Stream<Arguments> stream1 = buildArguments2(DELETE, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    Stream<Arguments> stream2 = buildArguments2(PATCH, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    Stream<Arguments> stream = Stream.concat(stream1, stream2);

    stream1 = buildArguments2(POST, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    stream2 = Stream.concat(stream, stream1);

    stream1 = buildArguments2(PUT, OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 405);
    return Stream.concat(stream2, stream1);
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
  private static Stream<Arguments> provideHeadersBodyStatusForGet() throws NoSuchMethodException, SecurityException {
    Stream<Arguments> stream1 = Stream.of(
      Arguments.of(OKAPI_HEAD, APP_JSON,   APP_RAML,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_JSON,   APP_JSON,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_JSON,   TEXT_PLAIN, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_JSON,   APP_STREAM, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_JSON,   NULL_STR,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_JSON,   APP_STAR,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_JSON,   STAR,       MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, APP_RAML,   MT_APP_JSON, NO_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, APP_JSON,   MT_APP_JSON, NO_PARAM, PLAIN_BODY,  200),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, TEXT_PLAIN, MT_APP_JSON, NO_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, APP_STREAM, MT_APP_JSON, NO_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, NULL_STR,   MT_APP_JSON, NO_PARAM, PLAIN_BODY,  200),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, STAR,       MT_APP_JSON, NO_PARAM, PLAIN_BODY,  200),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, APP_STAR,   MT_APP_JSON, NO_PARAM, PLAIN_BODY,  200),
      Arguments.of(OKAPI_HEAD, APP_STREAM, APP_RAML,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_STREAM, APP_JSON,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_STREAM, TEXT_PLAIN, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_STREAM, APP_STREAM, MT_APP_JSON, NO_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_STREAM, NULL_STR,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_STREAM, APP_STAR,   MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_STREAM, STAR,       MT_APP_JSON, NO_PARAM, JSON_OBJECT, 200),

      Arguments.of(OKAPI_HEAD, APP_JSON,   APP_RAML,   MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_JSON,   APP_JSON,   MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_JSON,   TEXT_PLAIN, MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_JSON,   APP_STREAM, MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_JSON,   NULL_STR,   MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_JSON,   APP_STAR,   MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_JSON,   STAR,       MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, APP_RAML,   MT_APP_RAML, PATH_PARAM, PLAIN_BODY,  200),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, APP_JSON,   MT_APP_RAML, PATH_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, TEXT_PLAIN, MT_APP_RAML, PATH_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, APP_STREAM, MT_APP_RAML, PATH_PARAM, PLAIN_BODY,  406),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, NULL_STR,   MT_APP_RAML, PATH_PARAM, PLAIN_BODY,  200),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, STAR,       MT_APP_RAML, PATH_PARAM, PLAIN_BODY,  200),
      Arguments.of(OKAPI_HEAD, TEXT_PLAIN, APP_STAR,   MT_APP_RAML, PATH_PARAM, PLAIN_BODY,  200),
      Arguments.of(OKAPI_HEAD, APP_STREAM, APP_RAML,   MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_STREAM, APP_JSON,   MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_STREAM, TEXT_PLAIN, MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_STREAM, APP_STREAM, MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 406),
      Arguments.of(OKAPI_HEAD, APP_STREAM, NULL_STR,   MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_STREAM, APP_STAR,   MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 200),
      Arguments.of(OKAPI_HEAD, APP_STREAM, STAR,       MT_APP_RAML, PATH_PARAM, JSON_OBJECT, 200)
    );

    String[] contentTypes = { APP_JSON, TEXT_PLAIN, APP_STREAM };
    String[] bodys = { JSON_OBJECT, PLAIN_BODY, JSON_OBJECT };
    String[] accepts = { APP_RAML, APP_JSON, TEXT_PLAIN, APP_STREAM, NULL_STR, APP_STAR, STAR };
    MediaType[] mediaTypes = { MT_APP_JSON, MT_APP_RAML };
    Object[] params = { NO_PARAM, PATH_PARAM };

    Stream<Arguments> stream2 = buildArguments1(OKAPI_HEAD_NO_URL, contentTypes, accepts, mediaTypes, params, bodys, 400);
    Stream<Arguments> stream = Stream.concat(stream1, stream2);
    stream1 = buildArguments1(NO_HEAD, contentTypes, accepts, mediaTypes, params, bodys, 400);

    return Stream.concat(stream, stream1);
  }

}
