package org.folio.spring.domain.controller.advice;

import org.folio.spring.web.model.response.ResponseErrors;
import org.folio.spring.web.utility.ErrorUtility;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice for domain.
 */
@RestControllerAdvice
public class DomainAdvice {

  private static final Logger logger = LoggerFactory.getLogger(DomainAdvice.class);

  /**
   * Default initializer.
   */
  public DomainAdvice() {
    // Must exist, even if empty, to comply with standard JavaDocs practices.
  }

  /**
   * Handle ConstraintViolationException.
   *
   * @param exception The exception.
   *
   * @return The constructed HTTP error JSON response.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseErrors handleConstraintViolationException(ConstraintViolationException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle DataIntegrityViolationException.
   *
   * @param exception The exception.
   *
   * @return The constructed HTTP error JSON response.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseErrors handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.BAD_REQUEST);
  }

}
