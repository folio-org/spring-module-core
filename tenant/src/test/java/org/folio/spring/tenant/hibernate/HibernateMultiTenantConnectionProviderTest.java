package org.folio.spring.tenant.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.folio.spring.tenant.service.SchemaService;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HibernateMultiTenantConnectionProviderTest {

  private static final String TENANT = "testTenant";

  private static final String SCHEMA = "testSchema";

  private static final String PLATFORM = "platform";

  private static final String PLATFORM_H2 = "h2";

  private static final String PLATFORM_POSTGRES = "postgres";

  private static final SQLException SQL_EXC = new SQLException("failure");

  @Mock
  private SchemaService schemaService;

  @Mock
  private DataSource dataSource;

  @InjectMocks
  private HibernateMultiTenantConnectionProvider hibernateMultiTenantConnectionProvider;

  @Mock
  private Connection connection;

  @Mock
  private Statement statement;

  @BeforeEach
  void beforeEach() throws SQLException {
    lenient().when(schemaService.getSchema(eq(TENANT))).thenReturn(SCHEMA);
    lenient().when(dataSource.getConnection()).thenReturn(connection);
    lenient().when(connection.createStatement()).thenReturn(statement);

    lenient().doNothing().when(connection).close();
    lenient().doNothing().when(statement).close();
  }

  @Test
  void testGetAnyConnectionWorks() throws SQLException {
    final Connection anyConnection = hibernateMultiTenantConnectionProvider.getAnyConnection();

    assertEquals(connection, anyConnection);

    verify(dataSource).getConnection();
  }

  @Test
  void testGetConnectionWorks() throws SQLException {
    when(statement.execute(any())).thenReturn(Boolean.TRUE);
    setField(hibernateMultiTenantConnectionProvider, PLATFORM, PLATFORM_POSTGRES);

    final Connection getConnection = hibernateMultiTenantConnectionProvider.getConnection(TENANT);

    assertEquals(connection, getConnection);

    verify(dataSource).getConnection();
  }

  @Test
  void testGetConnectionThrowsOnFailedExecution() throws SQLException {
    when(statement.execute(any())).thenThrow(SQL_EXC);
    setField(hibernateMultiTenantConnectionProvider, PLATFORM, PLATFORM_POSTGRES);

    assertThrows(HibernateException.class, () -> {
      hibernateMultiTenantConnectionProvider.getConnection(TENANT);
    });
  }

  @Test
  void testReleaseConnectionWorks() throws SQLException {
    when(statement.execute(any())).thenReturn(Boolean.TRUE);
    setField(hibernateMultiTenantConnectionProvider, PLATFORM, PLATFORM_POSTGRES);

    hibernateMultiTenantConnectionProvider.releaseConnection(TENANT, connection);

    verify(connection).close();
  }

  @Test
  void testReleaseConnectionThrowsOnFailedExecution() throws SQLException {
    when(statement.execute(any())).thenThrow(SQL_EXC);
    setField(hibernateMultiTenantConnectionProvider, PLATFORM, PLATFORM_POSTGRES);

    assertThrows(HibernateException.class, () -> {
      hibernateMultiTenantConnectionProvider.releaseConnection(TENANT, connection);
    });
  }

  @Test
  void testIsUnwrappableAsReturnsFalse() {
    assertEquals(Boolean.FALSE, hibernateMultiTenantConnectionProvider.isUnwrappableAs(Object.class));
  }

  @Test
  void testUnwrapReturnsNull() {
    assertEquals(null, hibernateMultiTenantConnectionProvider.unwrap(Object.class));
  }

  @Test
  void testSupportsAggressiveReleaseReturnsTrue() {
    assertEquals(Boolean.TRUE, hibernateMultiTenantConnectionProvider.supportsAggressiveRelease());
  }

  @Test
  void testConnectionSelectForH2()
    throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
  
    when(statement.execute(any())).thenReturn(Boolean.TRUE);
    setField(hibernateMultiTenantConnectionProvider, PLATFORM, PLATFORM_H2);

    final Method method = HibernateMultiTenantConnectionProvider.class.getDeclaredMethod("connectionPlatformSelectSchema", String.class, Connection.class);
    method.setAccessible(true);
    method.invoke(hibernateMultiTenantConnectionProvider, TENANT, connection);

    verify(statement).execute(argThat(sql -> {
      assertEquals("USE " + SCHEMA, sql);
      return true;
    }));
  }

  @Test
  void testConnectionSelectForPostgres()
    throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
  
    when(statement.execute(any())).thenReturn(Boolean.TRUE);
    setField(hibernateMultiTenantConnectionProvider, PLATFORM, PLATFORM_POSTGRES);

    final Method method = HibernateMultiTenantConnectionProvider.class.getDeclaredMethod("connectionPlatformSelectSchema", String.class, Connection.class);
    method.setAccessible(true);
    method.invoke(hibernateMultiTenantConnectionProvider, TENANT, connection);

    verify(statement).execute(argThat(sql -> {
      assertEquals("SET search_path TO " + SCHEMA + ", public", sql);
      return true;
    }));
  }

  @Test
  void testConnectionSelectThrowsOnUnknownPlatform()
    throws NoSuchMethodException {

    setField(hibernateMultiTenantConnectionProvider, PLATFORM, "UNKNOWN");

    final Method method = HibernateMultiTenantConnectionProvider.class.getDeclaredMethod("connectionPlatformSelectSchema", String.class, Connection.class);
    method.setAccessible(true);

    InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
      method.invoke(hibernateMultiTenantConnectionProvider, TENANT, connection);
    });

    assertInstanceOf(HibernateException.class, exception.getCause());
  }

}
