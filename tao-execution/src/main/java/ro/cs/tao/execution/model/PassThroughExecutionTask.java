package ro.cs.tao.execution.model;

import ro.cs.tao.component.PassThroughComponent;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.eodata.naming.NamingRule;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

public class PassThroughExecutionTask extends ExecutionTask {

    private PassThroughComponent component;

    @XmlTransient
    private List<NamingRule> rules;

    public PassThroughExecutionTask() {
        super();
    }

    public PassThroughComponent getComponent() {
        return component;
    }

    public void setComponent(PassThroughComponent component) {
        this.component = component;
    }

    @Override
    public void setInputParameterValue(String parameterName, String value) {
        final boolean descriptorExists =
                this.component.getSources().stream().anyMatch(s -> s.getName().equals(parameterName)) ||
                this.component.getTargets().stream().anyMatch(t -> t.getName().equals(parameterName));
        if (!descriptorExists) {
            throw new ValidationException(String.format("The input [%s] does not exists in the component '%s'",
                                                        parameterName, component.getLabel()));
        }
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        Variable variable = this.inputParameterValues.stream()
                                                     .filter(v -> v.getKey().equals(parameterName))
                                                     .findFirst()
                                                     .orElse(null);
        SourceDescriptor sourceDescriptor = this.component.getSources().stream()
                                                          .filter(s -> s.getName().equals(parameterName))
                                                          .findFirst()
                                                          .orElse(null);
        final int cardinality = sourceDescriptor != null ? sourceDescriptor.getCardinality() : 1;
        if (variable != null) {
            variable.setValue(value);
        } else {
            this.inputParameterValues.add(new Variable(parameterName, value));
        }
    }

    @Override
    public void setOutputParameterValue(String parameterName, String value) {
        boolean descriptorExists = this.component.getTargets().stream().anyMatch(t -> t.getName().equals(parameterName));
        if (!descriptorExists) {
            throw new ValidationException(String.format("The output parameter ID [%s] does not exists in the component '%s'",
                                                        parameterName, component.getLabel()));
        }
        if (this.outputParameterValues == null) {
            this.outputParameterValues = new ArrayList<>();
        }
        for (int i = 0; i < this.outputParameterValues.size(); i++) {
            if (this.outputParameterValues.get(i).getKey().equals(parameterName)) {
                this.outputParameterValues.set(i, new Variable(parameterName, value));
                return;
            }
        }
        this.outputParameterValues.add(new Variable(parameterName, value));
    }

    @Override
    public String buildExecutionCommand() {
        return "output";
    }
}
