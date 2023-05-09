package ro.cs.tao.execution.model;

import java.util.HashMap;
import java.util.Map;

public class ExecutionRequest {
    private long workflowId;
    private String name;
    private String label;
    private String jobType;
    private Map<Long, String> inputs;
    private Map<String, Map<String, String>> parameters;

    public ExecutionRequest() {
    }

    public ExecutionRequest(ExecutionRequest other) {
        this.workflowId = other.workflowId;
        this.name = other.name;
        this.label = other.label;
        this.jobType = other.jobType;
        if (other.inputs != null) {
            this.inputs = new HashMap<>(other.inputs);
        }
        if (other.parameters != null) {
            this.parameters = new HashMap<>(other.parameters);
        }
    }

    public long getWorkflowId() { return workflowId; }
    public void setWorkflowId(long workflowId) { this.workflowId = workflowId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Map<Long, String> getInputs() {
        return inputs;
    }

    public void setInputs(Map<Long, String> inputs) {
        this.inputs = inputs;
    }

    public Map<String, Map<String, String>> getParameters() { return parameters; }
    public void setParameters(Map<String, Map<String, String>> parameters) { this.parameters = parameters; }
}
