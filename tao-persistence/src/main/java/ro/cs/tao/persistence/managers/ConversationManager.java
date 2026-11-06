package ro.cs.tao.persistence.managers;

import org.springframework.stereotype.Component;
import ro.cs.tao.agent.Conversation;
import ro.cs.tao.persistence.repository.ConversationRepository;

import java.util.List;

@Component
public class ConversationManager extends EntityManager<Conversation, String, ConversationRepository> {
    public List<Conversation> getByUser(String userId) { return repository.findByUserIdOrderByLastUpdatedDesc(userId); }
    public Conversation getByIdAndUser(String id, String userId) { return repository.findByIdAndUserId(id, userId); }
    @Override protected String identifier() { return "lastUpdated"; }
    @Override protected boolean checkEntity(Conversation value) {
        return value != null && value.getId() != null && value.getUserId() != null && value.getProvider() != null;
    }
}
