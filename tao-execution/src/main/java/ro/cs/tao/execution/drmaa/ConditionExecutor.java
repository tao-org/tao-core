package ro.cs.tao.execution.drmaa;

import ro.cs.tao.component.ConditionComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.model.ConditionExecutionTask;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.execution.util.TaskUtilities;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The {@code ConditionExecutor} class is a specific implementation of {@code Executor}
 * for handling condition-based execution tasks. It processes tasks of type
 * {@code ConditionExecutionTask}, validates their configuration, and manages their
 * execution lifecycle, including start, stop, and error handling.
 */
public class ConditionExecutor extends Executor<ConditionExecutionTask> {

    private static ExecutionTaskProvider taskProvider;

    /**
     * Sets the task provider for the {@code ConditionExecutor}.
     *
     * @param taskProvider the {@link ExecutionTaskProvider} to be set as the task provider
     */
    public static void setTaskProvider(ExecutionTaskProvider taskProvider) {
        ConditionExecutor.taskProvider = taskProvider;
    }
    public ConditionExecutor() {
        super();
        if (taskProvider == null) {
            throw new ExecutionException("A TaskProvider is required to use this executor");
        }
    }

    @Override
    public boolean supports(TaoComponent component) {
        return component instanceof ConditionComponent;
    }

    @Override
    public void execute(ConditionExecutionTask task) throws ExecutionException {
        try {
            task.setResourceId(UUID.randomUUID().toString());
            logger.fine(String.format("Received task %s", task.getComponent().getId()));
            task.setExecutionNodeHostName(InetAddress.getLocalHost().getHostName());
            task.setStartTime(LocalDateTime.now());
            task.setExecutionStatus(ExecutionStatus.RUNNING);
            taskProvider.update(task);
            final Set<String> conditions = task.getComponent().getParameterDescriptors().stream().map(d -> d.getName()).collect(Collectors.toSet());
            int expectedSize = conditions.size() + 1;
            final List<Variable> inputParameterValues = task.getInputParameterValues();
            if (inputParameterValues.isEmpty() || inputParameterValues.size() != expectedSize) {
                throw new ExecutionException(String.format("Invalid cardinality (must be %d)", expectedSize));
            }
            final Variable input = inputParameterValues.stream().filter(p -> !conditions.contains(p.getKey())).findFirst().orElseThrow();
            task.setOutputParameterValue(task.buildExecutionCommand(), input.getValue());
            final List<String> removedTasks = TaskUtilities.removeUnmatchedConditionBranches(task);
            final String msg = String.format("Tasks %s were discarded", String.join(", ", removedTasks));
            task.setLog(msg);
            //Messaging.send(task.getJob().getUserId(), Topic.INFORMATION.value(), msg);
            markTaskFinished(task, ExecutionStatus.DONE);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            task.setLog(ex.getMessage());
            markTaskFinished(task, ExecutionStatus.FAILED, ex.getMessage());
        }
    }

    @Override
    public String defaultId() { return "ConditionExecutor"; }

    @Override
    public void stop(ConditionExecutionTask task) throws ExecutionException {
        markTaskFinished(task, ExecutionStatus.CANCELLED, ExecutionStatus.CANCELLED.friendlyName());
    }

    @Override
    public void suspend(ConditionExecutionTask task) throws ExecutionException {
        throw new ExecutionException("suspend() not supported on conditions");
    }

    @Override
    public void resume(ConditionExecutionTask task) throws ExecutionException {
        throw new ExecutionException("resume() not supported on conditions");
    }

    @Override
    public void monitorExecutions() throws ExecutionException {
        this.isInitialized.set(false);
    }
}
