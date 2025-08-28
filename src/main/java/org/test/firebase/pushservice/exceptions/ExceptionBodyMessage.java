package org.test.firebase.pushservice.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * * Error response body for API exceptions.
 * Contains details about the error status, timestamp, and message.
 * Used as the response entity body for all handled exceptions in the application.
 */
@Data
@AllArgsConstructor
public class ExceptionBodyMessage {
    /**
     * The HTTP status code and name
     */
    @Schema(name = "status", example = "404 NOT_FOUND")
    private String status;

    /**
     * The timestamp when the error occurred
     */
    @Schema(name = "timestamp", example = "2025-06-17T09:48:26.7327927")
    private LocalDateTime timestamp;

    /**
     * Error message describing the failure
     */
    @Schema(name = "message", example = "No user token found for {userId}")
    private String message;

}
