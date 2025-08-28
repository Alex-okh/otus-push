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
import org.springframework.web.bind.annotation.*;
import org.test.firebase.pushservice.exceptions.ExceptionBodyMessage;
import org.test.firebase.pushservice.model.dto.TokenDTO;
import org.test.firebase.pushservice.service.TokenService;
import java.time.LocalDateTime;

/**
 * REST controller for managing Firebase registration tokens.
 * Provides endpoints for token registration and deletion.
 */
@Slf4j
@RestController
@RequestMapping("api/pushservice/v1/")
@Tag(name = "Регистрация/удаление токена", description = "Предназначен для подписки на пуш - уведомления путем регистрации токена")
public class RegisterTokenController {
    private final TokenService tokenService;

    /**
     * Constructs a new {@link RegisterTokenController} with required service dependency.
     *
     * @param tokenService the service for token operations
     */
    public RegisterTokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Endpoint for registering new Firebase tokens.
     *
     * @param tokenDTO the token data transfer object containing:
     *                 token: Firebase registration token,
     *                 userId: Unique identifier of the user associated with the token.
     * @return ResponseEntity with 201 (CREATED) status on success
     */
    @Operation(summary = "Добавление токена", description = "Подписка на уведомления."

    )
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Токен зарегистрирован", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SuccesResponse.class))}),

            @ApiResponse(responseCode = "400", description = "Ошибка в запросе.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))}),

            @ApiResponse(responseCode = "409", description = "Такой токен уже есть в базе", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))}),

            @ApiResponse(responseCode = "500", description = "Неизвестная ошибка.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))})})
    @PostMapping("/token")
    public ResponseEntity<SuccesResponse> addToken(@Valid @RequestBody TokenDTO tokenDTO) {
        log.info("Add token request received: {}", tokenDTO);
        tokenService.save(tokenDTO);
        log.info("Add token request succeeded: {}", tokenDTO);
        return ResponseEntity.status(201)
                             .body(new SuccesResponse(HttpStatus.CREATED.toString(), LocalDateTime.now(),
                                                      "Token registered successfully"));
    }

    /**
     * Endpoint for deleting existing Firebase tokens.
     *
     * @param tokenDTO the token data transfer object containing:
     *                 token: Firebase registration token to delete,
     *                 userId: Unique identifier of the user associated with the token.
     * @return ResponseEntity with 200 (OK) status on success
     */
    @Operation(summary = "Удаление токена", description = "Удаление токена из базы. Для ручного отказа от уведомлений на токен.")

    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Токен удален", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SuccesResponse.class))}),

            @ApiResponse(responseCode = "400", description = "Ошибка в запросе.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))}),

            @ApiResponse(responseCode = "404", description = "Токена нет в базе, нечего удалять", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))}),

            @ApiResponse(responseCode = "500", description = "Неизвестная ошибка.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionBodyMessage.class))})})
    @DeleteMapping("/token")
    public ResponseEntity<SuccesResponse> deleteToken(@Valid @RequestBody TokenDTO tokenDTO) {
        log.info("Delete token request received: {}", tokenDTO);
        tokenService.delete(tokenDTO);
        log.info("Delete token request succeed: {}", tokenDTO);
        return ResponseEntity.ok(
                new SuccesResponse(HttpStatus.OK.toString(), LocalDateTime.now(), "Token deleted successfully"));
    }

}
