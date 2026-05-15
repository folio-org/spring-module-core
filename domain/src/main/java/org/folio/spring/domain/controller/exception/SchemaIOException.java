package org.folio.spring.domain.controller.exception;

/**
 * Schema I/O exception.
 */
public class SchemaIOException extends RuntimeException {

  private static final long serialVersionUID = -8336467594353513798L;

  /**
   * Constructor.
   *
   * @param message The exception.
   * @param cause The cause.
   */
  public SchemaIOException(String message, Throwable cause) {
    super(message, cause);
  }

}
