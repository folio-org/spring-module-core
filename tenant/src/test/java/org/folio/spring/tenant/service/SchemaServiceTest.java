package org.folio.spring.tenant.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.folio.spring.tenant.config.JpaPropertiesHibernateConfig;
import org.folio.spring.tenant.properties.BuildInfoProperties;
import org.folio.spring.tenant.properties.TenantProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = { SchemaService.class })
@EnableConfigurationProperties(value = { BuildInfoProperties.class, JpaPropertiesHibernateConfig.class, TenantProperties.class })
@TestPropertySource(properties = {
   "info.build.artifact=mod-Baz",
   "tenant.default-tenant=FooBar321"
 })
class SchemaServiceTest {

  @Autowired
  private SchemaService schemaService;

  @Test
  void validSchema() {
    assertThat(schemaService.getSchema("FooBar123"), is("foobar123_mod_baz"));
  }

  @ParameterizedTest
  @ValueSource(strings = { "ä", "?", "abc+xyz", "a1." })
  void invalidSchema(String tenant) {
    assertThrows(IllegalArgumentException.class, () -> schemaService.getSchema(tenant));
  }

  @Test
  void nullTenant() {
    assertThat(schemaService.getSchema(null), is("foobar321_mod_baz"));
  }

  @Test
  void emptyTenant() {
    assertThat(schemaService.getSchema(""), is("foobar321_mod_baz"));
  }

  @Nested 
  @TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.default_schema=mod-Boom"
  })
  class SchemaServiceWithFolioSchemaTest {
    @Test
    void validSchemaByFolio() {
      assertThat(schemaService.getSchema("FooBar123"), is("foobar123_mod_boom"));
    }
  }

  @Nested 
  @TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.default_schema="
  })
  class SchemaServiceWithEmptyFolioSchemaTest {
    @Test
    void emptySchemaByFolio() {
      assertThat(schemaService.getSchema("FooBar123"), is("foobar123_mod_baz"));
    }
  }
}
