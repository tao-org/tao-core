package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.agent.ConversationMessage;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, String> {
    List<ConversationMessage> findByConversationIdOrderByCreatedAsc(String conversationId);
    List<ConversationMessage> findByConversationIdAndArchivedFalseOrderByCreatedAsc(String conversationId);
    long countByConversationIdAndArchivedFalse(String conversationId);
    void deleteByConversationId(String conversationId);
}
