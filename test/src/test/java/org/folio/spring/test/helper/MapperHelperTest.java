package org.folio.spring.test.helper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * The helper class is for testing purpose, so only do basic existence tests.
 */
class MapperHelperTest {

  @Test
  void buildReturnsValidObjectTest() {
    final JsonMapper mapper = MapperHelper.build();

    assertNotNull(mapper);
  }

  @Test
  void constructReturnsValidObjectTest() {
    final JsonMapper.Builder builder = MapperHelper.construct();

    assertNotNull(builder);
  }

  @Test
  void constructReturnedBuilderBuildsTest() {
    final JsonMapper.Builder builder = MapperHelper.construct();

    assertNotNull(builder);

    final JsonMapper mapper = builder.build();

    assertNotNull(mapper);
  }

}
