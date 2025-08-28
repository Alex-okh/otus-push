package org.test.firebase.pushservice.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Success response format for API endpoints.
 * Contains status information, timestamp, and success message.
 */
@Data
@AllArgsConstructor
public class SuccesResponse {
    /**
     * HTTP status code and name.
     */
    @Schema(name = "status", example = "200 OK")
    private String status;

    /**
     * Timestamp when the response was generated
     */
    @Schema(name = "timestamp", example = "2025-06-17T09:48:26.7327927")
    private LocalDateTime timestamp;

    /**
     * Success message for ResponseEntity
     */
    @Schema(name = "message", example = "Message sent successfully")
    private String message;

}
