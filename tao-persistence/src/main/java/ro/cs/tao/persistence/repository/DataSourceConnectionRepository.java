package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.DataSourceConnection;

import java.util.List;

/**
 * CRUD repository for DataSourceConnection entities
 *
 * @author Cosmin Cara
 *
 */
@Repository
@Qualifier(value = "dataSourceConnectionRepository")
@Transactional
public interface DataSourceConnectionRepository extends PagingAndSortingRepository<DataSourceConnection, Long> {

    List<DataSourceConnection> findByUserId(int userId);

    DataSourceConnection findByUserIdAndDataSource(int userId, String dataSource);
}
