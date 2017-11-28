package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.messaging.Message;

/**
 * CRUD repository for Message entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "messageRepository")
@Transactional
public interface MessageRepository extends PagingAndSortingRepository<Message, String> {
    /**
     * Find Message entity by user identifier
     * @param userId - the given user identifier
     * @return the corresponding Message entity
     */
    Message findByUserId(Integer userId);

    /**
     * Find Message entity by source
     * @param source - the given source
     * @return the corresponding Message entity
     */
    Message findBySource(String source);
}
