package org.folio.spring.tenant.properties;

import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The build information properties.
 */
@ConfigurationProperties(prefix = "info.build")
public class BuildInfoProperties implements Serializable {

  private static final long serialVersionUID = -7137553746347037879L;

  private String artifact;

  private String description;

  private String name;

  private String version;

  /**
   * Initializer.
   */
  public BuildInfoProperties() {
    // Do nothing.
  }

  /**
   * Get the artifact.
   *
   * @return The value.
   */
  public String getArtifact() {
    return artifact;
  }

  /**
   * Get the name.
   *
   * @return The value.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the description.
   *
   * @return The value.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the version.
   *
   * @return The value.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Set the artifact.
   *
   * @param artifact The new value.
   */
  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  /**
   * Set the name.
   *
   * @param name The new value.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Set the description.
   *
   * @param description The new value.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Set the version.
   *
   * @param version The new value.
   */
  public void setVersion(String version) {
    this.version = version;
  }

}
