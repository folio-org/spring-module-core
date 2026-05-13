package org.folio.spring.domain.controller.exception;

/**
 * Schema not found exception.
 */
public class SchemaNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -3475304055299328175L;

  /**
   * Constructor.
   *
   * @param message The exception.
   */
  public SchemaNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message The exception.
   * @param cause The cause.
   */
  public SchemaNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
