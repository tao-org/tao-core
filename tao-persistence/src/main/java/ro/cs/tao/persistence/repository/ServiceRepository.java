package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.topology.ServiceDescription;

/**
 * CRUD repository for ServiceDescription entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "serviceRepository")
@Transactional
public interface ServiceRepository extends PagingAndSortingRepository<ServiceDescription, Integer> {
    /**
     * Find ServiceDescription entity by its identifier
     * @param id - the given service id
     * @return the corresponding ServiceDescription entity
     */
    ServiceDescription findById(Integer id);

    /**
     * Find ServiceDescription entity by its name and version (unique)
     * @param name - the given service name
     * @param version - the given service version
     * @return the corresponding ServiceDescription entity
     */
    ServiceDescription findByNameAndVersion(String name, String version);
}
