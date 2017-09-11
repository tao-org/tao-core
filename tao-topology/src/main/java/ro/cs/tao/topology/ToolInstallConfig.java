package ro.cs.tao.topology;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by cosmin on 8/7/2017.
 */
public class ToolInstallConfig {

    private int priority;
    private String name;
    private List<ToolInstallStep> installSteps = new ArrayList<>();
    private List<ToolInstallStep> uninstallSteps = new ArrayList<>();

    public ToolInstallConfig() {
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public List<ToolInstallStep> getInstallSteps() {
        return installSteps;
    }

    public List<ToolInstallStep> getUninstallSteps() {
        return uninstallSteps;
    }

    public void addToolInstallStep(ToolInstallStep installStep) {
        this.installSteps.add(installStep);
        // sort the steps according to their order id
        installSteps.sort(Comparator.comparingInt(ToolInstallStep::getOrderId));
    }

    public void addToolUninstallStep(ToolInstallStep installStep) {
        this.uninstallSteps.add(installStep);
        // sort the steps according to their order id
        uninstallSteps.sort(Comparator.comparingInt(ToolInstallStep::getOrderId));
    }

    public void setInstallSteps(List<ToolInstallStep> installSteps) {
        this.installSteps = installSteps;
        // sort the steps according to their order id
        installSteps.sort(Comparator.comparingInt(ToolInstallStep::getOrderId));
    }

    public void setUninstallSteps(List<ToolInstallStep> uninstallSteps) {
        this.uninstallSteps = uninstallSteps;
        // sort the steps according to their order id
        uninstallSteps.sort(Comparator.comparingInt(ToolInstallStep::getOrderId));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
