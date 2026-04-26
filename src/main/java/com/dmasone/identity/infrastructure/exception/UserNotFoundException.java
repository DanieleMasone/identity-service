package com.dmasone.identity.infrastructure.exception;

/**
 * Raised when a user lookup cannot resolve the requested identifier.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Creates the exception with an API-safe message.
     *
     * @param message explanation returned by the global exception handler
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
