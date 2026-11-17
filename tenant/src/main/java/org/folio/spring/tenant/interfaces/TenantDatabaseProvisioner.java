package org.folio.spring.tenant.interfaces;

import java.sql.Connection;
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
   * @param schema     The schema name.
   * @param tenant     The tenant name.
   * @param connection The connection to use.
   *
   * @throws SQLException On SQL error.
   */
  public void provisionSchema(String schema, String tenant, Connection connection) throws SQLException;
}
