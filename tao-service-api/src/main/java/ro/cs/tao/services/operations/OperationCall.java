package ro.cs.tao.services.operations;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The OperationCall class represents a request for an operation with specific parameters.
 * It encapsulates details such as the operation's identifier, name, arguments, and an idempotency key.
 * This class provides methods to access and modify these details, enabling the execution of operations
 * with specified arguments, and ensuring idempotency through a unique key.
 */
public class OperationCall {
    private String id;
    private String name;
    private Map<String, Object> arguments = new LinkedHashMap<>();
    private String idempotencyKey;

    public OperationCall() {
    }

    public OperationCall(String id, String name, Map<String, Object> arguments) {
        this.id = id;
        this.name = name;
        if (arguments != null) {
            this.arguments = arguments;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments != null ? arguments : new LinkedHashMap<>();
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}

