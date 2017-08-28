package ro.cs.tao.datasource.param;

/**
 * @author Cosmin Cara
 */
public class ParameterDescriptor {
    private final String name;
    private final Class type;
    private final Object defaultValue;
    private final boolean required;

    public ParameterDescriptor(String name, Class type) {
        this(name, type, null, false);
    }

    public ParameterDescriptor(String name, Class type, boolean required) {
        this(name, type, null, required);
    }

    public ParameterDescriptor(String name, Class type, Object defaultValue) {
        this(name, type, defaultValue, false);
    }

    public ParameterDescriptor(String name, Class type, Object defaultValue, boolean required) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() { return defaultValue; }
}
