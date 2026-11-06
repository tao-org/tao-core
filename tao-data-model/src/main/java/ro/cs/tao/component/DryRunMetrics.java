package ro.cs.tao.component;

import jakarta.xml.bind.annotation.XmlRootElement;
import ro.cs.tao.topology.NodeFlavor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class containing the dry run metrics of a processing component. Currently being provided by SIM-PIPE
 *
 * @author Stefan Efrem-Hagiu
 */
@XmlRootElement(name = "dryRunMetrics")
public class DryRunMetrics extends LongIdentifiable {

    private List<String> cpuUsageMetrics;
    private List<String> memoryUsageMetrics;
    private int duration;
    private NodeFlavor executionNodeFlavor;
    private ProcessingComponent component;

    public DryRunMetrics() {

    }

    public List<String> getCpuUsageMetrics() {
        return cpuUsageMetrics;
    }

    public void setCpuUsageMetrics(List<String> cpuUsageMetrics) {
        this.cpuUsageMetrics = cpuUsageMetrics;
    }

    public void setMemoryUsageMetrics(List<String> memoryUsageMetrics) {
        this.memoryUsageMetrics = memoryUsageMetrics;
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

    public ProcessingComponent getComponent() {
        return component;
    }

    public void setComponent(ProcessingComponent component) {
        this.component = component;
    }

    public NodeFlavor getExecutionNodeFlavor() {
        return executionNodeFlavor;
    }

    private void calculateNodeFlavor() {
        //TODO
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DryRunMetrics that)) return false;
        if (!super.equals(o)) return false;
        return getDuration() == that.getDuration() && Objects.equals(getComponent(), that.getComponent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDuration(), getComponent());
    }

    @Override
    public String toString() {
        return "DryRunMetrics{" +
                "cpuUsageMetrics=" + cpuUsageMetrics +
                ", memoryUsageMetrics=" + memoryUsageMetrics +
                ", duration=" + duration +
                ", executionNodeFlavor=" + executionNodeFlavor +
                ", component=" + component +
                '}';
    }
}

