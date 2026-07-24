package org.folio.spring.domain.config;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

  @Bean
  ObjectMapper objectMapper() {
    return JsonMapper.builder()
      .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
      .enable(MapperFeature.REQUIRE_TYPE_ID_FOR_SUBTYPES)
      .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .addModule(new JavaTimeModule())
      .build();
  }
}
