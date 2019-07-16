package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = "SELECT * FROM product.raster_data_product WHERE location IN (:locations)",
            nativeQuery = true)
    List<EOProduct> getProductsByLocation(@Param("locations") Set<String> locations);

    @Query(value = "SELECT * FROM product.raster_data_product WHERE location = :location", nativeQuery = true)
    List<EOProduct> getProductsByLocation(@Param("location") String location);

    @Query(value = "SELECT * FROM product.raster_data_product WHERE visibility_id = 1", nativeQuery = true)
    List<EOProduct> getPublicProducts();

    @Query(value = "SELECT p.* FROM product.raster_data_product p LEFT JOIN product.raster_data_product_refs r ON r.product_id = p.id " +
            "WHERE p.visibility_id = 1 AND p.status_id = 5 AND r.refs != :user", nativeQuery = true)
    List<EOProduct> getOtherPublishedProducts(@Param("user") String user);

    @Query(value = "SELECT name FROM product.raster_data_product WHERE name IN (:names) AND status_id = 3", nativeQuery = true)
    List<String> getExistingProductNames(@Param("names") Set<String> names);

    @Query(value = "SELECT * FROM product.raster_data_product WHERE name IN (:names)", nativeQuery = true)
    List<EOProduct> getProductsByName(@Param("names") Set<String> names);

    @Query(value = "SELECT COUNT(s.id) FROM component.source_descriptor s JOIN component.data_source_component c ON c.id = s.parent_id " +
            "WHERE c.id != :componentId AND s.location LIKE CONCAT('%',CAST(:name AS varchar),'%')",
            nativeQuery = true)
    int getOtherProductReferences(@Param("componentId") String componentId, @Param("name") String name);

    @Query(value = "SELECT COALESCE(SUM(p.approximate_size), 0) FROM product.raster_data_product p " +
            "JOIN product.raster_data_product_refs r ON r.product_id = p.id WHERE r.refs = :userName", nativeQuery = true)
    long getUserRasterProductsSize(@Param("userName") String userName);

    @Query(value = "SELECT COALESCE(SUM(p.approximate_size), 0) FROM product.raster_data_product p " +
            "JOIN product.raster_data_product_refs r ON r.product_id = p.id JOIN product.product_status s ON " +
    		"p.status_id = s.id WHERE r.refs = :userName AND s.status IN ('DOWNLOADING','DOWNLOADED') AND " +
            "p.location LIKE CONCAT(CAST(:publicLocation AS varchar), '%')", nativeQuery = true)
    long getUserInputRasterProductsSize(@Param("userName") String userName, @Param("publicLocation") String publicLocation);

    @Query(value = "WITH t AS (SELECT CONCAT('%', t.job_id, '-', t.id, '%') AS c FROM execution.task t " +
            "JOIN execution.job j on j.id = t.job_id WHERE j.workflow_id = :workflowId) " +
            "SELECT p.* FROM product.raster_data_product p JOIN t ON CONCAT(p.location, p.entry_point) LIKE t.c", nativeQuery = true)
    List<EOProduct> getWorkflowOutputs(@Param("workflowId") long workflowId);

    @Query(value = "WITH t AS (SELECT CONCAT('%', t.job_id, '-', t.id, '%') AS c FROM execution.task t " +
            "WHERE t.job_id = :jobId) " +
            "SELECT p.* FROM product.raster_data_product p JOIN t ON CONCAT(p.location, p.entry_point) LIKE t.c", nativeQuery = true)
    List<EOProduct> getJobOutputs(@Param("jobId") long jobId);

    @Modifying
    @Query(value = "WITH delprods AS (WITH refs AS (SELECT COUNT(s.id) AS cnt FROM component.source_descriptor s JOIN component.data_source_component c ON c.id = s.parent_id " +
            "WHERE c.id != :componentId AND s.location LIKE CONCAT('%', CAST(:name AS varchar), '%') DELETE FROM product.raster_data_product USING refs " +
            "WHERE name = :name AND status_id NOT IN (2,3,5) AND refs.cnt = 0 RETURNING id) " +
            "DELETE FROM product.data_product_attributes USING delprods WHERE data_product_id = delprods.id",
            nativeQuery = true)
    void deleteIfNotReferenced(@Param("componentId") String componentId, @Param("name") String productName);

    @Modifying
    @Transactional
    void deleteByName(String name);
}
