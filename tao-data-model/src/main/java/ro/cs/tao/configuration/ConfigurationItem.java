package ro.cs.tao.configuration;

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.datasource.param.JavaType;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.Objects;

public class ConfigurationItem extends StringIdentifiable {
    private String value;
    private String friendlyName;
    private String type;
    private ConfigurationCategory category;
    private String label;
    private Values values;
    private LocalDateTime lastUpdated;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Transient
    public Object getTypedValue() {
        return JavaType.fromFriendlyName(this.type).parse(this.value);
    }

    @Transient
    public void setTypedValue(Object typedValue) {
        final JavaType javaType = JavaType.fromFriendlyName(this.type);
        if (!javaType.value().equals(typedValue.getClass())) {
            throw new ClassCastException(typedValue.getClass().getName() + " cannot be casted to " + javaType.value().getName());
        }
        this.value = typedValue.toString();
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ConfigurationCategory getCategory() {
        return category;
    }

    public void setCategory(ConfigurationCategory category) {
        this.category = category;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Values getValues() {
        return values;
    }

    public void setValues(Values values) {
        this.values = values;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationItem that = (ConfigurationItem) o;
        return Objects.equals(id, that.id) && Objects.equals(value, that.value) && Objects.equals(type, that.type) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, type, category);
    }
}
