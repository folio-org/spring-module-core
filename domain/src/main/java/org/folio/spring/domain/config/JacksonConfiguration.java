package org.folio.spring.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson Confoguration.
 */
@Configuration
public class JacksonConfiguration {

  /**
   * Default constructor.
   */
  public JacksonConfiguration() {
  }

  @Bean
  ObjectMapper objectMapper() {
    return JsonMapper.builder()
      .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
      .enable(MapperFeature.REQUIRE_TYPE_ID_FOR_SUBTYPES)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();
  }
}
