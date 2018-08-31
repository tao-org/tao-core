/*
 * Copyright (C) 2018 CS ROMANIA
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

package ro.cs.tao.component;

import ro.cs.tao.component.enums.ProcessingComponentVisibility;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.UUID;

/**
 * A group component is a special type of TAO component that encapsulates (groups) several other TAO components.
 * A group component inherits the sources of the first child component and has as targets the targets of the last
 * child component.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "groupComponent")
public class GroupComponent extends TaoComponent {
    private int parallelism;
    private ProcessingComponentVisibility visibility;
    private boolean active;

    public static GroupComponent create(List<SourceDescriptor> sources, List<TargetDescriptor> targets) {

        GroupComponent component = new GroupComponent();
        component.setId(UUID.randomUUID().toString());
        component.setLabel("");
        component.setDescription("");
        component.setVersion("1.0");
        component.setAuthors("TAO Team");
        component.setCopyright("(C)TAO Team");
        component.setNodeAffinity("Any");
        component.setVisibility(ProcessingComponentVisibility.SYSTEM);
        if (sources != null) {
            for (SourceDescriptor source : sources) {
                SourceDescriptor sourceDescriptor = new SourceDescriptor();
                sourceDescriptor.setId(UUID.randomUUID().toString());
                sourceDescriptor.setName(source.getName());
                List<String> constraints = source.getConstraints();
                if (constraints != null) {
                    for (String constraint : constraints) {
                        sourceDescriptor.addConstraint(constraint);
                    }
                }
                DataDescriptor srcData = source.getDataDescriptor();
                DataDescriptor data = new DataDescriptor();
                data.setFormatType(srcData.getFormatType());
                data.setLocation(srcData.getLocation());
                data.setDimension(srcData.getDimension());
                data.setGeometry(srcData.getGeometry());
                data.setSensorType(srcData.getSensorType());
                data.setCrs(srcData.getCrs());
                sourceDescriptor.setDataDescriptor(data);
                sourceDescriptor.setCardinality(source.getCardinality());
                component.addSource(sourceDescriptor);
            }
        }
        if (targets != null) {
            for (TargetDescriptor target: targets) {
                TargetDescriptor targetDescriptor = new TargetDescriptor();
                targetDescriptor.setId(UUID.randomUUID().toString());
                targetDescriptor.setName(target.getName());
                List<String> constraints = target.getConstraints();
                if (constraints != null) {
                    for (String constraint : constraints) {
                        targetDescriptor.addConstraint(constraint);
                    }
                }
                DataDescriptor srcData = target.getDataDescriptor();
                DataDescriptor data = new DataDescriptor();
                data.setFormatType(srcData.getFormatType());
                data.setLocation(srcData.getLocation());
                data.setDimension(srcData.getDimension());
                data.setGeometry(srcData.getGeometry());
                data.setSensorType(srcData.getSensorType());
                data.setCrs(srcData.getCrs());
                targetDescriptor.setDataDescriptor(data);
                targetDescriptor.setCardinality(target.getCardinality());
                component.addTarget(targetDescriptor);
            }
        }
        component.setActive(true);
        return component;
    }

    public GroupComponent() {
        super();
    }

    public int getParallelism() { return parallelism; }

    public void setParallelism(int parallelism) { this.parallelism = parallelism; }

    public ProcessingComponentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ProcessingComponentVisibility visibility) {
        this.visibility = visibility;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String defaultId() { return "NewGroup"; }

    @Override
    public GroupComponent clone() throws CloneNotSupportedException {
        GroupComponent newDescriptor = (GroupComponent) super.clone();
        newDescriptor.parallelism = this.parallelism;
        newDescriptor.active = this.active;
        newDescriptor.visibility = this.visibility;
        return newDescriptor;
    }
}
