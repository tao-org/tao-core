package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.eodata.VectorData;

import java.util.List;
import java.util.Set;

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

    @Query(value = "SELECT * FROM tao.vector_data_product WHERE CONCAT(location, entry_point) IN (:locations)",
            nativeQuery = true)
    List<VectorData> getProductsByLocation(@Param("locations") Set<String> locations);
}
