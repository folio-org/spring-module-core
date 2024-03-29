package org.folio.spring.domain.generator;

import java.io.Serializable;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDGenerator;

public class CustomUUIDGenerator extends UUIDGenerator {

  private static final long serialVersionUID = -608780582196754676L;

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object) {
    Serializable id =
        (Serializable) session.getEntityPersister(null, object).getClassMetadata().getIdentifier(object, session);
    return id != null ? id : (Serializable) super.generate(session, object);
  }

}
