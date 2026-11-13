package org.folio.spring.tenant.storage;

/**
 * Provide thread-specific storage information, such as tenant.
 */
public class ThreadLocalStorage {

  private static final ThreadLocal<String> tenant = new ThreadLocal<>();

  /**
   * Constructor.
   */
  private ThreadLocalStorage() {
    // Do nothing.
  }

  /**
   * Set the tenant.
   *
   * @param tenantId The new value.
   */
  public static void setTenant(String tenantId) {
    tenant.set(tenantId);
  }

  /**
   * Get the tenant.
   *
   * @return The value.
   */
  public static String getTenant() {
    return tenant.get();
  }

  /**
   * Remove the tenant value for the current thread.
   */
  public static void unload() {
    tenant.remove();
  }

}
