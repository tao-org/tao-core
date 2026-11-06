package ro.cs.tao.configuration;

import ro.cs.tao.component.Identifiable;
import ro.cs.tao.datasource.param.JavaType;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserConfigurationItem implements Identifiable<UserConfigurationItemId> {
    private String userId;
    private String key;
    private String value;
    private String friendlyName;
    private String type;
    private ConfigurationCategory category;
    private String label;
    private Values values;
    private String dependsOnItemId;
    private String dependsOnItemValue;
    private LocalDateTime lastUpdated;

    public UserConfigurationItem() {
    }

    public UserConfigurationItem(String userId, ConfigurationItem item) {
        this.userId = userId;
        setItem(item);
    }

    @Override
    @Transient
    public UserConfigurationItemId getId() {
        return new UserConfigurationItemId(userId, key);
    }

    @Override
    public void setId(UserConfigurationItemId id) {
        if (id != null) {
            this.userId = id.getUserId();
            this.key = id.getKey();
        }
    }

    @Override
    public UserConfigurationItemId defaultId() {
        return null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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

    public void addAllowedValue(String value, String label) {
        if (this.values == null) {
            this.values = new Values();
        }
        this.values.addAllowedValue(value, label);
    }

    public String getDependsOnItemId() {
        return dependsOnItemId;
    }

    public void setDependsOnItemId(String dependsOnItemId) {
        this.dependsOnItemId = dependsOnItemId;
    }

    public String getDependsOnItemValue() {
        return dependsOnItemValue;
    }

    public void setDependsOnItemValue(String dependsOnItemValue) {
        this.dependsOnItemValue = dependsOnItemValue;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Transient
    public ConfigurationItem getItem() {
        final ConfigurationItem item = new ConfigurationItem();
        item.setId(key);
        item.setValue(value);
        item.setFriendlyName(friendlyName);
        item.setType(type);
        item.setCategory(category);
        item.setLabel(label);
        item.setValues(values);
        item.setDependsOnItemId(dependsOnItemId);
        item.setDependsOnItemValue(dependsOnItemValue);
        item.setLastUpdated(lastUpdated);
        item.setPerUser(true);
        return item;
    }

    @Transient
    public void setItem(ConfigurationItem item) {
        if (item != null) {
            this.key = item.getId();
            this.value = item.getValue();
            this.friendlyName = item.getFriendlyName();
            this.type = item.getType();
            this.category = item.getCategory();
            this.label = item.getLabel();
            this.values = item.getValues();
            this.dependsOnItemId = item.getDependsOnItemId();
            this.dependsOnItemValue = item.getDependsOnItemValue();
            this.lastUpdated = item.getLastUpdated();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserConfigurationItem that = (UserConfigurationItem) o;
        return Objects.equals(userId, that.userId) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, key);
    }
}
