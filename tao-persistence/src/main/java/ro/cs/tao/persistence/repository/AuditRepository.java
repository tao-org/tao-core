package ro.cs.tao.persistence.repository;

import jakarta.persistence.QueryHint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.user.LogEvent;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Repository
@Qualifier(value = "auditRepository")
@Transactional
public interface AuditRepository extends JpaRepository<LogEvent, Long> {

    @Query(value = "SELECT * FROM common.audit WHERE username = :user", nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    List<LogEvent> getByUser(@Param("user") String user);

    @Query(value = "SELECT * FROM common.audit WHERE username = :user AND " +
            "timestamp BETWEEN :startDate AND :endDate", nativeQuery = true)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    List<LogEvent> getByUserAndInterval(@Param("user") String user,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

}


