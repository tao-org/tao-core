package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ProcessingComponent;

/**
 * CRUD repository for ProcessingComponent entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "processingComponentRepository")
@Transactional
public interface ProcessingComponentRepository extends PagingAndSortingRepository<ProcessingComponent, String> {

    /**
     * Find ProcessingComponent entity by its identifier
     * @param id - the given processing component identifier
     * @return the corresponding ProcessingComponent entity
     */
    ProcessingComponent findById(String id);

    /**
     * Find ProcessingComponent entity by its label
     * @param label - the given label
     * @return the corresponding ProcessingComponent entity
     */
    ProcessingComponent findByLabel(String label);
}
