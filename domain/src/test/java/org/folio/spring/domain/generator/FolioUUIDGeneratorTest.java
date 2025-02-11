package org.folio.spring.domain.generator;

import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.AccessCallback;
import org.hibernate.id.enhanced.DatabaseStructure;
import org.hibernate.id.enhanced.Optimizer;
import org.hibernate.internal.SessionImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class FolioUUIDGeneratorTest {

  @Mock
  private AccessCallback accessCallback;

  @Mock
  private DatabaseStructure databaseStructure;

  @Mock
  private Optimizer optimizer;

  @Mock
  private SessionImpl session;

  @Mock
  private EntityPersister persister;

  @InjectMocks
  private FolioUUIDGenerator folioUUIDGenerator;

  @BeforeEach
  void beforeEach() {
    lenient().when(session.getEntityPersister(any(), any())).thenReturn(persister);
  }

  @Test
  void generateWorksTest() {
    when(persister.getIdentifier(any(), any(SharedSessionContractImplementor.class))).thenReturn(UUID);

    Object result = folioUUIDGenerator.generate(session, folioUUIDGenerator);

    assertEquals(UUID, result);
  }

  @Test
  void generateWorksWithNullIdentifierTest() {
    when(persister.getIdentifier(any(), any(SharedSessionContractImplementor.class))).thenReturn(null);
    when(databaseStructure.buildCallback(any())).thenReturn(accessCallback);
    when(optimizer.generate(any())).thenReturn(JSON_OBJECT);

    ReflectionTestUtils.setField(folioUUIDGenerator, "databaseStructure", databaseStructure);
    ReflectionTestUtils.setField(folioUUIDGenerator, "optimizer", optimizer);

    Object result = folioUUIDGenerator.generate(session, folioUUIDGenerator);

    assertEquals(JSON_OBJECT, result);
  }

  @Test
  void allowAssignedIdentifiersWorksTest() {
    assertTrue(folioUUIDGenerator.allowAssignedIdentifiers());
  }
}
