package org.folio.spring.tenant.controller;

import static org.folio.spring.web.utility.RequestHeaderUtility.unsupportedAccept;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.io.IOException;
import java.sql.SQLException;
import org.folio.spring.tenant.annotation.TenantHeader;
import org.folio.spring.tenant.model.request.TenantAttributes;
import org.folio.spring.tenant.service.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_/tenant")
public class TenantController {

  private static final Logger LOG = LoggerFactory.getLogger(TenantController.class);

  private final SchemaService schemaService;

  /**
   * Initializer.
   *
   * @param schemaService The schema service.
   */
  public TenantController(SchemaService schemaService) {

    this.schemaService = schemaService;
  }

  @PostMapping
  public ResponseEntity<String> create(
    @TenantHeader String tenant,
    @RequestBody @Validated TenantAttributes attributes,
    @RequestHeader(required = false) String accept
  ) throws SQLException, IOException {
    if (unsupportedAccept(accept, MediaType.TEXT_PLAIN)) {
      return ResponseEntity.status(406).build();
    }

    LOG.debug("Creating schema for tenant '{}'.", tenant);

    schemaService.createTenant(tenant);

    return new ResponseEntity<>("Success", CREATED);
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(
    @TenantHeader String tenant,
    @RequestHeader(required = false) String accept
  ) throws SQLException {
    if (unsupportedAccept(accept, MediaType.TEXT_PLAIN)) {
      return ResponseEntity.status(406).build();
    }

    LOG.debug("Deleting schema for tenant '{}'.", tenant);

    schemaService.deleteTenant(tenant);

    return new ResponseEntity<>(NO_CONTENT);
  }

}
