package org.folio.rest.tenant.hibernate;

import java.sql.Connection;
import java.sql.SQLException;

public interface HibernateTenantInit {

  public void initialize(Connection connection, String tenant) throws SQLException;

}
