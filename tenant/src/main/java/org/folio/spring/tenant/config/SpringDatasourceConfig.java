package org.folio.spring.tenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Helper configuration for accessing 'spring.datasource' derived values.
 *
 * These values are provided by Spring but these values aren't available in this form, thus this class.
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class SpringDatasourceConfig {

  private String driverClassName;

  private String password;

  private String url;

  private String username;

  /**
   * Get driver class name.
   *
   * @return The value.
   */
  public String getDriverClassName() {
    return driverClassName;
  }

  /**
   * Get pass word.
   *
   * @return The value.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Get User name.
   *
   * @return The value.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Get URL.
   *
   * @return The value.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set driver class name.
   *
   * @param driverClassName The new value.
   */
  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  /**
   * Set URL.
   *
   * @param url The new value.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Set user name.
   *
   * @param username The new value.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Set pass word.
   *
   * @param password The new value.
   */
  public void setPassword(String password) {
    this.password = password;
  }

}
