package org.folio.spring.messaging.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { KafkaMessageConfig.class })
class KafkaMessageConfigTest {

  @InjectMocks
  private KafkaMessageConfig kafkaMessageConfig;

  @Mock
  private KafkaProperties kafkaProperties;

  @Test
  void kafkaListenerContainerFactoryTest() {
    Map<String, Object> props = new HashMap<>();
    doReturn(props).when(kafkaProperties).buildConsumerProperties(null);

    assertNotNull(kafkaMessageConfig.kafkaListenerContainerFactory(kafkaProperties));
    verify(kafkaProperties, times(1)).buildConsumerProperties(null);
  }

}
