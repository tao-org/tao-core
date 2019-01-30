package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ProcessingComponent;

import java.util.List;

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

    @Query(value = "SELECT * FROM component.processing_component WHERE owner_user = :userName AND component_type_id = :typeId",
            nativeQuery = true)
    List<ProcessingComponent> getUserComponentsByType(@Param("userName") String userName,
                                                      @Param("typeId") int typeId);

    @Query(value = "SELECT * FROM component.processing_component WHERE label = :label", nativeQuery = true)
    List<ProcessingComponent> getByLabel(@Param("label") String label);
}
