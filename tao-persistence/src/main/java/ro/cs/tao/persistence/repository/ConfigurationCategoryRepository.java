package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.configuration.ConfigurationCategory;

@Repository
@Qualifier(value = "configurationCategoryRepository")
@Transactional
public interface ConfigurationCategoryRepository extends PagingAndSortingRepository<ConfigurationCategory, Integer> {
}
