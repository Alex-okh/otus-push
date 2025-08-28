package org.test.firebase.pushservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.test.firebase.pushservice.exceptions.ExceptionBodyMessage;
import org.test.firebase.pushservice.model.dto.MessageDTO;
import org.test.firebase.pushservice.service.SendMessageService;
import java.time.LocalDateTime;

/**
 * REST controller for handling push notification sending operations.
 * Provides endpoint for sending messages via Firebase Cloud Messaging.
 */
@Slf4j
@RestController
@RequestMapping("api/pushservice/v1/")
@Tag(name = "Отправка произвольного сообщения пользователю", description = "Предназначен для отправки сообщения произвольного содержания пользователю с известным ID")
public class SendMessageController {
    private final SendMessageService sendMessageService;

    /**
     * Constructs a new {@link SendMessageController} with required service dependency.
     *
     * @param sendMessageService the service for sending messages
     */
    SendMessageController(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;

    }

    /**
     * Endpoint "/send" for sending push notifications.
     *
     * @param messageDTO the message data transfer object containing:
     *                   userId: recipient identifier,
     *                   messageTitle: notification title,
     *                   messageText: notification text
     * @return ResponseEntity with success status and message
     */
    @Operation(summary = "Отправка сообщения", description = "Отправка сообщения с указанным текстом и заголовкои пользователю с указанным ID. Отправка происходит на все зарегистрированные устройства.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Удачная отправка как минимум на один токен", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SuccesResponse.class))}),

            @ApiResponse(responseCode = "400", description = "Ошибка в запросе или все токены получателя недействительны.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))}),

            @ApiResponse(responseCode = "404", description = "Получатель не найден", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))}),

            @ApiResponse(responseCode = "504", description = "Ошибка внешнего сервиса", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))}),

            @ApiResponse(responseCode = "500", description = "Неизвестная ошибка.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))})})
    @PostMapping("/send")
    public ResponseEntity<SuccesResponse> sendMessage(@Valid @RequestBody MessageDTO messageDTO) {
        log.info("Send message request recieved: {}", messageDTO);
        sendMessageService.send(messageDTO);
        log.info("Send message request succeeded: {}", messageDTO);
        return ResponseEntity.ok(
                new SuccesResponse(HttpStatus.OK.toString(), LocalDateTime.now(), "Message sent successfully"));

    }

}
