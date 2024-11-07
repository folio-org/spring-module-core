package org.folio.spring.domain.generator;

import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;

import java.io.Serializable;
import java.util.EnumSet;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.id.UUIDGenerator;

/*
public class CustomUUIDGenerator implements BeforeExecutionGenerator {

  private static final long serialVersionUID = -608780582196754676L;

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object, Object currentValue, EventType eventType) {
    return (Serializable) session.getEntityPersister(null, object).getIdentifier(eventType, session);
  }
 */

public class CustomUUIDGenerator extends UUIDGenerator {

  private static final long serialVersionUID = -608780582196754676L;

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object, Object currentValue, EventType eventType) {
    Serializable id = (Serializable) session.getEntityPersister(null, object).getIdentifier(eventType, session);
    return id == null ? (Serializable) super.generate(session, object) : id;
  }

  /*@Override
  public EnumSet<EventType> getEventTypes() {
    return INSERT_ONLY;
  }*/

}
