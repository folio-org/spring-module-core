package org.folio.spring.tenant.controller;

import static org.folio.spring.web.utility.RequestHeaderUtility.unsupportedAccept;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.io.IOException;
import java.sql.SQLException;
import org.folio.spring.tenant.annotation.TenantHeader;
import org.folio.spring.tenant.hibernate.HibernateSchemaService;
import org.folio.spring.tenant.model.request.TenantAttributes;
import org.folio.spring.web.utility.RequestHeaderUtility;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private HibernateSchemaService hibernateSchemaService;

  @PostMapping
  public ResponseEntity<String> create(
    @TenantHeader String tenant,
    @RequestBody @Validated TenantAttributes attributes,
    @RequestHeader(value = "accept", required = false) String accept
  ) throws SQLException, IOException {
    if (accept != null && unsupportedAccept(accept, RequestHeaderUtility.TEXT_PLAIN)) {
      return ResponseEntity.status(415).build();
    }

    hibernateSchemaService.createTenant(tenant);
    return new ResponseEntity<String>("Success", CREATED);
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(
    @TenantHeader String tenant,
    @RequestHeader(value = "accept", required = false) String accept
  ) throws SQLException {
    if (accept != null && unsupportedAccept(accept, RequestHeaderUtility.TEXT_PLAIN)) {
      return ResponseEntity.status(415).build();
    }

    hibernateSchemaService.deleteTenant(tenant);
    return new ResponseEntity<Void>(NO_CONTENT);
  }

}
