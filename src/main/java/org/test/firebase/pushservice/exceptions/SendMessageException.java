package org.test.firebase.pushservice.exceptions;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom exception class for errors that occur during message sending operations.
 * Extends {@link RuntimeException} to indicate it's an unchecked exception.
 * Contains details about the Firebase messaging error and appropriate HTTP status code
 */
@Getter
public class SendMessageException extends RuntimeException {
    private final FirebaseMessagingException fbException;
    private final String message;
    private final HttpStatus returnStatusCode;

    /**
     * Constructs a new {@link SendMessageException} with a custom message and HTTP status code.
     *
     * @param message    the detailed error message
     * @param statusCode the HTTP status code for the API response
     */
    public SendMessageException(String message, HttpStatus statusCode) {
        returnStatusCode = statusCode;
        this.message = message;
        fbException = null;
    }

    /**
     * Constructs a new overloaded {@link SendMessageException}
     * with a custom message and originating Firebase exception
     *
     * @param message the detailed error message
     * @param e       the original FirebaseMessagingException that caused this error
     */
    public SendMessageException(String message, FirebaseMessagingException e) {
        this.message = message;
        fbException = e;
        returnStatusCode = null;
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
