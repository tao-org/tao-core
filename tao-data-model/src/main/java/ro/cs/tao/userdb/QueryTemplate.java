package ro.cs.tao.userdb;

import ro.cs.tao.component.Identifiable;

import java.util.Objects;

public class QueryTemplate implements Identifiable<String> {
    private String name;
    private String description;
    private String sql;

    public QueryTemplate() {
    }

    public QueryTemplate(String name, String description, String sql) {
        this.name = name;
        this.description = description;
        this.sql = sql;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public void setId(String id) {
        this.name = id;
    }

    @Override
    public String defaultId() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryTemplate that = (QueryTemplate) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
