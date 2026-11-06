package ro.cs.tao.userdb;

import ro.cs.tao.component.Identifiable;

import java.time.LocalDateTime;
import java.util.Objects;

public class SavedQuery implements Identifiable<SavedQueryId> {
    private String name;
    private String userId;
    private String description;
    private String sql;
    private LocalDateTime created;
    private LocalDateTime lastUpdated;

    public SavedQuery() {
    }

    public SavedQuery(String name, String userId, String description, String sql) {
        this.name = name;
        this.userId = userId;
        this.description = description;
        this.sql = sql;
    }

    @Override
    public SavedQueryId getId() {
        return new SavedQueryId(name, userId);
    }

    @Override
    public void setId(SavedQueryId id) {
        if (id != null) {
            this.name = id.getName();
            this.userId = id.getUserId();
        }
    }

    @Override
    public SavedQueryId defaultId() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SavedQuery that = (SavedQuery) o;
        return Objects.equals(name, that.name) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, userId);
    }
}
