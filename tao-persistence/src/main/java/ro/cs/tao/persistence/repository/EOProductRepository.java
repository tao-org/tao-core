package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.eodata.EOProduct;

import java.util.List;
import java.util.Set;

/**
 * CRUD repository for EOProduct entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "eoProductRepository")
@Transactional
public interface EOProductRepository extends PagingAndSortingRepository<EOProduct, String> {
    /**
     * Find EOProduct entity by its alphanumeric given identifier
     * @param id - the given data product identifier
     * @return the corresponding EOProduct entity
     */
    EOProduct findById(String id);

    @Query(value = "SELECT * FROM tao.raster_data_product WHERE CONCAT(location, entry_point) IN (:locations)",
            nativeQuery = true)
    List<EOProduct> getProductsByLocation(@Param("locations") Set<String> locations);
}
