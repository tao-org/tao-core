package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.docker.Container;

/**
 * CRUD repository for Container entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "containerRepository")
@Transactional
public interface ContainerRepository extends PagingAndSortingRepository<Container, String> {

    /**
     * Find Container entity by its name
     * @param name - the given container name
     * @return the corresponding Container entity
     */
    Container findByName(String name);
}
