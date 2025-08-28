package org.test.firebase.pushservice.service;

import com.google.firebase.messaging.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.test.firebase.pushservice.exceptions.SendMessageException;
import org.test.firebase.pushservice.model.dto.MessageDTO;
import org.test.firebase.pushservice.model.dto.TokenDTO;
import org.test.firebase.pushservice.model.entity.UserToken;
import org.test.firebase.pushservice.repository.TokenRepository;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMessageServiceTest {

    @Mock
    private TokenRepository repository;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private SendMessageService sendMessageService;
    private MessageDTO validMessage;
    private TokenDTO validToken;
    private UserToken validUserToken;

    @BeforeEach
    void setUp() {

        validMessage = new MessageDTO();
        validMessage.setUserId("testUser123");
        validMessage.setMessageTitle("Test Notification");
        validMessage.setMessageText("This is a test message");

        validToken = new TokenDTO();
        validToken.setUserId("testUser123");
        validToken.setToken("testToken123");

        validUserToken = new UserToken();
        validUserToken.setUserId("testUser123");
        validUserToken.setToken("testToken123");
    }

    @Test
    @DisplayName("Тест NOT_FOUND, когда токены отсутствуют")
    void send_ShouldThrowNotFoundException_WhenNoTokensAvailable() {

        when(repository.findByUserId("testUser123")).thenReturn(List.of());

        SendMessageException exception = assertThrows(SendMessageException.class,
                                                      () -> sendMessageService.send(validMessage));

        assertEquals("No user token found for testUser123", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Отправка сообщения должна удалять невалидные токены при ошибке UNREGISTERED")
    void send_ShouldDeleteInvalidTokens_WhenUnregisteredErrorOccurs() throws FirebaseMessagingException {
        // Подготовка
        when(repository.findByUserId("testUser123")).thenReturn(List.of(validUserToken));

        BatchResponse batchResponse = mock(BatchResponse.class);
        when(batchResponse.getFailureCount()).thenReturn(1);
        when(batchResponse.getSuccessCount()).thenReturn(0);

        SendResponse failedResponse = mock(SendResponse.class);
        when(failedResponse.isSuccessful()).thenReturn(false);

        FirebaseMessagingException fme = mock(FirebaseMessagingException.class);
        when(fme.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        when(failedResponse.getException()).thenReturn(fme);

        when(batchResponse.getResponses()).thenReturn(List.of(failedResponse));
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);

        SendMessageException exception = assertThrows(SendMessageException.class,
                                                      () -> sendMessageService.send(validMessage));

        assertEquals("No messages sent for userId testUser123. All stored tokens are not valid or unregistered",
                     exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(repository, times(1)).deleteByToken("testToken123");
    }

    @Test
    @DisplayName("Отправка сообщения должна успешно доставлять сообщения при валидных токенах")
    void send_ShouldSuccessfullyDeliverMessages_WhenTokensAreNotInValid() throws FirebaseMessagingException {

        when(repository.findByUserId("testUser123")).thenReturn(List.of(validUserToken));

        BatchResponse successResponse = mock(BatchResponse.class);
        when(successResponse.getFailureCount()).thenReturn(0);
        when(successResponse.getSuccessCount()).thenReturn(1);
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(successResponse);

        assertDoesNotThrow(() -> sendMessageService.send(validMessage));
    }

    @Test
    @DisplayName("Тест GATEWAY_TIMEOUT при неизвестной ошибке")
    void send_ShouldThrowGatewayTimeout_WhenUnknownErrorOccurs() throws FirebaseMessagingException {

        when(repository.findByUserId("testUser123")).thenReturn(List.of(validUserToken));

        BatchResponse batchResponse = mock(BatchResponse.class);
        when(batchResponse.getFailureCount()).thenReturn(1);
        when(batchResponse.getSuccessCount()).thenReturn(0);

        SendResponse failedResponse = mock(SendResponse.class);
        when(failedResponse.isSuccessful()).thenReturn(false);

        FirebaseMessagingException fme = mock(FirebaseMessagingException.class);
        when(fme.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INTERNAL);
        when(failedResponse.getException()).thenReturn(fme);

        when(batchResponse.getResponses()).thenReturn(List.of(failedResponse));
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).thenReturn(batchResponse);

        SendMessageException exception = assertThrows(SendMessageException.class,
                                                      () -> sendMessageService.send(validMessage));

        assertEquals("No messages sent for userId testUser123. See logs for more information", exception.getMessage());
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Проверка токена должна возвращать true для невалидного токена")
    void isTokenInvalid_ShouldReturnTrue_ForInvalidToken() throws FirebaseMessagingException {
        // Подготовка
        FirebaseMessagingException fme = mock(FirebaseMessagingException.class);
        when(fme.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INVALID_ARGUMENT);
        when(fme.getMessage()).thenReturn("The registration token is not a valid FCM registration token");
        when(firebaseMessaging.send(any(Message.class), anyBoolean())).thenThrow(fme);

        assertTrue(sendMessageService.isTokenInvalid(validToken));
    }

    @Test
    @DisplayName("Проверка токена должна возвращать false для не невалидного токена")
    void isTokenInvalid_ShouldReturnFalse_ForValidToken() throws FirebaseMessagingException {

        when(firebaseMessaging.send(any(Message.class), anyBoolean())).thenReturn("testMessageId");

        assertFalse(sendMessageService.isTokenInvalid(validToken));
    }

    @Test
    @DisplayName("Проверка токена должна обрабатывать другие ошибки без пометки токена как невалидного")
    void isTokenInvalid_ShouldHandleOtherErrors_WithoutMarkingTokenInvalid() throws FirebaseMessagingException {

        FirebaseMessagingException fme = mock(FirebaseMessagingException.class);
        when(fme.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INTERNAL);
        when(firebaseMessaging.send(any(Message.class), anyBoolean())).thenThrow(fme);

        assertFalse(sendMessageService.isTokenInvalid(validToken));
    }
}