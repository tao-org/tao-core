package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.eodata.VectorData;

/**
 * CRUD repository for VectorData entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "vectorDataRepository")
@Transactional
public interface VectorDataRepository extends PagingAndSortingRepository<VectorData, String> {
    /**
     * Find VectorData entity by its alphanumeric given identifier
     * @param id - the given vector data product identifier
     * @return the corresponding VectorData entity
     */
    VectorData findById(String id);
}
