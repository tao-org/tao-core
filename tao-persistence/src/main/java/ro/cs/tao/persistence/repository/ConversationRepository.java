package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.agent.Conversation;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findByUserIdOrderByLastUpdatedDesc(String userId);
    Conversation findByIdAndUserId(String id, String userId);
}
