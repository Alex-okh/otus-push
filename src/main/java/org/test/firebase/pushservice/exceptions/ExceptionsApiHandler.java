package org.test.firebase.pushservice.exceptions;

import com.google.firebase.messaging.MessagingErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Provides centralized exception handling across all controllers and returns standardized error responses.
 */

@Slf4j
@RestControllerAdvice
public class ExceptionsApiHandler {

    /**
     * Handles validation exceptions for method arguments.
     *
     * @param e the validation exception containing field errors
     * @return ResponseEntity containing error details with HTTP status 400 (BAD_REQUEST)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionBodyMessage> handleException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                               .getFieldErrors()
                               .stream()
                               .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                               .collect(Collectors.joining(", "));
        return new ResponseEntity<>(
                new ExceptionBodyMessage(HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now(), errorMessage),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles exceptions related to message sending operations.
     *
     * @param e the SendMessageException containing error details
     * @return ResponseEntity containing error details with
     * HTTP status NOT_FOUND, GATEWAY_TIMEOUT, BAD_REQUEST
     * and other appropriate status code and error message
     */
    @ExceptionHandler(SendMessageException.class)
    public ResponseEntity<ExceptionBodyMessage> handleException(SendMessageException e) {
        if (e.getFbException() == null || e.getFbException()
                                           .getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
            return new ResponseEntity<>(new ExceptionBodyMessage("404 NOT_FOUND", LocalDateTime.now(), e.getMessage()),
                                        e.getReturnStatusCode());
        }
        return new ResponseEntity<>(new ExceptionBodyMessage(e.getHttpStatus()
                                                              .toString(), LocalDateTime.now(), e.getMessage()),
                                    e.getHttpStatus());
    }

    /**
     * Handles exceptions related to token registration operations.
     *
     * @param e the RegisterTokenException containing error details
     * @return ResponseEntity containing error details with
     * HTTP status 400 (BAD_REQUEST), 409 (CONFLICT), 404 (NOT_FOUND)
     */
    @ExceptionHandler(RegisterTokenException.class)
    public ResponseEntity<ExceptionBodyMessage> handleException(RegisterTokenException e) {
        return new ResponseEntity<>(new ExceptionBodyMessage(e.getHttpStatus()
                                                              .toString(), LocalDateTime.now(), e.getMessage()),
                                    e.getHttpStatus());
    }

    /**
     * Handles exceptions related to all other uncaught exceptions.
     * Logs the full error stack trace.
     *
     * @param e the caught exception
     * @return ResponseEntity with generic error message and HTTP status 500 (INTERNAL_SERVER_ERROR)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionBodyMessage> handleException(Exception e) {
        log.error("Unexpected server error");
        log.error(e.getMessage(), e.getCause());
        log.error(Arrays.toString(e.getStackTrace()));
        return new ResponseEntity<>(new ExceptionBodyMessage("Unexpected error", LocalDateTime.now(), e.getMessage()),
                                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

}


