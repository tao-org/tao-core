package ro.cs.tao.execution.model;

import ro.cs.tao.component.*;
import ro.cs.tao.component.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class WPSExecutionTask extends ExecutionTask {

    private WPSComponent component;

    public WPSExecutionTask() { super(); }

    public WPSComponent getComponent() { return component; }

    public void setComponent(WPSComponent component) { this.component = component; }

    @Override
    public void setInputParameterValue(String parameterId, String value) {
        boolean descriptorExists = false;
        List<ParameterDescriptor> descriptorList = this.component.getParameters();
        for (ParameterDescriptor descriptor : descriptorList) {
            if (descriptor.getId().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        List<SourceDescriptor> sources = this.component.getSources();
        for (SourceDescriptor source : sources) {
            if (source.getName().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        List<TargetDescriptor> targets = this.component.getTargets();
        for (TargetDescriptor target : targets) {
            if (target.getName().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        if (!descriptorExists) {
            throw new ValidationException(String.format("The parameter ID [%s] does not exists in the component '%s'",
                                                        parameterId, component.getLabel()));
        }
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        Variable variable = this.inputParameterValues.stream()
                .filter(v -> v.getKey().equals(parameterId))
                .findFirst()
                .orElse(null);
        SourceDescriptor sourceDescriptor = this.component.getSources().stream()
                .filter(s -> s.getName().equals(parameterId))
                .findFirst()
                .orElse(null);
        final int cardinality = sourceDescriptor != null ? sourceDescriptor.getCardinality() : 1;
        if (variable != null) {
            if (cardinality == 1) {
                variable.setValue(value);
            } else {
                variable.setValue(appendValueToList(variable.getValue(), value));
            }
        } else {
            Variable var;
            if (cardinality == 1) {
                var = new Variable(parameterId, value);
            } else {
                String newValue = appendValueToList(null, value);
                var = new Variable(parameterId, newValue);
            }
            this.inputParameterValues.add(var);
        }
    }

    @Override
    public void setOutputParameterValue(String parameterId, String value) {
        if (this.outputParameterValues == null) {
            this.outputParameterValues = new ArrayList<>();
        }
        this.outputParameterValues.add(new Variable(parameterId, value));
    }

    @Override
    public String buildExecutionCommand() {
        return null;
    }
}
