package ro.cs.tao.execution.model;

import ro.cs.tao.component.Variable;
import ro.cs.tao.component.WPSComponent;

import java.util.ArrayList;

public class WPSExecutionTask extends ExecutionTask {

    private WPSComponent component;

    public WPSExecutionTask() { super(); }

    public WPSComponent getComponent() { return component; }

    public void setComponent(WPSComponent component) { this.component = component; }

    @Override
    public void setInputParameterValue(String parameterId, String value) {

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
