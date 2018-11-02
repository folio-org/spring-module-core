package org.folio.rest.controller.advice;

import org.folio.rest.controller.exception.SchemaIOException;
import org.folio.rest.controller.exception.SchemaNotFoundException;
import org.folio.rest.model.response.Errors;
import org.folio.rest.utility.ErrorUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SchemaAdvice {

  private static final Logger logger = LoggerFactory.getLogger(SchemaAdvice.class);

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(SchemaNotFoundException.class)
  public Errors handleSchemaNotFoundException(SchemaNotFoundException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(SchemaIOException.class)
  public Errors handleSchemaIOException(SchemaIOException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
