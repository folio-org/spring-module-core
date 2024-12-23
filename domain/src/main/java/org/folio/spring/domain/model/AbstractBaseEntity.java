package org.folio.spring.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.folio.spring.domain.annotation.FolioUUIDGeneratorType;

@MappedSuperclass
public abstract class AbstractBaseEntity {

  @Id
  @FolioUUIDGeneratorType
  @Column(updatable = false, nullable = false, insertable = true)
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
