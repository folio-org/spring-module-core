package org.folio.spring.messaging.config;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.folio.spring.messaging.model.Event;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
@Profile("messaging")
public class KafkaMessageConfig {

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Event> kafkaListenerContainerFactory(
    KafkaProperties kafkaProperties
  ) {
    ConcurrentKafkaListenerContainerFactory<String, Event> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setBatchListener(true);
    factory.setConsumerFactory(jsonNodeConsumerFactory(kafkaProperties));

    return factory;
  }

  private ConsumerFactory<String, Event> jsonNodeConsumerFactory(
    KafkaProperties kafkaProperties
  ) {
    JsonDeserializer<Event> deserializer = new JsonDeserializer<>(Event.class);
    Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    config.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

    return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
  }

}
