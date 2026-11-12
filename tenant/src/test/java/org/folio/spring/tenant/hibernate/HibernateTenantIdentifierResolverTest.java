package org.folio.spring.tenant.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.withSettings;
import static org.mockito.quality.Strictness.LENIENT;

import org.folio.spring.tenant.exception.NoTenantHeaderException;
import org.folio.spring.tenant.properties.TenantProperties;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class HibernateTenantIdentifierResolverTest {

  private static final String TENANT = "testTenant";

  private static final String TENANT_DEFAULT = "defaultTenant";

  private static final String TENANT_HEADER_NAME = "X-Okapi-Tenant";

  private MockedStatic<ThreadLocalStorage> threadLocalStorage;

  private MockedStatic<RequestContextHolder> requestContextHolder;

  @Mock
  private TenantProperties tenantProperties;

  @InjectMocks
  private HibernateTenantIdentifierResolver tenantIdentifierResolver;

  @Mock
  private ServletRequestAttributes servletRequestAttributes;

  @Mock
  private HttpServletRequest httpServletRequest;

  @BeforeEach
  void beforeEach() {
    threadLocalStorage = mockStatic(ThreadLocalStorage.class, withSettings().strictness(LENIENT));
    requestContextHolder = mockStatic(RequestContextHolder.class, withSettings().strictness(LENIENT));

    lenient().when(tenantProperties.getHeaderName()).thenReturn(TENANT_HEADER_NAME);
    lenient().when(tenantProperties.getDefaultTenant()).thenReturn(TENANT_DEFAULT);
  }

  @AfterEach
  void afterEach() {
    threadLocalStorage.close();
    requestContextHolder.close();
  }

  @Test
  void testUnloadWorks() {
    threadLocalStorage.when(ThreadLocalStorage::unload).then(invocation -> null);

    tenantIdentifierResolver.unload();

    threadLocalStorage.verify(ThreadLocalStorage::unload);
  }

  @Test
  void testResolveWorksWithValidRequest() {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
    lenient().when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
    lenient().when(httpServletRequest.getHeader(eq(TENANT_HEADER_NAME))).thenReturn(TENANT);

    final String tenant = tenantIdentifierResolver.resolveCurrentTenantIdentifier();

    assertEquals(TENANT, tenant);
  }

  @Test
  void testValidRequestWithNullTenantReturnsDefaultWhenNoForce() {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
    lenient().when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
    lenient().when(httpServletRequest.getHeader(eq(TENANT_HEADER_NAME))).thenReturn(null);

    lenient().when(tenantProperties.isForceTenant()).thenReturn(Boolean.FALSE);

    final String tenant = tenantIdentifierResolver.resolveCurrentTenantIdentifier();

    assertEquals(TENANT_DEFAULT, tenant);
  }

  @Test
  void testValidRequestWithNullTenantThrowsErrorOnForce() {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
    lenient().when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
    lenient().when(httpServletRequest.getHeader(eq(TENANT_HEADER_NAME))).thenReturn(null);

    lenient().when(tenantProperties.isForceTenant()).thenReturn(Boolean.TRUE);

    assertThrows(NoTenantHeaderException.class, () -> {
      tenantIdentifierResolver.resolveCurrentTenantIdentifier();
    });
  }

  @Test
  void testResolveWorksWithInvalidRequestAndValidStorage() {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
    lenient().when(servletRequestAttributes.getRequest()).thenReturn(null);

    threadLocalStorage.when(ThreadLocalStorage::getTenant).thenReturn(TENANT);

    final String tenant = tenantIdentifierResolver.resolveCurrentTenantIdentifier();

    assertEquals(TENANT, tenant);
  }

  @Test
  void testResolveWorksWithInvalidRequestReturnsDefaultWithInvalidStorage() {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
    lenient().when(servletRequestAttributes.getRequest()).thenReturn(null);

    threadLocalStorage.when(ThreadLocalStorage::getTenant).thenReturn(null);

    final String tenant = tenantIdentifierResolver.resolveCurrentTenantIdentifier();

    assertEquals(TENANT_DEFAULT, tenant);
  }

  @Test
  void testResolveWorksWithInvalidRequestAttributesAndValidStorage() {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

    threadLocalStorage.when(ThreadLocalStorage::getTenant).thenReturn(TENANT);

    final String tenant = tenantIdentifierResolver.resolveCurrentTenantIdentifier();

    assertEquals(TENANT, tenant);
  }

  @Test
  void testValidateExistingCurrentSessionsReturnsTrue() {
    assertEquals(Boolean.TRUE, tenantIdentifierResolver.validateExistingCurrentSessions());
  }


}
