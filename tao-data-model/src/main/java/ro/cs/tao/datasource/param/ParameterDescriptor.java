package ro.cs.tao.datasource.param;

/**
 * @author Cosmin Cara
 */
public class ParameterDescriptor {
    private final String name;
    private final Class type;
    private final boolean required;

    public ParameterDescriptor(String name, Class type) {
        this(name, type, false);
    }

    public ParameterDescriptor(String name, Class type, boolean required) {
        this.name = name;
        this.type = type;
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
}
