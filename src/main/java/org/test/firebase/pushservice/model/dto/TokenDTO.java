package org.test.firebase.pushservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object for Firebase registration tokens.
 * Used for token registration and validation operations.
 */
@Data
public class TokenDTO {
    /**
     * Firebase Cloud Messaging registration token.
     */
    @Schema(description = "Уникальный токен пользователя", example = "dPnEcn3Q373lvm6tynqGxb:APA91bGOSnSLdQ9j9hkoeRAMMx0eOGVVii_VZWHtvleRmydKJsevR47C25949DxP-0ID7KXyi4LkSIdgrxdir4AIo-BAr8MtV-Z4uQJlgF1-QqcuyN-GG6Z", accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "may not be blank")
    private String token;

    /**
     * Unique identifier of the user associated with the token.
     */
    @Schema(description = "Уникальный идентификатор пользователя", example = "124523", accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "may not be blank")
    private String userId;
}
