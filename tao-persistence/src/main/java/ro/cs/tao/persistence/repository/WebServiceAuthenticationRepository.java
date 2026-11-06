package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.component.WebServiceAuthentication;

public interface WebServiceAuthenticationRepository extends JpaRepository<WebServiceAuthentication, String> {
}

