package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.Tag;

import java.util.List;

@Repository
@Qualifier(value = "containerRepository")
@Transactional
public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {

    /**
     * Find Tag entity by its name
     * @param tag - the given tag name
     */
    Tag findByText(String tag);

    @Query(value = "SELECT id, tag_type_id, tag FROM common.tag WHERE tag_type_id = :typeId ORDER BY tag",
            nativeQuery = true)
    List<Tag> getTags(@Param("typeId")int tagTypeId);
}
