package org.folio.spring.tenant.interfaces;

import java.sql.SQLException;

/**
 * Provide a custom database provisioning service.
 *
 * Implementations of this are intended to be called by TenantProvisioningService.
 */
public interface TenantDatabaseProvisioner {

  /**
   * Provision a schema using the provided connection.
   *
   * @param schema The schema name.
   * @param tenant The tenant name.
   *
   * @throws SQLException On SQL error.
   */
  public void provisionSchema(String schema, String tenant) throws SQLException;
}
