package org.folio.spring.domain.controller;

import static org.folio.spring.web.utility.RequestHeaderUtility.unsupportedAccept;

import java.io.IOException;
import java.util.Optional;
import org.folio.spring.domain.controller.exception.SchemaIOException;
import org.folio.spring.domain.service.JsonSchemasService;
import org.folio.spring.web.utility.RequestHeaderUtility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the JSON Schemas.
 */
@RestController
@RequestMapping("/_/jsonSchemas")
public class JsonSchemasController {

  private static final String CONTENT_TYPE_HEADER = "Content-Type";
  private static final String APPLICATION_SCHEMA_JSON = "application/schema+json";
  private static final String APPLICATION_JSON = "application/json";

  private final JsonSchemasService jsonSchemasService;

  /**
   * Constructor.
   *
   * @param jsonSchemasService The JSON schema service.
   */
  public JsonSchemasController(JsonSchemasService jsonSchemasService) {
      this.jsonSchemasService = jsonSchemasService;
  }

  /**
   * Make a request to OKAPI to get the schemas.
   *
   * @param path The path.
   * @param okapiUrl The OKAPI URL.
   * @param accept The HTTP accept header.
   *
   * @return The HTTP response.
   */
  @GetMapping
  public ResponseEntity<Object> getSchemas(
    @RequestParam(required = false) Optional<String> path,
    @RequestHeader(value = "x-okapi-url", required = true) String okapiUrl,
    @RequestHeader(required = false) String accept
  ) {
    try {
      if (path.isPresent()) {
        if (unsupportedAccept(accept, RequestHeaderUtility.APP_SCHEMA)) {
          return ResponseEntity.status(406).build();
        }

        String schema = jsonSchemasService.getSchemaByPath(path.get(), okapiUrl);
        return ResponseEntity.ok().header(CONTENT_TYPE_HEADER, APPLICATION_SCHEMA_JSON).body(schema);
      } else {
        if (unsupportedAccept(accept, MediaType.APPLICATION_JSON)) {
          return ResponseEntity.status(406).build();
        }

        return ResponseEntity.ok().header(CONTENT_TYPE_HEADER, APPLICATION_JSON).body(jsonSchemasService.getSchemas());
      }
    } catch (IOException e) {
      throw new SchemaIOException("Unable to get JSON Schemas!", e);
    }
  }

}
