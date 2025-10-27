package org.folio.spring.domain.controller;

import static org.folio.spring.web.utility.RequestHeaderUtility.unsupportedAccept;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.folio.spring.domain.controller.exception.SchemaIOException;
import org.folio.spring.domain.service.RamlsService;
import org.folio.spring.web.utility.RequestHeaderUtility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_/ramls")
public class RamlsController {

  private static final String CONTENT_TYPE_HEADER = "Content-Type";
  private static final String APPLICATION_RAML_YAML = "application/raml+yaml";
  private static final String APPLICATION_JSON = "application/json";

  private final RamlsService ramlsService;

  public RamlsController(RamlsService ramlsService) {
    this.ramlsService = ramlsService;
  }

  @GetMapping
  public ResponseEntity<Object> getRamls(
    HttpServletResponse response,
    @RequestParam(value = "path", required = false) Optional<String> path,
    @RequestHeader(value = "x-okapi-url", required = true) String okapiUrl,
    @RequestHeader(value = "accept", required = false) String accept
  ) {
    try {
      if (path.isPresent()) {
        if (unsupportedAccept(accept, RequestHeaderUtility.APP_RAML)) {
          return ResponseEntity.status(406).build();
        }

        return ResponseEntity.ok()
          .header(CONTENT_TYPE_HEADER, APPLICATION_RAML_YAML)
          .body(ramlsService.getRamlByPath(path.get(), okapiUrl));
      } else {
        if (unsupportedAccept(accept, MediaType.APPLICATION_JSON)) {
          return ResponseEntity.status(406).build();
        }

        return ResponseEntity.ok()
          .header(CONTENT_TYPE_HEADER, APPLICATION_JSON)
          .body(ramlsService.getRamls());
      }
    } catch (IOException e) {
      throw new SchemaIOException("Unable to get RAMLs!", e);
    }
  }

}
