package ro.cs.tao.services.operations;

import ro.cs.tao.services.interfaces.TAOService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents an abstract operation that can be performed using a specified service.
 * Besides the main service defined by the parameter of the type, if additional services are needed,
 * they can be obtained by using ObjectProvider instances.
 *
 * @param <S> the type of service the operation works with, extending from TAOService
 */
public abstract class Operation<S extends TAOService> {

    protected static final String IDEMPOTENCY_KEY = "idempotencyKey";

    protected S service;

    public abstract void setService(S service);

    public String name() {
        return definition().getQualifiedName();
    }

    public abstract OperationDescriptor definition();

    public OperationResult execute(String userId, Map<String, Object> arguments) {
        // The idempotency key is handled by the caller and may be passed among the arguments
        final Map<String, Object> values = new LinkedHashMap<>();
        if (arguments != null) {
            arguments.forEach((name, value) -> {
                if (!IDEMPOTENCY_KEY.equals(name)) {
                    values.put(name, value);
                }
            });
        }
        List<OperationDescriptor.Parameter> parameters = definition().getParameters();
        if (parameters != null) {
            Map<String, OperationDescriptor.Parameter> parameterMap = parameters.stream()
                                                                                .collect(Collectors.toMap(OperationDescriptor.Parameter::getName,
                                                                                                          Function.identity()));
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String name = entry.getKey();
                OperationDescriptor.Parameter parameter = parameterMap.get(name);
                if (parameter == null) {
                    return OperationResult.fail("BAD_REQUEST", String.format("Unknown parameter '%s'", name));
                }
                try {
                    entry.setValue(convert(entry.getValue(), parameter.getType()));
                } catch (Exception e) {
                    return OperationResult.fail("BAD_REQUEST", String.format("Parameter '%s' is not of expected type '%s'",
                                                                             name, parameter.getTypeName()));
                }
            }
            final List<String> missing = parameters.stream()
                                                   .filter(p -> p.isRequired() && values.get(p.getName()) == null)
                                                   .map(OperationDescriptor.Parameter::getName)
                                                   .collect(Collectors.toList());
            if (!missing.isEmpty()) {
                return OperationResult.fail("BAD_REQUEST", "Required parameter(s) missing: " + String.join(", ", missing));
            }
        } else if (!values.isEmpty()) {
            return OperationResult.fail("BAD_REQUEST", "This operation does not accept parameters");
        }
        try {
            return OperationResult.ok(executeImpl(userId, values));
        } catch (IllegalArgumentException e) {
            return OperationResult.fail("BAD_REQUEST", e.getMessage());
        }
    }

    /**
     * Converts a value received from the LLM (i.e. deserialized from JSON) to the declared type of a parameter,
     * when the conversion is lossless (e.g. an integral number to Long, a number to String, "true" to Boolean).
     *
     * @throws ClassCastException if the value cannot be converted
     */
    protected Object convert(Object value, Class<?> type) {
        if (value == null || type == null || type.isInstance(value)) {
            return value;
        }
        if (Long.class.equals(type) || Integer.class.equals(type) || Short.class.equals(type) || Byte.class.equals(type)) {
            final long number;
            if (value instanceof Number && ((Number) value).doubleValue() == Math.rint(((Number) value).doubleValue())) {
                number = ((Number) value).longValue();
            } else if (value instanceof String) {
                try {
                    number = Long.parseLong(((String) value).trim());
                } catch (NumberFormatException e) {
                    throw new ClassCastException();
                }
            } else {
                throw new ClassCastException();
            }
            if (Long.class.equals(type)) {
                return number;
            } else if (Integer.class.equals(type)) {
                return Math.toIntExact(number);
            } else if (Short.class.equals(type)) {
                return (short) Math.toIntExact(number);
            }
            return (byte) Math.toIntExact(number);
        }
        if (Double.class.equals(type) || Float.class.equals(type) || Number.class.equals(type)) {
            final double number;
            if (value instanceof Number) {
                number = ((Number) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    number = Double.parseDouble(((String) value).trim());
                } catch (NumberFormatException e) {
                    throw new ClassCastException();
                }
            } else {
                throw new ClassCastException();
            }
            return Float.class.equals(type) ? (Object) (float) number : (Object) number;
        }
        if (Boolean.class.equals(type) && value instanceof String) {
            final String text = ((String) value).trim();
            if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
                return Boolean.parseBoolean(text);
            }
        }
        if (String.class.equals(type) && (value instanceof Number || value instanceof Boolean)) {
            return String.valueOf(value);
        }
        throw new ClassCastException();
    }

    protected abstract Object executeImpl(String userId, Map<String, Object> parameters);

    protected String requiredString(Map<String, Object> parameters, String name) {
        Object value = parameters.get(name);
        String text = optionalString(value, name);
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(String.format("Parameter '%s' cannot be empty", name));
        }
        return text;
    }

    protected String optionalString(Object value, String name) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(String.format("Parameter '%s' is not of expected type 'string'", name));
        }
        return ((String) value).trim();
    }

    protected long longValue(Object value, String name) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong(((String) value).trim());
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException(String.format("Parameter '%s' is not of expected type 'integer'", name));
            }
        }
        throw new IllegalArgumentException(String.format("Parameter '%s' is not of expected type 'integer'", name));
    }

    protected float floatValue(Object value, String name) {
        if (value == null) {
            return 0.0f;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            try {
                return Float.parseFloat(((String) value).trim());
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException(String.format("Parameter '%s' is not of expected type 'number'", name));
            }
        }
        throw new IllegalArgumentException(String.format("Parameter '%s' is not of expected type 'number'", name));
    }

    protected boolean booleanValue(Object value, boolean defaultValue, String name) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        throw new IllegalArgumentException(String.format("Parameter '%s' is not of expected type 'boolean'", name));
    }
}

