package org.folio.spring.domain.config;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return JsonMapper.builder()
      .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
      .disable(MapperFeature.REQUIRE_TYPE_ID_FOR_SUBTYPES)
      .disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();
  }
}
