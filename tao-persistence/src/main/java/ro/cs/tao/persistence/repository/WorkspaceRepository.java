package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workspaces.Workspace;

import java.util.List;

@Repository
@Qualifier(value = "workspaceRepository")
@Transactional
public interface WorkspaceRepository extends PagingAndSortingRepository<Workspace, Integer> {

    @Query(value = "SELECT * FROM topology.workspace WHERE username = :userName", nativeQuery = true)
    List<Workspace> getByUser(@Param("userName") String userName);

}
