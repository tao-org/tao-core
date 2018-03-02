package ro.cs.tao.component.execution;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.Variable;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExecutionGroup extends ExecutionTask {

    private List<ExecutionTask> tasks;

    @XmlTransient
    public List<ExecutionTask> getTasks() { return tasks; }
    public void setTasks(List<ExecutionTask> tasks) { this.tasks = tasks; }

    public ExecutionGroup() { }

    public ExecutionGroup(ProcessingComponent processingComponent) {
        throw new IllegalArgumentException("Cannot assign a component to a task group");
    }

    @Override
    public void setParameterValue(String parameterId, String value) {
        super.setParameterValue(parameterId, value);
        if (this.tasks != null && this.tasks.size() > 0) {
            this.tasks.get(0).setParameterValue(parameterId, value);
        }
    }

    @Override
    public void setInputParameterValues(List<Variable> inputParameterValues) {
        super.setInputParameterValues(inputParameterValues);
        if (this.tasks != null && this.tasks.size() > 0) {
            this.tasks.get(0).setInputParameterValues(inputParameterValues);
        }
    }

    @Override
    public String buildExecutionCommand() {
        throw new java.lang.UnsupportedOperationException("Operation not permitted on a task group");
    }

    public void addTask(ExecutionTask task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        if (!contains(task)) {
            this.tasks.add(task);
        }
        task.setGroupTask(this);
    }

    public void removeTask(ExecutionTask task) {
        if (contains(task)) {
            Iterator<ExecutionTask> iterator = this.tasks.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getId().equals(task.getId())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Override
    public ExecutionTask getNext() {
        ExecutionTask next = null;
        if (this.tasks != null && this.tasks.size() > 0) {
            switch (this.executionStatus) {
                case UNDETERMINED:
                case QUEUED_ACTIVE:
                    next = this.tasks.get(0);
                    break;
                case SUSPENDED:
                    next = this.tasks.stream()
                                     .filter(t -> t.getExecutionStatus() == ExecutionStatus.SUSPENDED)
                                     .findFirst().orElse(null);
                    break;
                case RUNNING:
                    for (ExecutionTask task : this.tasks) {
                        if (task.getExecutionStatus() != ExecutionStatus.RUNNING) {
                            next = task;
                            break;
                        }
                    }
                    break;
                case DONE:
                case FAILED:
                case CANCELLED:
                default:
                    break;
            }
        }
        return next;
    }

    public ExecutionTask getByWorkflowNode(Long workflowNodeId) {
        ExecutionTask task = null;
        if (this.tasks != null) {
            task = this.tasks.stream()
                    .filter(t -> t.getWorkflowNodeId() != null && t.getWorkflowNodeId().equals(workflowNodeId))
                    .findFirst().orElse(null);
        }
        return task;
    }

    private boolean contains(ExecutionTask task) {
        return this.tasks != null && task != null && this.tasks.stream().anyMatch(t -> t.getId().equals(task.getId()));
    }
}
