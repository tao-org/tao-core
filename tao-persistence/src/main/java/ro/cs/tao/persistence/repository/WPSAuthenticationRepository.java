package ro.cs.tao.persistence.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import ro.cs.tao.component.WebServiceAuthentication;

public interface WPSAuthenticationRepository extends PagingAndSortingRepository<WebServiceAuthentication, String> {
}
