package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * CRUD repository for User entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "userRepository")
@Transactional
public interface UserRepository extends OptimisedPagingAndSortingRepository<User, String> {

    /**
     * Find User entity by its username
     * @param username - the user's username
     * @return the corresponding User entity
     */
    User findByUsername(final String username);

    /**
     * Find User entity by its email
     * @param email - the user's email
     * @return the corresponding User entity
     */
    User findByEmail(final String email);

    /**
     * Find User entity by its alternative email
     * @param alternativeEmail - the user's alternative email
     * @return the corresponding User entity
     */
    User findByAlternativeEmail(final String alternativeEmail);

    /**
     * Find User entities by their activation status
     * @param status - the user's status
     * @return the corresponding User entities
     */
    List<User> findByStatus(final UserStatus status);

    /**
     * Finds a User entity based on the provided API key.
     *
     * @param apiKey - the API key of the user
     * @return the corresponding User entity, or null if no user is found with the given API key
     */
    User findByApiKey(final String apiKey);

    @Query(value = "SELECT u.* FROM usr.user u JOIN usr.user_group ug ON ug.user_id = u.id " +
            "WHERE ug.group_id = 1 AND u.status_id = 2", nativeQuery = true)
    List<User> getAdministrators();

    @Query(value = "SELECT * FROM usr.user WHERE id in (:ids) ORDER BY username", nativeQuery = true)
    List<User> getUsers(@Param("ids")Set<String> ids);

    @Query(value = "SELECT * FROM usr.user WHERE status_id = :statusId AND id in (:ids) ORDER BY username", nativeQuery = true)
    List<User> getUsers(@Param("ids")Set<String> ids, @Param("statusId") int statusId);

    @Query(value = "SELECT id FROM usr.user WHERE username = :usr and password = :pwd", nativeQuery = true)
    String login(@Param("usr") String user, @Param("pwd") String password);

    @Query(value = "SELECT id FROM usr.user WHERE username = :usr", nativeQuery = true)
    String getId(@Param("usr") String userName);

    @Query(value = "SELECT api_key FROM usr.user WHERE id = :id", nativeQuery = true)
    String getApiKey(@Param("id") String userId);

    @Query(value = "UPDATE usr.user SET api_key = :apiKey, api_key_expiration = :expiration WHERE id = :id", nativeQuery = true)
    @Modifying
    void updateApiKey(@Param("id") String userId, @Param("apiKey") String apiKey,
                      @Param("expiration") LocalDateTime expiration);

    @Query(value = "UPDATE usr.user SET api_key = NULL WHERE api_key_expiration <= :expiration", nativeQuery = true)
    @Modifying
    void deleteApiKeysOlderThan(@Param("expiration") LocalDateTime expiration);
}
