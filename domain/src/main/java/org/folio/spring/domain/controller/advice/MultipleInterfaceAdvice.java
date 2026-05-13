package org.folio.spring.domain.controller.advice;

import org.folio.spring.domain.controller.exception.SchemaIOException;
import org.folio.spring.domain.controller.exception.SchemaNotFoundException;
import org.folio.spring.web.model.response.ResponseErrors;
import org.folio.spring.web.utility.ErrorUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller advice for multiple interface.
 */
@RestControllerAdvice
public class MultipleInterfaceAdvice {

  private static final Logger logger = LoggerFactory.getLogger(MultipleInterfaceAdvice.class);

  /**
   * Default initializer.
   */
  public MultipleInterfaceAdvice() {
  }

  /**
   * Handle SchemaNotFoundException.
   *
   * @param exception The exception.
   *
   * @return The constructed HTTP error JSON response.
   */
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(SchemaNotFoundException.class)
  public ResponseErrors handleSchemaNotFoundException(SchemaNotFoundException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.NOT_FOUND);
  }

  /**
   * Handle SchemaIOException.
   *
   * @param exception The exception.
   *
   * @return The constructed HTTP error JSON response.
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(SchemaIOException.class)
  public ResponseErrors handleSchemaIOException(SchemaIOException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
