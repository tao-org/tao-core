package ro.cs.tao.execution.drmaa;

import ro.cs.tao.component.ConditionComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.model.ConditionExecutionTask;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConditionExecutor extends Executor<ConditionExecutionTask> {

    private static ExecutionTaskProvider taskProvider;

    public static void setTaskProvider(ExecutionTaskProvider taskProvider) {
        ConditionExecutor.taskProvider = taskProvider;
    }

    @Override
    public String defaultId() {
        return "ConditionExecutor";
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
            final List<Variable> inputParameterValues = task.getInputParameterValues();
            if (inputParameterValues.isEmpty() || inputParameterValues.size() != 1) {
                throw new ExecutionException("Invalid cardinality (must be 1)");
            }
            final Variable input = inputParameterValues.get(0);
            task.setOutputParameterValue(task.buildExecutionCommand(), input.getValue());
            markTaskFinished(task, ExecutionStatus.DONE);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            task.setLog(ex.getMessage());
            markTaskFinished(task, ExecutionStatus.FAILED, ex.getMessage());
        }
    }

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
