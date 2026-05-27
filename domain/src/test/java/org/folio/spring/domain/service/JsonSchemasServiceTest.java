package org.folio.spring.domain.service;

import static org.folio.spring.test.mock.MockMvcConstant.PATH;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.folio.spring.domain.controller.exception.SchemaNotFoundException;
import org.folio.spring.test.helper.MapperHelper;
import org.folio.spring.test.mock.MockMvcConstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class JsonSchemasServiceTest {

  @Mock
  private ResourcePatternResolver resolver;

  @Mock
  private Resource resource;

  @InjectMocks
  private JsonSchemasService jsonSchemasService;

  private JsonMapper mapper;

  @BeforeEach
  void beforeEach() {
    mapper = MapperHelper.build();

    jsonSchemasService = new JsonSchemasService(resolver, mapper);
  }

  @Test
  void getSchemaWorksTest() throws IOException {
    Resource[] resources = { resource };
    jsonSchemasService = new JsonSchemasService(resolver, mapper);

    when(resource.getFilename()).thenReturn(VALUE);
    when(resolver.getResources(anyString())).thenReturn(resources);

    List<String> response = jsonSchemasService.getSchemas();
    assertTrue(response.contains(VALUE));
  }

  @Test
  void getSchemaByPathWorksTest() throws IOException {
    InputStream ramlIn = JsonSchemasServiceTest.class.getResourceAsStream("/ramls/exampletask-input.json");
    String raml = readFile("/ramls/exampletask-expect.json");
    JsonNode expectRaml = mapper.readValue(raml, JsonNode.class);

    when(resource.exists()).thenReturn(true);
    when(resource.getInputStream()).thenReturn(ramlIn);
    when(resolver.getResource(anyString())).thenReturn(resource);

    String response = jsonSchemasService.getSchemaByPath(PATH, MockMvcConstant.URL);
    assertEquals(expectRaml.toString(), response);
  }

  @Test
  void getSchemaByPathThrowsSchemaNotFoundExceptionTest() {
    when(resource.exists()).thenReturn(false);
    when(resolver.getResource(anyString())).thenReturn(resource);

    Assertions.assertThrows(SchemaNotFoundException.class, () -> {
      jsonSchemasService.getSchemaByPath(PATH, MockMvcConstant.URL);
    });
  }

  /**
   * Open file at path and read contents.
   *
   * @param path The path to the file.
   *
   * @return
   *   The read string or an empty string.
   *
   * @throws IOException On error when reading file.
   */
  private static String readFile(String path) throws IOException {
    URL url = JsonSchemasServiceTest.class.getResource(path);
    File file = new File(url.getFile());
    return FileUtils.readFileToString(file, "UTF-8");
  }

}
