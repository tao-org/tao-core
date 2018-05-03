package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.user.Group;
import ro.cs.tao.user.User;

import java.util.List;

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
     * Find users withing a group
     * @param group - the users group
     * @return the corresponding User entities
     */
    List<User> findByGroup(Group group);
}
