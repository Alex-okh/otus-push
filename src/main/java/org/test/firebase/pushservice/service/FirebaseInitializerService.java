package org.test.firebase.pushservice.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;

/**
 * Service for initializing Firebase application.
 * Loading Firebase credentials and initializing the FirebaseApp during application startup.
 * Provides a configured FirebaseMessaging bean for sending messages.
 */
@Service
@Slf4j
public class FirebaseInitializerService {

    @Value("${firebase.credentials.filename}")
    private String credentialsFilename;

    /**
     * Provides a FirebaseMessaging instance configured with the initialized FirebaseApp.
     *
     * @return FirebaseMessaging instance for sending messages
     * @throws IOException           if there is an issue accessing the Firebase credentials
     * @throws IllegalStateException if FirebaseApp has not been initialized
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (FirebaseApp.getApps()
                       .isEmpty()) {
            throw new IllegalStateException("FirebaseApp is not initialized");
        }
        return FirebaseMessaging.getInstance();
    }

    /**
     * Initializes FirebaseApp during application startup by loading credentials from the specified file.
     *
     * @throws RuntimeException if the credentials file cannot be found or if Firebase initialization fails
     */
    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps()
                           .isEmpty()) {
                log.info("Loading Firebase credentials from classpath: {}", credentialsFilename);

                InputStream credentialsStream = getClass().getClassLoader()
                                                          .getResourceAsStream(credentialsFilename);

                if (credentialsStream == null) {
                    throw new RuntimeException("Firebase credentials file not found: " + credentialsFilename);
                }

                FirebaseOptions options = FirebaseOptions.builder()
                                                         .setCredentials(
                                                                 GoogleCredentials.fromStream(credentialsStream))
                                                         .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}