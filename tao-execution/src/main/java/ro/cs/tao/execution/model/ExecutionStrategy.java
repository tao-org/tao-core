package ro.cs.tao.execution.model;

import org.ggf.drmaa.DeniedByDrmException;
import org.ggf.drmaa.DrmaaException;
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

    public static ExecutionStrategy getExecutionStrategy(ExecutionTask task) {
        final ExecutionStrategyType strategyType = Enum.valueOf(ExecutionStrategyType.class,
                                                                ConfigurationManager.getInstance()
                                                                        .getValue("execution.strategy",
                                                                                "DISTRIBUTE"));
        final ExecutionTask tmpTask = task.getJob().getTasks().stream()
                .filter(t -> t instanceof ProcessingExecutionTask &&
                             t.getExecutionNodeHostName() != null &&
                             !t.getExecutionNodeHostName().equalsIgnoreCase("same"))
                .findFirst().orElse(null);
        return create(task, strategyType, tmpTask != null ? tmpTask.getExecutionNodeHostName() : null);
    }

    static ExecutionStrategy create(ExecutionTask task, ExecutionStrategyType type) {
        return create(task, type, null);
    }

    static ExecutionStrategy create(ExecutionTask task, ExecutionStrategyType type, String hostName) {
        return new ExecutionStrategy(task, type, hostName);
    }

    ExecutionStrategy(ExecutionTask task, ExecutionStrategyType type, String hostName) {
        this.type = type;
        this.hostName = hostName;
        this.task = task;
    }

    public ExecutionStrategyType getType() {
        return type;
    }

    public String getHostName() {
        return hostName;
    }

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
