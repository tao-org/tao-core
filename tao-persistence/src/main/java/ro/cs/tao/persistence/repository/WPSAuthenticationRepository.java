package ro.cs.tao.persistence.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import ro.cs.tao.component.WPSAuthentication;

public interface WPSAuthenticationRepository extends PagingAndSortingRepository<WPSAuthentication, String> {
}
