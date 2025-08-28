package org.test.firebase.pushservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object for sending push notifications.
 * Contains all necessary information to construct and send a Firebase Cloud Message.
 */
@Data
public class MessageDTO {
    /**
     * Unique identifier of the user who should receive the message.
     */
    @Schema(description = "Уникальный идентификатор пользователя", example = "12345", accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "may not be blank")
    private String userId;

    /**
     * Title of the push notification.
     */
    @Schema(description = "Заголовок уведомления", example = "ВАЖНО!", accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "may not be blank")
    private String messageTitle;

    /**
     * Text of push notification message.
     */
    @Schema(description = "Текст уведомления", example = "В ваш аккаунт произведен успешный вход.", accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "may not be blank")
    private String messageText;
}
