package ro.cs.tao.agent;

import ro.cs.tao.component.StringIdentifiable;

import java.time.LocalDateTime;

/** A persisted conversation between one user and an LLM provider. */
public class Conversation extends StringIdentifiable {
    private String userId;
    private String provider;
    private String name;
    private String summary;
    private LocalDateTime created;
    private LocalDateTime lastUpdated;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
