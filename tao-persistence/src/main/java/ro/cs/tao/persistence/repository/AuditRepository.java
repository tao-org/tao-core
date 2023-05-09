package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.user.LogEvent;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Repository
@Qualifier(value = "auditRepository")
@Transactional
public interface AuditRepository extends PagingAndSortingRepository<LogEvent, Long> {

    @Query(value = "SELECT * FROM common.audit WHERE username = :user", nativeQuery = true)
    List<LogEvent> getByUser(@Param("user") String user);

    @Query(value = "SELECT * FROM common.audit WHERE username = :user AND " +
            "timestamp BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<LogEvent> getByUserAndInterval(@Param("user") String user,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
