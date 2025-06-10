package ro.cs.tao.execution.model;

import org.ggf.drmaa.DeniedByDrmException;
import org.ggf.drmaa.DrmaaException;
import ro.cs.tao.component.NodeAffinity;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.execution.monitor.NodeManager;

/**
 * An execution strategy dictates where a task should be executed.
 * If multiple execution nodes are available, there may be many choices on what node is chosen for a specific task.
 *
 * @author  Cosmin Cara
 * @since   1.4.9
 */
public class ExecutionStrategy {
    private final ExecutionStrategyType type;
    private final String hostName;
    private final ExecutionTask task;

    /**
     * Retrieves the execution strategy for a given execution task based on the configured execution strategy type
     * and the node affinity settings of associated tasks. Determines the appropriate host name, if applicable,
     * to guide the task execution strategy.
     *
     * @param task the execution task for which the execution strategy should be determined
     * @return an instance of {@code ExecutionStrategy} corresponding to the configured strategy type
     *         and the associated node affinity, if any
     */
    public static ExecutionStrategy getExecutionStrategy(ExecutionTask task) {
        final ExecutionStrategyType strategyType = Enum.valueOf(ExecutionStrategyType.class,
                                                                ConfigurationManager.getInstance()
                                                                        .getValue("execution.strategy",
                                                                                "DISTRIBUTE"));
        final ExecutionTask tmpTask = task.getJob().getTasks().stream()
                .filter(t -> t instanceof ProcessingExecutionTask &&
                             t.getExecutionNodeHostName() != null &&
                             !t.getExecutionNodeHostName().equalsIgnoreCase(NodeAffinity.SameNode.getValue()))
                .findFirst().orElse(null);
        return create(task, strategyType, tmpTask != null ? tmpTask.getExecutionNodeHostName() : null);
    }

    /**
     * Constructs an execution strategy for a given execution task and strategy type.
     *
     * @param task the execution task to be mapped to a strategy
     * @param type the type of execution strategy to apply
     * @return an instance of {@code ExecutionStrategy} configured with the specified task and type
     */
    static ExecutionStrategy create(ExecutionTask task, ExecutionStrategyType type) {
        return create(task, type, null);
    }

    /**
     * Creates an instance of {@code ExecutionStrategy} based on the provided execution task,
     * strategy type, and host name.
     *
     * @param task the execution task to be associated with the created execution strategy
     * @param type the type of execution strategy to apply
     * @param hostName the host name associated with the execution strategy
     * @return an instance of {@code ExecutionStrategy} configured with the specified task,
     *         type, and host name
     */
    static ExecutionStrategy create(ExecutionTask task, ExecutionStrategyType type, String hostName) {
        return new ExecutionStrategy(task, type, hostName);
    }

    /**
     * Constructs an instance of {@code ExecutionStrategy} based on the provided execution task,
     * execution strategy type, and host name.
     *
     * @param task the execution task to be associated with the created execution strategy
     * @param type the type of execution strategy to apply
     * @param hostName the host name associated with the execution strategy
     */
    ExecutionStrategy(ExecutionTask task, ExecutionStrategyType type, String hostName) {
        this.type = type;
        this.hostName = hostName;
        this.task = task;
    }

    /**
     * Retrieves the execution strategy type associated with this instance.
     *
     * @return the execution strategy type represented by an instance of {@code ExecutionStrategyType}
     */
    public ExecutionStrategyType getType() {
        return type;
    }

    /**
     * Retrieves the host name associated with this execution strategy instance.
     *
     * @return the host name as a String, or null if no host name is configured
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Retrieves a {@code NodeData} instance that represents the node where the task will execute,
     * based on the provided memory constraints and configuration of the execution strategy.
     * If the hostName is not set, it attempts to find an available node that satisfies the required specifications.
     *
     * @param memory the amount of memory required, used for node selection if memory constraints are applied
     * @return a {@code NodeData} object containing details of the allocated node, its CPU count, and memory
     * @throws DrmaaException if an error occurs during the process of retrieving or allocating the node
     */
    public NodeData getNode(long memory) throws DrmaaException {
        NodeData nodeData = null;
        if (this.hostName != null) {
            if (NodeManager.isAvailable() && NodeManager.getInstance() != null) {
                nodeData = NodeManager.getInstance().getNode(this.hostName);
            }
            if (nodeData == null) {
                throw new DeniedByDrmException(String.format("Node '%s' was not found", this.hostName));
            }
        } else {
            if (task instanceof ScriptTask) {
                nodeData = NodeManager.getInstance().getMasterNode();
            } else {
                // the next call blocks until a node is available
                nodeData = getAvailableNode(task.getJob().getUserId(), ((ProcessingExecutionTask) task).getComponent().getParallelism(),
                        ExecutionConfiguration.forceMemoryConstraint() ? memory : 0L);
            }
        }
        return nodeData;
    }

    private NodeData getAvailableNode(String user, int cpus, long memory) throws DrmaaException {
        NodeData nodeData;
        if (NodeManager.isAvailable()) {
            nodeData = NodeManager.getInstance().getAvailableNode(user, cpus, memory, this);
        } else {
            nodeData = NodeManager.getInstance().getMasterNode();
        }
        if (nodeData == null) {
            throw new DeniedByDrmException("No node was found for execution");
        }
        return nodeData;
    }
}
