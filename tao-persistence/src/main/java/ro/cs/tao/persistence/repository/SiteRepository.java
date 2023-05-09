package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workspaces.Site;

import java.util.List;

@org.springframework.stereotype.Repository
@Qualifier(value = "siteRepository")
@Transactional
public interface SiteRepository extends PagingAndSortingRepository<Site, String> {

    @Query(value = "SELECT * FROM workspace.site WHERE username = :user", nativeQuery = true)
    List<Site> getByUser(@Param("user") String user);
}
