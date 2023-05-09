package ro.cs.tao.persistence.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import ro.cs.tao.workspaces.RepositoryTemplate;

public interface RepositoryTemplateJPARepository  extends PagingAndSortingRepository<RepositoryTemplate, String> {

}
