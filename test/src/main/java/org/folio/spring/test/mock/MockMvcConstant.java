package org.folio.spring.test.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

/**
 * A utility intended to assist in mocking MVC requests during testing, focusing on constants.
 */
public class MockMvcConstant {

  /**
   * Private initializer as per java:S1118.
   */
  private MockMvcConstant() {
  }

  private static final Logger logger = LoggerFactory.getLogger(MockMvcConstant.class);

  /**
   * Provide "application/json" Content-Type.
   */
  public static final String APP_JSON = "application/json";

  /**
   * Provide "application/raml+yaml" Content-Type.
   */
  public static final String APP_RAML = "application/raml+yaml";

  /**
   * Provide "application/schema+json" Content-Type.
   */
  public static final String APP_SCHEMA = "application/schema+json";

  /**
   * Provide "application/*", mainly intended to be used as a wild card in an HTTP Accept header.
   */
  public static final String APP_STAR = "application/*";

  /**
   * Provide "application/octet-stream" Content-Type.
   */
  public static final String APP_STREAM = "application/octet-stream";

  /**
   * Provide the application string.
   */
  public static final String APPLICATION = "application";

  /**
   * Provide generic Boolean for use as a false value.
   */
  public static final Boolean BOOL_FALSE = false;

  /**
   * Provide generic Boolean for use as a true value.
   */
  public static final Boolean BOOL_TRUE = true;

  /**
   * Provide generic string for use as an ID.
   */
  public static final String ID = "id";

  /**
   * Provide generic Integer for use as a value.
   */
  public static final Integer INT_VALUE = 20;

  /**
   * Provide empty JSON array.
   */
  public static final String JSON_ARRAY = "[]";

  /**
   * Provide JSON array with a generic value.
   */
  public static final String JSON_ARRAY_VALUE = "[\"value\"]";

  /**
   * Provide empty JSON object.
   */
  public static final String JSON_OBJECT = "{}";

  /**
   * Provide JSON object with a generic key and value.
   */
  public static final String JSON_OBJECT_VALUE = "{\"key\":\"value\"}";

  /**
   * Provide generic string for use as a key.
   */
  public static final String KEY = "key";

  /**
   * Provide OKAPI Tenant HTTP Header.
   */
  public static final String OKAPI_TENANT = "X-Okapi-Tenant";

  /**
   * Provide OKAPI Token HTTP Header.
   */
  public static final String OKAPI_TOKEN = "X-Okapi-Token";

  /**
   * Provide OKAPI URL HTTP Header.
   */
  public static final String OKAPI_URL = "X-Okapi-Url";

  /**
   * Provide generic Long for use as a value.
   */
  public static final Long LONG_VALUE = 100L;

  /**
   * Provide a NULL string.
   * 
   * This intended to simplify the code of not having (String) null being used in the parameterized tests.
   */
  public static final String NULL_STR = null;

  /**
   * Provide a string representation of "path", generally intended to be used as a query parameter name.
   */
  public static final String PATH = "path";

  /**
   * Provide a string representation part of a URL path, generally intended to be used as a query parameter value for the path query parameter.
   */
  public static final String PATH_PART = "/path";

  /**
   * Provide text to be used as the body of "plain/text" Content-Type responses.
   */
  public static final String PLAIN_BODY = "Plain text.";

  /**
   * Provide an asterisk, mainly intended to be used as a wild card in an HTTP Accept header.
   */
  public static final String STAR = "*";

  /**
   * Provide basic text representing "Success", often used in FOLIO responses.
   */
  public static final String SUCCESS = "Success";

  /**
   * Provide text/other Content-Type.
   *
   * This is intended for testing "text/plain" with or without wild cards like "text/*".
   */
  public static final String TEXT_OTHER = "text/other";

  /**
   * Provide text/plain Content-Type.
   */
  public static final String TEXT_PLAIN = "text/plain";

  /**
   * Provide text/* Content-Type.
   */
  public static final String TEXT_STAR = "text/*";

  /**
   * Provide basic HTTP URL.
   */
  public static final String URL = "http://localhost/";

  /**
   * Provide generic string for use as an UUID.
   */
  public static final String UUID = "aec4faca-8c3d-4b3b-a943-6003636991c0";

  /**
   * Provide generic string for use as a value.
   */
  public static final String VALUE = "value";

