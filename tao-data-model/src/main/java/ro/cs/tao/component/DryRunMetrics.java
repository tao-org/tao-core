package ro.cs.tao.component;

import ro.cs.tao.topology.NodeFlavor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DryRunMetrics extends LongIdentifiable {

    private String componentName;
    private List<String> cpuUsageMetrics;
    private List<String> memoryUsageMetrics;
    private int duration;
    private NodeFlavor executionNodeFlavor;

    public DryRunMetrics() {

    }

    public List<String> getCpuUsageMetrics() {
        return cpuUsageMetrics;
    }

    public void addCpuUsageMetric(String cpuUsageMetric) {
        if(this.cpuUsageMetrics == null){
            this.cpuUsageMetrics = new ArrayList<>();
        }
        this.cpuUsageMetrics.add(cpuUsageMetric);
    }

    public List<String> getMemoryUsageMetrics() {
        return memoryUsageMetrics;
    }

    public void addMemoryUsageMetric(String memoryUsageMetric) {
        if(this.memoryUsageMetrics == null){
            this.memoryUsageMetrics = new ArrayList<>();
        }
        this.memoryUsageMetrics.add(memoryUsageMetric);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public NodeFlavor getExecutionNodeFlavor() {
        return executionNodeFlavor;
    }

    private void calculateNodeFlavor() {
        //TODO
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DryRunMetrics that = (DryRunMetrics) o;
        return Objects.equals(componentName, that.componentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), componentName);
    }

    @Override
    public String toString() {
        return "DryRunMetrics{" +
                "componentName='" + componentName + '\'' +
                ", cpuUsageMetrics=" + cpuUsageMetrics +
                ", memoryUsageMetrics=" + memoryUsageMetrics +
                ", duration=" + duration +
                ", executionNodeFlavor=" + executionNodeFlavor +
                '}';
    }
}
