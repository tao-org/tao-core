package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workspaces.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
@Qualifier(value = "repositoryJPARepository")
@Transactional
public interface RepositoryJPARepository extends PagingAndSortingRepository<Repository, String> {

    @Query(value = "SELECT * FROM workspace.repository WHERE username = :userName ORDER BY system desc, created", nativeQuery = true)
    List<Repository> getByUser(@Param("userName") String userName);

}
