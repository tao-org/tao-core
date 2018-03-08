/*
 * Copyright (C) 2017 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
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
    private String description;
    private String version;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
