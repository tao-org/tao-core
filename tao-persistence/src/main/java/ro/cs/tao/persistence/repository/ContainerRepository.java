package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.docker.Container;

import java.util.List;

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

    /**
     * List containers by type id
     * @param typeId    The type identifier (see {@code ro.cs.tao.docker.ContainerType})
     */
    @Query(value = "SELECT * FROM component.container WHERE type_id = :typeId", nativeQuery = true)
    List<Container> findByTypeId(@Param("typeId") int typeId);
}
