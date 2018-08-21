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

    @Query(value = "SELECT * FROM tao.raster_data_product WHERE CONCAT(location, entry_point) IN (:locations)",
            nativeQuery = true)
    List<EOProduct> getProductsByLocation(@Param("locations") Set<String> locations);

    @Query(value = "SELECT * FROM tao.raster_data_product WHERE location = :location", nativeQuery = true)
    List<EOProduct> getProductsByLocation(@Param("location") String location);

    @Query(value = "SELECT * FROM tao.raster_data_product WHERE visibility_id = 1", nativeQuery = true)
    List<EOProduct> getPublicProducts();
}
