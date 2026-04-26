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
}
