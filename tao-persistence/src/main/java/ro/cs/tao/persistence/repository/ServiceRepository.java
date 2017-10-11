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
public interface ServiceRepository extends PagingAndSortingRepository<ServiceDescription, String> {
    /**
     * Find ServiceDescription entity by its name
     * @param name - the given service name
     * @return the corresponding ServiceDescription entity
     */
    ServiceDescription findByName(String name);
}
