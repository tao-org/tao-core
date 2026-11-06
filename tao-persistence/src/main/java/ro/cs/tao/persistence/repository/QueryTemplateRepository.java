package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.userdb.QueryTemplate;

public interface QueryTemplateRepository extends JpaRepository<QueryTemplate, String> {
}
