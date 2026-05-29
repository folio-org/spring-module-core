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
class FolioSqlConfigTest {

  private static final String SCHEMA = "schema";

  @InjectMocks
  private FolioConfig folioConfig;

  /**
   * Test the schema getter.
   */
  @Test
  void getSchemaWorksTest() {

    setField(folioConfig.getSql(), SCHEMA, VALUE);

    assertEquals(VALUE, folioConfig.getSql().getSchema());
  }

  /**
   * Test the schema setter.
   */
  @Test
  void setSchemaWorksTest() {

    setField(folioConfig.getSql(), SCHEMA, null);

    folioConfig.getSql().setSchema(VALUE);

    assertEquals(VALUE, getField(folioConfig.getSql(), SCHEMA));
  }

}
