package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.userdb.SavedQuery;
import ro.cs.tao.userdb.SavedQueryId;

import java.util.List;

public interface SavedQueryRepository extends JpaRepository<SavedQuery, SavedQueryId> {
    List<SavedQuery> findByUserIdOrderByNameAsc(String userId);
}
