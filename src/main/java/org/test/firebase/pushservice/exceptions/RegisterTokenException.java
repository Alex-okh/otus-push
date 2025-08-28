package org.test.firebase.pushservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Custom exception class for token registration related errors.
 * Extends {@link RuntimeException} to indicate it's an unchecked exception.
 */
public class RegisterTokenException extends RuntimeException {
    /**
     * HTTP status code to be returned in the API response.
     */
    private final HttpStatus returnStatusCode;

    /**
     * Constructs a new {@link RegisterTokenException} with the specified error message and HTTP status code.
     *
     * @param message          the detail message describing the error
     * @param returnStatusCode the HTTP status code to return for this error
     */
    public RegisterTokenException(String message, HttpStatus returnStatusCode) {
        super(message);
        this.returnStatusCode = returnStatusCode;
    }

    /**
     * Gets the HTTP status code associated with this exception.
     *
     * @return HTTP status code to be returned in the API response
     */
    public HttpStatus getHttpStatus() {
        return returnStatusCode;
    }
}
