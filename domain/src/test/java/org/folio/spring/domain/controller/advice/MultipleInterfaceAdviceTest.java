package org.folio.spring.domain.controller.advice;

import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.folio.spring.domain.controller.exception.SchemaIOException;
import org.folio.spring.domain.controller.exception.SchemaNotFoundException;
import org.folio.spring.web.model.response.ResponseErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MultipleInterfaceAdviceTest {

  private static final SchemaNotFoundException SNF_EXC = new SchemaNotFoundException(VALUE);

  private static final SchemaIOException SIO_EXC = new SchemaIOException(VALUE, null);

  private MultipleInterfaceAdvice advice;

  @BeforeEach
  void beforeEach() {
    advice = new MultipleInterfaceAdvice();
  }

  @Test
  void exceptionsThrownForConstraintViolationExceptionTest() {

    final String simpleName = SchemaNotFoundException.class.getSimpleName();
    final ResponseErrors response = advice.handleSchemaNotFoundException(SNF_EXC);

    response.getErrors();
    assertNotNull(response);
    assertNotNull(response.getErrors());

    assertEquals(1, response.getTotalRecords());
    assertEquals(HttpStatus.NOT_FOUND.toString(), response.getErrors().getFirst().getCode());
    assertEquals(simpleName, response.getErrors().getFirst().getType());
  }

  @Test
  void exceptionsThrownForSchemaIOExceptionTest() {

    final String simpleName = SchemaIOException.class.getSimpleName();
    final ResponseErrors response = advice.handleSchemaIOException(SIO_EXC);

    response.getErrors();
    assertNotNull(response);
    assertNotNull(response.getErrors());

    assertEquals(1, response.getTotalRecords());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), response.getErrors().getFirst().getCode());
    assertEquals(simpleName, response.getErrors().getFirst().getType());
  }

}
