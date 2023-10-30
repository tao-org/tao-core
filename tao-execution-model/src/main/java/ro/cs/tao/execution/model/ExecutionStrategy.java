package ro.cs.tao.execution.model;

public class ExecutionStrategy {
    private final ExecutionStrategyType type;
    private final String hostName;

    public static ExecutionStrategy create(ExecutionStrategyType type) {
        return create(type, null);
    }

    public static ExecutionStrategy create(ExecutionStrategyType type, String hostName) {
        return new ExecutionStrategy(type, hostName);
    }

    ExecutionStrategy(ExecutionStrategyType type, String hostName) {
        this.type = type;
        this.hostName = hostName;
    }

    public ExecutionStrategyType getType() {
        return type;
    }

    public String getHostName() {
        return hostName;
    }
}
