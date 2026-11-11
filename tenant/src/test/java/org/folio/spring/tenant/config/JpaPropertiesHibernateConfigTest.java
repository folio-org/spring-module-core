package org.folio.spring.tenant.config;

import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaPropertiesHibernateConfigTest {

  private static final String DEFAULT_SCHEMA = "defaultSchema";

  @InjectMocks
  private JpaPropertiesHibernateConfig jpaConfig;

  /**
   * Test the defaultSchema getter.
   */
  @Test
  void getSchemaWorksTest() {

    setField(jpaConfig, DEFAULT_SCHEMA, VALUE);

    assertEquals(VALUE, jpaConfig.getDefaultSchema());
  }

  /**
   * Test the defaultSchema setter.
   */
  @Test
  void setSchemaWorksTest() {

    setField(jpaConfig, DEFAULT_SCHEMA, null);

    jpaConfig.setDefaultSchema(VALUE);

    assertEquals(VALUE, getField(jpaConfig, DEFAULT_SCHEMA));
  }

}
