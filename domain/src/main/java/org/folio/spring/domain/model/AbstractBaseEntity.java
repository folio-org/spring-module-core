package org.folio.spring.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.folio.spring.domain.annotation.UUIDGenerator;
import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.annotations.UuidGenerator;

@MappedSuperclass
public abstract class AbstractBaseEntity {

  @Id
  //@GeneratedValue(generator = "UUID")
  //@UUIDGenerator
  @UuidGenerator(style = UuidGenerator.Style.RANDOM)
  @Column(updatable = false, nullable = false, insertable = true)
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
