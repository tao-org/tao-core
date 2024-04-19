package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.configuration.ConfigurationItem;

import java.util.List;

@Repository
@Qualifier(value = "configurationRepository")
@Transactional
public interface ConfigurationRepository extends PagingAndSortingRepository<ConfigurationItem, String> {

    @Query(value = "SELECT * FROM config.config WHERE category_id = :categoryId ORDER BY id", nativeQuery = true)
    List<ConfigurationItem> getByCategory(@Param("categoryId") int categoryId);

    List<ConfigurationItem> findByIdContaining(String filter);
}
