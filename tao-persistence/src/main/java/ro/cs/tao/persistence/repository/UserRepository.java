package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.user.User;

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
     * Find User entity by its given identifier
     * @param id - the given user identifier
     * @return the corresponding User entity
     */
    User findById(final Long id);

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
}
