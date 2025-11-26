package ro.cs.tao.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.QueryHint;

@NoRepositoryBean
public interface OptimisedPagingAndSortingRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Override
    Iterable<T> findAll(Sort sort);

    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Override
    Page<T> findAll(Pageable pageable);

    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Override
    Iterable<T> findAll();

    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Override
    Iterable<T> findAllById(Iterable<ID> ids);
}
