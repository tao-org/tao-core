package ro.cs.tao.agent;

import ro.cs.tao.component.StringIdentifiable;

import java.time.LocalDateTime;

/** One ordered message in a persisted conversation. */
public class ConversationMessage extends StringIdentifiable {
    private String conversationId;
    private String role;
    private String content;
    private boolean archived;
    private LocalDateTime created;
    private LocalDateTime lastUpdated;

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
