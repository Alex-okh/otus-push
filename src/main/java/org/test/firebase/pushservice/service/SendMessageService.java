package org.test.firebase.pushservice.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.test.firebase.pushservice.exceptions.SendMessageException;
import org.test.firebase.pushservice.model.dto.MessageDTO;
import org.test.firebase.pushservice.model.dto.TokenDTO;
import org.test.firebase.pushservice.model.entity.UserToken;
import org.test.firebase.pushservice.repository.TokenRepository;
import java.util.List;

/**
 * Service for sending Firebase push-notifications (FCM) and managing message delivery.
 * Performs sending multicast messages, processing responses, and checking for tokens invalidity.
 */
@Service
@Slf4j
public class SendMessageService {
    private final TokenRepository repository;
    private final FirebaseMessaging firebaseMessaging;

    /**
     * Constructs a new {@link SendMessageService} with token repository and FirebaseMessaging instance.
     *
     * @param repository        the repository for accessing user tokens
     * @param firebaseMessaging the FirebaseMessaging instance for sending messages
     */

    public SendMessageService(TokenRepository repository, FirebaseMessaging firebaseMessaging) {
        this.repository = repository;
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * Sends a multicast message to all tokens associated with the userID specified in the MessageDTO.
     * Processes the response to handle failures and clean up invalid tokens.
     *
     * @param message the MessageDTO containing the userID, message title, and message text
     * @throws SendMessageException if no tokens are found for the user, or if all tokens are invalid,
     *                              or if no messages could be sent due to other errors
     */
    @Transactional
    public void send(MessageDTO message) {
        List<UserToken> tokens = repository.findByUserId(message.getUserId());
        log.info("Found {} tokens for userID {}", tokens.size(), message.getUserId());
        if (tokens.isEmpty()) {
            log.info("No tokens found for userID {}, throwing 404", message.getUserId());
            throw new SendMessageException("No user token found for " + message.getUserId(), HttpStatus.NOT_FOUND);
        }
        List<String> tokenList = tokens.stream()
                                       .map(UserToken::getToken)
                                       .toList();

        var multicastMessage = MulticastMessage.builder()
                                               .addAllTokens(tokenList)
                                               .setNotification(Notification.builder()
                                                                            .setTitle(message.getMessageTitle())
                                                                            .setBody(message.getMessageText())
                                                                            .build())
                                               .build();

        BatchResponse responses = null;
        try {
            responses = firebaseMessaging.sendEachForMulticast(multicastMessage);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending Multicast message: {}", e.getMessage());
        }

        int badTokensCount = 0;
        if (responses != null && responses.getFailureCount() > 0) {
            badTokensCount = processBadTokens(tokenList, responses);
        }

        if (responses != null && responses.getSuccessCount() == 0) {
            if (badTokensCount == responses.getFailureCount()) {
                log.info("No messages sent for userId {}. All stored tokens are not valid or unregistered",
                         message.getUserId());
                throw new SendMessageException("No messages sent for userId " + message.getUserId() +
                                               ". All stored tokens are not valid or unregistered",
                                               HttpStatus.BAD_REQUEST);
            } else {
                throw new SendMessageException(
                        "No messages sent for userId " + message.getUserId() + ". See logs for more information",
                        HttpStatus.GATEWAY_TIMEOUT);
            }
        }
    }

    /**
     * Processes the responses from a multicast message send operation.
     * Identifies and removes invalid or unregistered tokens from the repository.
     *
     * @param tokenList the list of tokens that were included in the multicast message
     * @param responses the BatchResponse from the Firebase send operation
     * @return the number of invalid or unregistered tokens that were processed
     */
    private int processBadTokens(List<String> tokenList, BatchResponse responses) {
        int badTokensCount = 0;
        for (int i = 0; i < responses.getResponses()
                                     .size(); i++) {
            var response = responses.getResponses()
                                    .get(i);
            var token = tokenList.get(i);
            if (!response.isSuccessful()) {
                var errorCode = response.getException()
                                        .getMessagingErrorCode();
                if (errorCode == MessagingErrorCode.UNREGISTERED || (errorCode == MessagingErrorCode.INVALID_ARGUMENT &&
                                                                     response.getException()
                                                                             .getMessage()
                                                                             .equals("The registration token is not a valid FCM registration token"))) {
                    badTokensCount++;
                    repository.deleteByToken(token);
                    log.info("Token {} unregistered or not valid. Deleted from DB.", token);
                } else {
                    log.error("Unexpected send message error. Token: {}", token);
                    log.error("Errorcode = {}", response.getException()
                                                        .getMessagingErrorCode()
                                                        .toString());
                    log.error("Message = {}", response.getException()
                                                      .getMessage());
                }
            }
        }
        return badTokensCount;
    }

    /**
     * Validates a Firebase Cloud Messaging token by sending dryRun attempt to send a test message.
     *
     * @param token the TokenDTO containing the token to check for invalidation
     * @return true if the token is invalid or unregistered, false otherwise
     */
    public boolean isTokenInvalid(TokenDTO token) {
        Message message = Message.builder()
                                 .setToken(token.getToken())
                                 .build();
        try {
            firebaseMessaging.send(message, true);
            log.info("Token {} validated successfully.", token.getToken());
        } catch (FirebaseMessagingException e) {
            var errorCode = e.getMessagingErrorCode();
            log.info("Validating token {} failed with errorcode {}", token.getToken(), errorCode);
            if (errorCode == MessagingErrorCode.UNREGISTERED || (errorCode == MessagingErrorCode.INVALID_ARGUMENT &&
                                                                 e.getMessage()
                                                                  .equals("The registration token is not a valid FCM registration token"))) {
                return true;
            }
        }
        return false;
    }
}
