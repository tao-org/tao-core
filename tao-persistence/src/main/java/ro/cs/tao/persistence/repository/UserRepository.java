package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.user.User;
import ro.cs.tao.user.UserStatus;

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
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

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

    @Query(value = "SELECT u.* FROM usr.user u JOIN usr.user_group ug ON ug.user_id = u.id " +
            "WHERE ug.group_id = 1 AND u.status_id = 2", nativeQuery = true)
    List<User> getAdministrators();

    @Query(value = "SELECT * FROM usr.user WHERE username in (:names) ORDER BY username", nativeQuery = true)
    List<User> getUsers(@Param("names")Set<String> names);

    @Query(value = "SELECT id FROM usr.user WHERE username = :usr and password = :pwd", nativeQuery = true)
    Integer login(@Param("usr") String user, @Param("pwd") String password);
}
