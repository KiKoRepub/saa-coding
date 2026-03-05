package org.cookpro.exception;

public class SaTokenException extends RuntimeException {

    public SaTokenException(String message) {
        super(message);
    }

    public SaTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
