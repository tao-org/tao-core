package ro.cs.tao.persistence.managers;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.agent.ConversationMessage;
import ro.cs.tao.persistence.repository.ConversationMessageRepository;

import java.util.List;

@Component
public class ConversationMessageManager extends EntityManager<ConversationMessage, String, ConversationMessageRepository> {
    public List<ConversationMessage> getByConversation(String id) { return repository.findByConversationIdOrderByCreatedAsc(id); }
    public List<ConversationMessage> getActiveByConversation(String id) { return repository.findByConversationIdAndArchivedFalseOrderByCreatedAsc(id); }
    public long countActive(String id) { return repository.countByConversationIdAndArchivedFalse(id); }
    @Transactional public void deleteByConversation(String id) { repository.deleteByConversationId(id); }
    @Override protected String identifier() { return "created"; }
    @Override protected boolean checkEntity(ConversationMessage value) {
        return value != null && value.getId() != null && value.getConversationId() != null
                && value.getRole() != null && value.getContent() != null;
    }
}
