package org.test.firebase.pushservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.test.firebase.pushservice.exceptions.RegisterTokenException;
import org.test.firebase.pushservice.model.dto.TokenDTO;
import org.test.firebase.pushservice.model.entity.UserToken;
import org.test.firebase.pushservice.repository.TokenRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Service class for managing user tokens. Provides functionality of saving, deleting,
 * and cleaning up tokens, mapping between DTO and entity models.
 */

@Slf4j
@Service
public class TokenService {
    private final TokenRepository repository;
    private final SendMessageService sendMessageService;

    /**
     * Constructs a new {@link TokenService} with token repository and message service.
     *
     * @param repository         the repository for token operations
     * @param sendMessageService the service for validating tokens and sending push-notifications
     */
    public TokenService(TokenRepository repository, SendMessageService sendMessageService) {
        this.repository = repository;
        this.sendMessageService = sendMessageService;
    }

    /**
     * Performs mapping {@link UserToken} entity to a {@link TokenDTO}.
     *
     * @param userToken {@link UserToken} entity to map containing the token and userID
     * @return mapped TokenDTO
     */
    public static TokenDTO mapToDto(UserToken userToken) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(userToken.getToken());
        tokenDTO.setUserId(userToken.getUserId());
        return tokenDTO;
    }

    /**
     * Performs mapping {@link TokenDTO} to a {@link UserToken} entity.
     *
     * @param tokenDTO {@link TokenDTO} dto to map containing the token and userID
     * @return mapped UserToken entity
     */
    public static UserToken mapToEntity(TokenDTO tokenDTO) {
        UserToken userToken = new UserToken();
        userToken.setToken(tokenDTO.getToken());
        userToken.setUserId(tokenDTO.getUserId());
        return userToken;
    }

    /**
     * Saves a token to the database after checking for invalidity and existence in the database
     *
     * @param tokenDTO the {@link TokenDTO} containing the token and userID
     * @throws RegisterTokenException if the token is not valid or already registered
     */
    public void save(TokenDTO tokenDTO) {
        log.info("save token: {}", tokenDTO);
        if (sendMessageService.isTokenInvalid(tokenDTO)) {
            log.info("Token: {} invalid, throwing 400", tokenDTO);
            throw new RegisterTokenException("Token is not valid", HttpStatus.BAD_REQUEST);
        }
        if (repository.existsByToken(mapToEntity(tokenDTO).getToken())) {
            log.info("Token: {} already exists, throwing 409", tokenDTO);
            throw new RegisterTokenException("Token already registered", HttpStatus.CONFLICT);
        }
        repository.save(mapToEntity(tokenDTO));
        log.info("Token: {} saved to DB", tokenDTO);
    }

    /**
     * Deletes a token from the database if it exists in the database
     *
     * @param tokenDTO the {@link TokenDTO} containing the token and userID
     * @throws RegisterTokenException if the token is not registered in database
     */
    @Transactional
    public void delete(TokenDTO tokenDTO) {
        if (!repository.existsByToken(tokenDTO.getToken())) {
            log.info("Token: {} is not registered. Throwing 404.", tokenDTO);
            throw new RegisterTokenException("Token not registered", HttpStatus.NOT_FOUND);
        }
        repository.deleteByToken(mapToEntity(tokenDTO).getToken());
        log.info("Token: {} deleted from DB", tokenDTO);

    }

    /**
     * Performs scheduled task to clean up tokens older than 270 days.
     * Runs daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldTokens() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC)
                                            .minusDays(270);
        repository.deleteByTimeBefore(cutoff);
        log.info("Deleted token older than 270 days");
    }

}