package ro.cs.tao.persistence.repository;

import jakarta.persistence.QueryHint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ProcessingComponent;

import java.util.List;
import java.util.Set;

/**
 * CRUD repository for ProcessingComponent entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "processingComponentRepository")
@Transactional
public interface ProcessingComponentRepository extends TaggableEntityRepository<ProcessingComponent, String> {

    @Query(value = "SELECT * FROM component.processing_component WHERE owner_user_id = :userId AND component_type_id = :typeId",
            nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true"), @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    List<ProcessingComponent> getUserComponentsByType(@Param("userId") String userId,
                                                      @Param("typeId") int typeId);

    @Query(value = "SELECT * FROM component.processing_component WHERE label = :label", nativeQuery = true)
    List<ProcessingComponent> getByLabel(@Param("label") String label);

    @Query(value = "SELECT * FROM component.processing_component p " +
                   "WHERE EXISTS (SELECT 1 FROM unnest(string_to_array(p.tags, ',')) tag WHERE trim(tag) IN (:tags))",
           countQuery = "SELECT count(*) FROM component.processing_component p " +
                        "WHERE EXISTS (SELECT 1 FROM unnest(string_to_array(p.tags, ',')) tag WHERE trim(tag) IN (:tags))",
           nativeQuery = true)
    Page<ProcessingComponent> getByTags(@Param("tags") List<String> tags, Pageable pageable);

    @Query(value = "SELECT count(*) FROM component.processing_component p " +
                   "WHERE EXISTS (SELECT 1 FROM unnest(string_to_array(p.tags, ',')) tag WHERE trim(tag) IN (:tags))",
           nativeQuery = true)
    long countByTags(@Param("tags") List<String> tags);

    @Query(value = "SELECT * FROM component.processing_component p " +
                   "WHERE LOWER(p.label) LIKE LOWER(CONCAT('%', :label, '%')) " +
                   "AND EXISTS (SELECT 1 FROM unnest(string_to_array(p.tags, ',')) tag WHERE trim(tag) IN (:tags))",
           countQuery = "SELECT count(*) FROM component.processing_component p " +
                        "WHERE LOWER(p.label) LIKE LOWER(CONCAT('%', :label, '%')) " +
                        "AND EXISTS (SELECT 1 FROM unnest(string_to_array(p.tags, ',')) tag WHERE trim(tag) IN (:tags))",
           nativeQuery = true)
    Page<ProcessingComponent> getByLabelLikeAndTags(@Param("label") String label,
                                                    @Param("tags") List<String> tags,
                                                    Pageable pageable);

    @Query(value = "SELECT count(*) FROM component.processing_component p " +
                   "WHERE LOWER(p.label) LIKE LOWER(CONCAT('%', :label, '%')) " +
                   "AND EXISTS (SELECT 1 FROM unnest(string_to_array(p.tags, ',')) tag WHERE trim(tag) IN (:tags))",
           nativeQuery = true)
    long countByLabelLikeAndTags(@Param("label") String label, @Param("tags") List<String> tags);

    @Query(value = "SELECT * FROM component.processing_component WHERE id NOT IN (?1)", nativeQuery = true)
    List<ProcessingComponent> getOtherComponents(Set<String> ids);

    @Query(value = "SELECT * FROM component.processing_component WHERE container_id = :containerId", nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true"), @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    List<ProcessingComponent> getByContainer(@Param("containerId") String containerId);

    @Query(value = "SELECT * FROM component.processing_component WHERE category_id = :categoryId", nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true"), @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    List<ProcessingComponent> getByCategory(@Param("category") int categoryId);

    @Query(value = "SELECT * FROM component.processing_component WHERE id = :id AND container_id = :containerId", nativeQuery = true)
    ProcessingComponent getByIdAndContainer(@Param("id") String id, @Param("containerId") String containerId);

    @Query(value = "SELECT * FROM component.processing_component WHERE label = :label AND container_id = :containerId", nativeQuery = true)
    ProcessingComponent getByLabelAndContainer(@Param("label") String label, @Param("containerId") String containerId);

    @Query(value = "SELECT * FROM component.processing_component WHERE (visibility_id = 1 OR (owner_user_id = :userId)) AND " +
                   "(similarity(label, :text) >= 0.3 OR similarity(tags, :text) >= 0.3) LIMIT 5", nativeQuery = true)
    List<ProcessingComponent> findBySimilarity(@Param("text") String text, @Param("userId") String userId);

    @Query(value = "SELECT label FROM component.processing_component WHERE (visibility_id = 1 OR (owner_user_id = :userId)) AND " +
                   "(similarity(label, :text) >= 0.3 OR similarity(tags, :text) >= 0.3) LIMIT 5", nativeQuery = true)
    List<String> findLabelsBySimilarity(@Param("text") String text, @Param("userId") String userId);
}

