package org.test.firebase.pushservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.test.firebase.pushservice.model.entity.UserToken;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing {@link UserToken} entities.
 * Provides CRUD operations and custom query methods for working with user tokens in database.
 * Extends {@link JpaRepository} to inherit standard JPA operations.
 */
@Repository
public interface TokenRepository extends JpaRepository<UserToken, Long> {
    /**
     * Checks if a token exists in the database.
     *
     * @param token the token to check
     * @return true if the token exists, false otherwise
     */
    boolean existsByToken(String token);

    /**
     * Finds a user token by its value.
     *
     * @param token the token value to search for
     * @return the {@link UserToken} entity if found, or null otherwise
     */
    UserToken findByToken(String token);

    /**
     * Finds all tokens associated with a specific userID.
     *
     * @param userId the user ID to search for
     * @return a list of {@link UserToken} entities for the given user ID
     */
    List<UserToken> findByUserId(String userId);

    /**
     * Deletes all tokens older than the specified time.
     * Used for cleaning up expired tokens.
     *
     * @param time the cutoff time (tokens older than this will be deleted)
     */
    void deleteByTimeBefore(LocalDateTime time);

    /**
     * Deletes a specific token by its value.
     *
     * @param token the token value to delete
     */
    void deleteByToken(String token);
}
