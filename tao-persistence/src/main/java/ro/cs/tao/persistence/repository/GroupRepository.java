package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.user.Group;

/**
 * CRUD repository for User entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "groupRepository")
@Transactional
public interface GroupRepository extends PagingAndSortingRepository<Group, Integer> {
    /**
     * Find Group entity by its given identifier
     * @param id - the given user identifier
     * @return the corresponding Group entity
     */
    Group findById(final Long id);

    /**
     * Find Group entity by its name
     * @param name - the group name
     * @return the corresponding Group entity
     */
    Group findByName(final String name);
}
