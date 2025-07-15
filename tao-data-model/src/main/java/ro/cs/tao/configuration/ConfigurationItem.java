package ro.cs.tao.configuration;

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.datasource.param.JavaType;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a configurable item with a unique identifier, a value, and associated metadata.
 * The configuration item can have a type, category, label, friendly name, and possible values
 * that describe its characteristics and constraints.
 *
 * This class allows for the management of configuration data, including the ability to
 * parse and set values based on their declared type.
 *
 * Extends the {@code StringIdentifiable} abstract class to provide a string-based unique identifier.
 *
 * Fields:
 * - value: Represents the current value of the configuration item.
 * - friendlyName: A user-friendly name used for display purposes.
 * - type: Specifies the type of the configuration value.
 * - category: Defines the category to which the configuration belongs.
 * - label: A label associated with the configuration item, typically for display.
 * - values: Specifies the allowed values or bounds for the configuration item.
 * - lastUpdated: Indicates the date and time when the configuration item was last updated.
 *
 * Methods:
 * - getValue(): Gets the current value of the configuration item.
 * - setValue(String value): Sets the current value of the configuration item.
 * - getTypedValue(): Retrieves the typed representation of the value according to its defined type.
 * - setTypedValue(Object typedValue): Sets the value using a typed object, ensuring compatibility with the defined type.
 * - getFriendlyName(): Retrieves the user-friendly name of the configuration item.
 * - setFriendlyName(String friendlyName): Sets the user-friendly name of the configuration item.
 * - getType(): Gets the type of the configuration item's value.
 * - setType(String type): Sets the type of the configuration item's value.
 * - getCategory(): Gets the category of the configuration item.
 * - setCategory(ConfigurationCategory category): Sets the category of the configuration item.
 * - getLabel(): Retrieves the label associated with the configuration item.
 * - setLabel(String label): Sets the label associated with the configuration item.
 * - getValues(): Retrieves the allowed values or bounds for this configuration item.
 * - setValues(Values values): Sets the allowed values or bounds for this configuration item.
 * - getLastUpdated(): Returns the timestamp of the last update applied to the configuration item.
 * - setLastUpdated(LocalDateTime lastUpdated): Updates the timestamp of the last modification of this configuration item.
 * - equals(Object o): Compares this configuration item with another object for equality based on its identifier, value, type, and category.
 * - hashCode(): Computes the hash code of the configuration item using its identifier, value, type, and category.
 */
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
