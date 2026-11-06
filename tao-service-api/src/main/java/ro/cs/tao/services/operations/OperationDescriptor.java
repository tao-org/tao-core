package ro.cs.tao.services.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The OperationDescriptor class provides a detailed representation of an operation,
 * including its category, name, description, and parameter information. It serves
 * as a descriptor for operations that can be executed by a LLM.
 *
 * It supports a variety of attributes such as whether the operation is mutating or
 * not, what parameters are required, and what parameter types are required for execution.
 */
public class OperationDescriptor {
    private OperationCategory category;
    private String name;
    private String description;
    private boolean mutating;
    private List<Parameter> parameters;
    private List<String> requiredParameters;

    public OperationDescriptor() {
    }

    public OperationDescriptor(OperationCategory category, String name, String description, boolean mutating) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.mutating = mutating;
    }

    public OperationCategory getCategory() {
        return category;
    }

    public void setCategory(OperationCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifiedName() {
        return this.category != null && this.name != null
                ? this.category.friendlyName() + "." + this.name
                : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputSchema() {
        return toString();
    }

    public boolean isMutating() {
        return mutating;
    }

    public void setMutating(boolean mutating) {
        this.mutating = mutating;
    }

    public void addParameter(String name, Class type, boolean required) {
        addParameter(name, type, required, null);
    }

    /**
     * Adds a parameter with a description that guides the LLM about the expected value (format, allowed values, etc.).
     */
    public void addParameter(String name, Class type, boolean required, String description) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.add(new Parameter(name, type, required, description));
        if (required) {
            if (this.requiredParameters == null) {
                this.requiredParameters = new ArrayList<>();
            }
            this.requiredParameters.add(name);
        }
    }

    public List<Parameter> getParameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"object\",");
        if (this.requiredParameters != null && !this.requiredParameters.isEmpty()) {
            builder.append("\"required\":[");
            for (String rParam : this.requiredParameters) {
                builder.append("\"").append(rParam).append("\",");
            }
            builder.setLength(builder.length() - 1);
            builder.append("],");
        }
        builder.append("\"properties\":{");
        if (this.parameters != null && !this.parameters.isEmpty()) {
            for (Parameter param : this.parameters) {
                builder.append("\"").append(param.getName()).append("\":{")
                       .append("\"type\":\"").append(param.getTypeName()).append("\"");
                if (param.getDescription() != null && !param.getDescription().isEmpty()) {
                    builder.append(",\"description\":\"").append(escape(param.getDescription())).append("\"");
                }
                builder.append("},");
            }
            builder.setLength(builder.length() - 1);
        }
        builder.append("}}");
        return builder.toString();
    }

    private static String escape(String text) {
        final StringBuilder builder = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"': builder.append("\\\""); break;
                case '\\': builder.append("\\\\"); break;
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                case '\t': builder.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
            }
        }
        return builder.toString();
    }

    public class Parameter {
        private final String name;
        private final Class type;
        private final boolean required;
        private final String description;

        public Parameter(String name, Class type, boolean required) {
            this(name, type, required, null);
        }

        public Parameter(String name, Class type, boolean required, String description) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.description = description;
        }

        public String getDescription() {
            return description;
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

        /**
         * Returns the JSON Schema type corresponding to the Java type of the parameter.
         */
        public String getTypeName() {
            if (Long.class.equals(this.type) || Integer.class.equals(this.type) ||
                Short.class.equals(this.type) || Byte.class.equals(this.type)) {
                return "integer";
            } else if (Number.class.isAssignableFrom(this.type)) {
                return "number";
            } else if (Boolean.class.equals(this.type)) {
                return "boolean";
            } else if (String.class.equals(this.type)) {
                return "string";
            } else if (Collection.class.isAssignableFrom(this.type) || this.type.isArray()) {
                return "array";
            } else {
                return "object";
            }
        }
    }
}

