package org.folio.spring.domain.generator;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

public class FolioUUIDGenerator extends SequenceStyleGenerator {

  private static final long serialVersionUID = -608780582196754676L;

  @Override
  public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
    Object id = session.getEntityPersister(null, object).getIdentifier(object, session);
    return id == null ? super.generate(session, object) : id;
  }

  @Override
  public boolean allowAssignedIdentifiers() {
    return true;
  }
}
