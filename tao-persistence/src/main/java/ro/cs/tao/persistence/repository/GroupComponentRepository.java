package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.GroupComponent;

/**
 * CRUD repository for GroupComponent entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "groupComponentRepository")
@Transactional
public interface GroupComponentRepository extends PagingAndSortingRepository<GroupComponent, String> {

    /**
     * Find ProcessingComponent entity by its label
     * @param label - the given label
     * @return the corresponding ProcessingComponent entity
     */
    GroupComponent findByLabel(String label);
}
