package org.test.firebase.pushservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.test.firebase.pushservice.exceptions.RegisterTokenException;
import org.test.firebase.pushservice.model.dto.TokenDTO;
import org.test.firebase.pushservice.model.entity.UserToken;
import org.test.firebase.pushservice.repository.TokenRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    private final String userId = "1";
    private final String testToken = "test-token";
    private final String validToken = "valid-token";
    private final String invalidToken = "invalid-token";
    private final String existingToken = "existing-token";
    private final String nonExistingToken = "non-existing-token";

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private SendMessageService sendMessageService;

    @InjectMocks
    private TokenService tokenService;

    @Test
    @DisplayName("Test mapToDto()")
    void testMapToDto_ShouldCorrectlyMapEntityToDto() {
        UserToken userToken = new UserToken();
        userToken.setToken(testToken);
        userToken.setUserId(userId);
        TokenDTO tokenDTO = TokenService.mapToDto(userToken);
        assertEquals(testToken, tokenDTO.getToken());
        assertEquals(userId, tokenDTO.getUserId());
    }

    @Test
    @DisplayName("Test mapToEntity()")
    void testMapToEntity_ShouldCorrectlyMapDtoToEntity() {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(testToken);
        tokenDTO.setUserId(userId);
        UserToken userToken = TokenService.mapToEntity(tokenDTO);
        assertEquals(testToken, userToken.getToken());
        assertEquals(userId, userToken.getUserId());
    }

    @Test
    @DisplayName("Должен сохранять новый валидный токен")
    void testSave_WhenTokenValidAndNotExists() {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(validToken);
        tokenDTO.setUserId(userId);
        when(sendMessageService.isTokenInvalid(tokenDTO)).thenReturn(false);
        when(tokenRepository.existsByToken(validToken)).thenReturn(false);
        tokenService.save(tokenDTO);
        verify(tokenRepository, times(1)).save(any(UserToken.class));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при попытке сохранения невалидного токена")
    void testSave_ShouldThrowException_WhenTokenInvalid() {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(invalidToken);
        tokenDTO.setUserId(userId);
        when(sendMessageService.isTokenInvalid(tokenDTO)).thenReturn(true);
        RegisterTokenException exception = assertThrows(RegisterTokenException.class,
                                                        () -> tokenService.save(tokenDTO));
        assertEquals("Token is not valid", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при попытке сохранения существующего токена")
    void testSave_ShouldThrowException_WhenTokenExists() {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(existingToken);
        tokenDTO.setUserId(userId);
        when(sendMessageService.isTokenInvalid(tokenDTO)).thenReturn(false);
        when(tokenRepository.existsByToken(existingToken)).thenReturn(true);
        RegisterTokenException exception = assertThrows(RegisterTokenException.class,
                                                        () -> tokenService.save(tokenDTO));
        assertEquals("Token already registered", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
    }

    @Test
    @DisplayName("метод delete должен удалять существующий токен")
    void testDelete_ShouldDeleteToken_WhenTokenExists() {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(existingToken);
        tokenDTO.setUserId(userId);
        when(tokenRepository.existsByToken(existingToken)).thenReturn(true);
        tokenService.delete(tokenDTO);
        verify(tokenRepository, times(1)).deleteByToken(existingToken);
    }

    @Test
    @DisplayName("метод delete должен выбрасывать исключение при попытке удалить несуществующий токен")
    void testDelete_ShouldThrowException_WhenTokenNotExists() {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(nonExistingToken);
        tokenDTO.setUserId(userId);
        when(tokenRepository.existsByToken(nonExistingToken)).thenReturn(false);
        RegisterTokenException exception = assertThrows(RegisterTokenException.class,
                                                        () -> tokenService.delete(tokenDTO));
        assertEquals("Token not registered", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Тест cleanupOldTokens()")
    void testCleanupOldTokens_ShouldDeleteTokensOlderThan270Days() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC)
                                            .minusDays(270);
        tokenService.cleanupOldTokens();
        verify(tokenRepository, times(1)).deleteByTimeBefore(cutoff);
    }
}