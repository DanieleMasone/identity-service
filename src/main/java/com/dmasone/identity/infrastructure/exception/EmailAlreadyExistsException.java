package com.dmasone.identity.infrastructure.exception;

/**
 * Raised when a create-user request attempts to reuse an existing email address.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Creates the exception with an API-safe message.
     *
     * @param message explanation returned by the global exception handler
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Creates the exception while retaining the database conflict that caused it.
     *
     * @param message explanation returned by the global exception handler
     * @param cause underlying persistence failure
     */
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
