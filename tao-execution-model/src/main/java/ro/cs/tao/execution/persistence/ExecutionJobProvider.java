package ro.cs.tao.execution.persistence;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.persistence.EntityProvider;

import java.util.List;
import java.util.Set;

public interface ExecutionJobProvider extends EntityProvider<ExecutionJob, Long> {

    List<ExecutionJob> listByWorkflow(long workflowId);
    List<String> getWorkflowOutputKeys(long workflowId);
    List<String> getOutputKeys(long jobId);
    List<ExecutionJob> list(ExecutionStatus status);
    List<ExecutionJob> list(Set<ExecutionStatus> statuses);
    List<ExecutionJob> list(String userId, Set<ExecutionStatus> statuses);

}
