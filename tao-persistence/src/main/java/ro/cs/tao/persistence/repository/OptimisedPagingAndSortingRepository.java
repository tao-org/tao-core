package ro.cs.tao.persistence.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface OptimisedPagingAndSortingRepository<T, ID> extends JpaRepository<T, ID> {
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Override
    List<T> findAll(Sort sort);

    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Override
    Page<T> findAll(Pageable pageable);

    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Override
    List<T> findAll();

    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Override
    List<T> findAllById(Iterable<ID> ids);
}


