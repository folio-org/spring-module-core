package org.folio.spring.tenant.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.folio.spring.tenant.config.FolioConfig.FolioSqlConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FolioConfigTest {

  private static final String SQL = "sql";

  @InjectMocks
  private FolioConfig folioConfig;

  @Mock
  private FolioSqlConfig folioSqlConfig;

  /**
   * Test the SQL getter.
   */
  @Test
  void getSqlWorksTest() {

    setField(folioConfig, SQL, folioSqlConfig);

    assertEquals(folioConfig.getSql(), folioSqlConfig);
  }

  /**
   * Test the SQL setter.
   */
  @Test
  void setSqlWorksTest() {

    setField(folioConfig, SQL, null);

    folioConfig.setSql(folioSqlConfig);

    assertEquals(getField(folioConfig, SQL), folioSqlConfig);
  }

}