  /**
   * Provide the "application/json" Content-Type (media type) (using different name to avoid conflicts).
   */
  public static final MediaType MT_APP_JSON = MediaType.APPLICATION_JSON;

  /**
   * Provide the "application/raml+yaml" Content-Type (media type).
   */
  public static final MediaType MT_APP_RAML = new MediaType(APPLICATION, "raml+yaml");

  /**
   * Provide the "application/schema+json" Content-Type (media type).
   */
  public static final MediaType MT_APP_SCHEMA = new MediaType(APPLICATION, "schema+json");

  /**
   * Provide the "application/*" Content-Type (media type).
   */
  public static final MediaType MT_APP_STAR = new MediaType(APPLICATION, "*");

  /**
   * Provide the "text/plain" Content-Type (media type) (using different name to avoid conflicts).
   */
  public static final MediaType MT_TEXT_PLAIN = MediaType.TEXT_PLAIN;

  /**
   * Provide the null Content-Type (media type) (using different name to avoid conflicts).
   */
  public static final MediaType MT_NULL = null;

  /**
   * Provide a set of HTTP headers containing the OKAPI Tenant, Token, and URL HTTP Headers and values.
   */
  public static final HttpHeaders OKAPI_HEAD = new HttpHeaders(CollectionUtils.toMultiValueMap(Map.of(
    OKAPI_TENANT, List.of(VALUE),
    OKAPI_TOKEN, List.of(VALUE),
    OKAPI_URL, List.of(URL)
  )));

  /**
   * Provide a set of HTTP headers containing the OKAPI Tenant and Token Headers and values.
   */
  public static final HttpHeaders OKAPI_HEAD_NO_URL = new HttpHeaders(CollectionUtils.toMultiValueMap(Map.of(
    OKAPI_TENANT, List.of(VALUE),
    OKAPI_TOKEN, List.of(VALUE)
  )));

  /**
   * Provide a set of HTTP headers containing only the OKAPI Tenant Header and value.
   */
  public static final HttpHeaders OKAPI_HEAD_TENANT = new HttpHeaders(CollectionUtils.toMultiValueMap(Map.of(
    OKAPI_TENANT, List.of(VALUE)
  )));

  /**
   * Provide a set of HTTP headers containing the OKAPI Token Header and values.
   */
  public static final HttpHeaders OKAPI_HEAD_TOKEN = new HttpHeaders(CollectionUtils.toMultiValueMap(Map.of(
    OKAPI_TOKEN, List.of(VALUE)
  )));

  /**
   * Provide a set of HTTP headers containing the OKAPI URL Header and values.
   */
  public static final HttpHeaders OKAPI_HEAD_URL = new HttpHeaders(CollectionUtils.toMultiValueMap(Map.of(
    OKAPI_URL, List.of(URL)
  )));

  /**
   * Provide an empty set of HTTP headers.
   */
  public static final HttpHeaders NO_HEAD = new HttpHeaders();

  /**
   * Provide an empty set of parameters.
   *
   * HttpHeaders provides a cleanly initialized MultiValueMap for use as an empty map.
   */
  public static final MultiValueMap<String, String> NO_PARAM = new HttpHeaders();

  /**
   * Provide a set of parameters containing the path parameter and its values.
   */
  public static final MultiValueMap<String, String> PATH_PARAM = CollectionUtils.toMultiValueMap(Map.of(PATH, List.of(PATH_PART))); 

  /**
   * Provide a set of parameters containing the ID parameter and its values.
   */
  public static final MultiValueMap<String, String> ID_PARAM = CollectionUtils.toMultiValueMap(Map.of(ID, List.of(UUID))); 

  /**
   * Provide a generic list of strings of no importance intended for use as a generic list response.
   */
  public static final List<String> STRING_LIST = new ArrayList<>(List.of(PATH, VALUE));

  /**
   * Provide a generic list of strings of no importance in JSON array format intended for use as a generic list response.
   */
  public static final String STRING_LIST_AS_JSON;

  static {
    String responseAsJson = "";

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      responseAsJson = objectMapper.writeValueAsString(STRING_LIST);
    } catch (JsonProcessingException e) {
      logger.error("Initialization of static string list as JSON failed.", e);
    }

    STRING_LIST_AS_JSON = responseAsJson;
  }
}
