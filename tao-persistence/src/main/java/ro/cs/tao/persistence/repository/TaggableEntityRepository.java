package ro.cs.tao.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import ro.cs.tao.component.TaoComponent;

import java.util.List;

@NoRepositoryBean
public interface TaggableEntityRepository<T extends TaoComponent, ID> extends AuditableEntityRepository<T, ID> {

    @Query("SELECT e FROM #{#entityName} e WHERE LOWER(e.label) LIKE LOWER(CONCAT('%', :label, '%'))")
    Page<T> getByLabelLike(@Param("label") String label, Pageable pageable);

    @Query("SELECT count(e) FROM #{#entityName} e WHERE LOWER(e.label) LIKE LOWER(CONCAT('%', :label, '%'))")
    long countByLabelLike(@Param("label") String label);

    Page<T> getByTags(List<String> tags, Pageable pageable);

    long countByTags(List<String> tags);

    Page<T> getByLabelLikeAndTags(String label, List<String> tags, Pageable pageable);

    long countByLabelLikeAndTags(String label, List<String> tags);

}
