package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.messaging.Message;

import java.util.List;

/**
 * CRUD repository for Message entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "messageRepository")
@Transactional
public interface MessageRepository extends PagingAndSortingRepository<Message, Long> {

    /**
     * Find Message entity by user identifier
     * @param userName - the given user identifier
     * @return the corresponding Message entity
     */
    Page<Message> findByUser(String userName, Pageable pageRequest);

    @Query(value = "SELECT * FROM common.notification WHERE username = :user AND read = false", nativeQuery = true)
    List<Message> getUnreadMessages(@Param("user") String userName);

    @Query(value = "SELECT * FROM common.notification WHERE username = :user AND timestamp = :timestamp", nativeQuery = true)
    Message get(@Param("user") String userName, @Param("timestamp") long timestamp);

    @Modifying
    @Query(value = "UPDATE common.notification SET read = true WHERE id IN (:ids)", nativeQuery = true)
    void markAsRead(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "DELETE FROM common.notification WHERE username = :user", nativeQuery = true)
    void deleteAll(@Param("user") String userName);
}
