package org.folio.spring.domain.controller.advice;

import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.folio.spring.web.model.response.ResponseErrors;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class DomainAdviceTest {

  private static final ConstraintViolationException CV_EXC = new ConstraintViolationException(VALUE, null, VALUE);

  private static final DataIntegrityViolationException DIV_EXC = new DataIntegrityViolationException(VALUE);

  private DomainAdvice advice;

  @BeforeEach
  void beforeEach() {
    advice = new DomainAdvice();
  }

  @Test
  void handlesConstraintViolationExceptionTest() {

    final String simpleName = ConstraintViolationException.class.getSimpleName();
    final ResponseErrors response = advice.handleConstraintViolationException(CV_EXC);

    response.getErrors();
    assertNotNull(response);
    assertNotNull(response.getErrors());

    assertEquals(1, response.getTotalRecords());
    assertEquals(HttpStatus.BAD_REQUEST.toString(), response.getErrors().getFirst().getCode());
    assertEquals(simpleName, response.getErrors().getFirst().getType());
  }

  @Test
  void handlesDataIntegrityViolationExceptionTest() {

    final String simpleName = DataIntegrityViolationException.class.getSimpleName();
    final ResponseErrors response = advice.handleDataIntegrityViolationException(DIV_EXC);

    response.getErrors();
    assertNotNull(response);
    assertNotNull(response.getErrors());

    assertEquals(1, response.getTotalRecords());
    assertEquals(HttpStatus.BAD_REQUEST.toString(), response.getErrors().getFirst().getCode());
    assertEquals(simpleName, response.getErrors().getFirst().getType());
  }

}
