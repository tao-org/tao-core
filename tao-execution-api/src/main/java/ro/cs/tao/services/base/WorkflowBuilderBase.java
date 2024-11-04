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

package ro.cs.tao.services.base;

import ro.cs.tao.component.*;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceComponentGroup;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.services.interfaces.ComponentService;
import ro.cs.tao.services.interfaces.DataSourceComponentService;
import ro.cs.tao.services.interfaces.WorkflowBuilder;
import ro.cs.tao.services.interfaces.WorkflowService;
import ro.cs.tao.services.utils.WorkflowUtilities;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.ComponentType;
import ro.cs.tao.workflow.enums.Status;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class WorkflowBuilderBase implements WorkflowBuilder {
    protected static final PersistenceManager persistenceManager;
    protected static final ComponentService componentService;
    protected static final WorkflowService workflowService;
    protected static final DataSourceComponentService dataSourceComponentService;
    private static final float xOrigin = 300;
    private static final float yOrigin = 150;
    private static final float xStep = 300;
    private static final float yStep = 150;

    static {
        persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
        componentService = SpringContextBridge.services().getService(ComponentService.class);
        workflowService = SpringContextBridge.services().getService(WorkflowService.class);
        dataSourceComponentService = SpringContextBridge.services().getService(DataSourceComponentService.class);
    }

    protected DataSourceComponent newDataSourceComponent(String sensor, String dataSource) throws PersistenceException {
        // let's have a DataSourceComponent
        String componentId = sensor + "-" + dataSource;
        DataSourceComponent dataSourceComponent;
        dataSourceComponent = persistenceManager.dataSourceComponents().get(componentId);
        if (dataSourceComponent == null) {
            dataSourceComponent = new DataSourceComponent(sensor, dataSource);
            dataSourceComponent.setFetchMode(FetchMode.OVERWRITE);
            dataSourceComponent.setLabel(dataSourceComponent.getSensorName() + " from " + dataSourceComponent.getDataSourceName());
            dataSourceComponent.setVersion("1.0");
            dataSourceComponent.setDescription(dataSourceComponent.getId());
            dataSourceComponent.setAuthors("TAO Team");
            dataSourceComponent.setCopyright("(C) TAO Team");
            dataSourceComponent.setNodeAffinity(NodeAffinity.Any);
            persistenceManager.dataSourceComponents().save(dataSourceComponent);
        }
        return dataSourceComponent;
    }

    protected DataSourceComponent newDataSourceComponent(String sensor, List<String> productNames, Principal principal) throws PersistenceException {
        // let's have a DataSourceComponent
        return dataSourceComponentService.createForLocations(productNames, sensor, "Local Database", null, "Test DSC", principal);
    }

    @Override
    public WorkflowDescriptor createWorkflowDescriptor() throws PersistenceException {
        WorkflowDescriptor descriptor;
        descriptor = persistenceManager.workflows().getByName(getName());
        if (descriptor == null) {
            descriptor = new WorkflowDescriptor();
            descriptor.setName(getName());
            descriptor.setStatus(Status.DRAFT);
            descriptor.setCreated(LocalDateTime.now());
            descriptor.setActive(true);
            descriptor.setUserId(SystemPrincipal.instance().getName());
            descriptor.setVisibility(Visibility.PRIVATE);
            descriptor = persistenceManager.workflows().save(descriptor);
            addNodes(descriptor);
        }/* else {
            Logger.getLogger(getClass().getName()).warning(String.format("Workflow '%s' already exists", getName()));
        }*/
        return descriptor;
    }

    protected abstract void addNodes(WorkflowDescriptor workflow) throws PersistenceException;

    protected WorkflowNodeDescriptor addNode(WorkflowDescriptor parent, String name, String componentId,
                                             ComponentType componentType, Map<String, String> customValues,
                                             WorkflowNodeDescriptor parentNode, ComponentType parentComponentType,
                                             Direction... relativeDirection) throws PersistenceException {
        WorkflowNodeDescriptor node = new WorkflowNodeDescriptor();
        node.setWorkflow(parent);
        node.setName(name);
        float[] coords = placeNode(parentNode, relativeDirection);
        node.setxCoord(coords[0]);
        node.setyCoord(coords[1]);
        node.setComponentId(componentId);
        node.setComponentType(componentType);
        if (customValues != null) {
            for (Map.Entry<String, String> entry : customValues.entrySet()) {
                node.addCustomValue(entry.getKey(), entry.getValue());
            }
        }
        node.setPreserveOutput(true);
        node.setCreated(LocalDateTime.now());
        node.setLevel(parentNode != null ? parentNode.getLevel() + 1 : 0);
        node = workflowService.addNode(parent.getId(), node);
        if (parentNode != null) {
            TaoComponent component1 = WorkflowUtilities.findComponent(parentNode);
            TaoComponent component2 = WorkflowUtilities.findComponent(node);
            workflowService.addLink(parentNode.getId(), component1.getTargets().get(0).getId(),
                                    node.getId(), component2.getSources().get(0).getId());
        }
        return node;
    }

    protected WorkflowNodeDescriptor addUtilityNode(WorkflowDescriptor parent,
                                                    String nodeType,
                                                    String containerId,
                                                    Map<String, String> customValues,
                                                    WorkflowNodeDescriptor parentNode,
                                                    Direction... relativeDirection) throws PersistenceException {
        final UtilityNode type = UtilityNode.valueOf(nodeType);
        final ProcessingComponentProvider componentProvider = persistenceManager.processingComponents();
        final ProcessingComponent component = componentProvider.getByLabel(type.name(), containerId);
        if (component == null) {
            throw new PersistenceException("Cannot add utility node '%s' because the container '%s' doesn't define it",
                                           nodeType, containerId);
        }
        WorkflowNodeDescriptor node = new WorkflowNodeDescriptor();
        node.setWorkflow(parent);
        node.setName(nodeType);
        final float[] coords = placeNode(parentNode, relativeDirection);
        node.setxCoord(coords[0]);
        node.setyCoord(coords[1]);
        node.setComponentId(component.getId());
        node.setComponentType(ComponentType.PROCESSING);
        if (customValues != null) {
            for (Map.Entry<String, String> entry : customValues.entrySet()) {
                node.addCustomValue(entry.getKey(), entry.getValue());
            }
        }
        node.setPreserveOutput(true);
        node.setCreated(LocalDateTime.now());
        node.setLevel(parentNode != null ? parentNode.getLevel() + 1 : 0);
        node = workflowService.addNode(parent.getId(), node);
        if (parentNode != null) {
            TaoComponent component1 = WorkflowUtilities.findComponent(parentNode);
            workflowService.addLink(parentNode.getId(), component1.getTargets().get(0).getId(),
                    node.getId(), component.getSources().get(0).getId());
        }
        return node;
    }

    protected WorkflowNodeDescriptor addNode(WorkflowDescriptor parent, String name, String componentId,
                                             ComponentType componentType, Map<String, String> customValues,
                                             WorkflowNodeDescriptor parentNode, ComponentType parentComponentType,
                                             int parentIndex,
                                             Direction... relativeDirection) throws PersistenceException {
        WorkflowNodeDescriptor node = new WorkflowNodeDescriptor();
        node.setWorkflow(parent);
        node.setName(name);
        float[] coords = placeNode(parentNode, relativeDirection);
        node.setxCoord(coords[0]);
        node.setyCoord(coords[1]);
        node.setComponentId(componentId);
        node.setComponentType(componentType);
        if (customValues != null) {
            for (Map.Entry<String, String> entry : customValues.entrySet()) {
                node.addCustomValue(entry.getKey(), entry.getValue());
            }
        }
        node.setPreserveOutput(true);
        node.setCreated(LocalDateTime.now());
        node.setLevel(parentNode != null ? parentNode.getLevel() + 1 : 0);
        node = workflowService.addNode(parent.getId(), node);
        if (parentNode != null) {
            TaoComponent component1 = WorkflowUtilities.findComponent(parentNode);
            if (!(component1 instanceof DataSourceComponentGroup)) {
                throw new PersistenceException("Method is intended for data source groups as parent");
            }
            DataSourceComponentGroup group = (DataSourceComponentGroup) component1;
            List<TargetDescriptor> targets = group.getTargets();
            if (targets.size() <= parentIndex) {
                throw new PersistenceException(String.format("Invalid index %d (expected at most %d)",
                                                             parentIndex, targets.size() - 1));
            }
            //DataSourceComponent parentComponent = dataSourceComponents.get(parentIndex);
            TaoComponent component2 = WorkflowUtilities.findComponent(node);
            workflowService.addLink(parentNode.getId(), targets.get(parentIndex).getId(),
                                    node.getId(), component2.getSources().get(0).getId());
        }
        return node;
    }

    protected WorkflowNodeDescriptor addGroupNode(WorkflowDescriptor parent, String name,
                                                  WorkflowNodeDescriptor parentNode,
                                                  WorkflowNodeDescriptor... nodes) throws PersistenceException {
        WorkflowNodeGroupDescriptor grpNode = new WorkflowNodeGroupDescriptor();
        grpNode.setWorkflow(parent);
        grpNode.setName(name);
        float[] coords = placeNode(parentNode, Direction.RIGHT);
        grpNode.setxCoord(coords[0]);
        grpNode.setyCoord(coords[1]);
        grpNode.setCreated(LocalDateTime.now());
        grpNode.setPreserveOutput(true);
        grpNode.setLevel(parentNode != null ? parentNode.getLevel() + 1 : 0);
        return workflowService.group(parent.getId(), grpNode, Arrays.asList(nodes));
    }

    protected void addLink(WorkflowNodeDescriptor parent, WorkflowNodeDescriptor child) throws PersistenceException {
        addLink(parent, null, child, null);
    }

    protected void addLink(WorkflowNodeDescriptor parent,
                           String fromParentPort,
                           WorkflowNodeDescriptor child,
                           String toChildPort) throws PersistenceException {
        if (parent != null && child != null) {
            TaoComponent component1 = WorkflowUtilities.findComponent(parent);
            TaoComponent component2 = WorkflowUtilities.findComponent(child);
            final TargetDescriptor linkSource;
            final SourceDescriptor linkTarget;
            if (fromParentPort == null) {
                linkSource = component1.getTargets().get(0);
            } else {
                linkSource = component1.getTargets().stream().filter(t -> t.getName().equals(fromParentPort))
                                       .findFirst().orElse(component1.findDescriptor(fromParentPort));
                if (linkSource == null) {
                    throw new PersistenceException(String.format("No such target descriptor [%s] for component [%s]",
                                                                 fromParentPort, parent.getId()));
                }
            }
            if (toChildPort == null) {
                linkTarget = component2.getSources().get(0);
            } else {
                linkTarget = component2.getSources().stream().filter(s -> s.getName().equals(toChildPort))
                                       .findFirst().orElse(component2.findDescriptor(toChildPort));
                if (linkTarget == null) {
                    throw new PersistenceException(String.format("No such source descriptor [%s] for component [%s]",
                                                                 toChildPort, parent.getId()));
                }
            }
            workflowService.addLink(parent.getId(), linkSource.getId(),
                                    child.getId(), linkTarget.getId());
        }
    }

    protected float[] placeNode(WorkflowNodeDescriptor relativeTo, Direction... directions) {
        float x,y;
        if (relativeTo == null) {
            x = xOrigin;
            y = yOrigin;
        } else {
            x = relativeTo.getxCoord();
            y = relativeTo.getyCoord();
        }
        if (directions != null) {
            for (Direction direction : directions) {
                if (direction != null) {
                    switch (direction) {
                        case TOP:
                            y -= yStep;
                            break;
                        case BOTTOM:
                            y += yStep;
                            break;
                        case LEFT:
                            x -= xStep;
                            break;
                        case RIGHT:
                            x += xStep;
                            break;
                        case TOP_LEFT:
                            x -= xStep;
                            y -= yStep;
                            break;
                        case TOP_RIGHT:
                            x += xStep;
                            y -= yStep;
                            break;
                        case BOTTOM_LEFT:
                            x -= xStep;
                            y += yStep;
                            break;
                        case BOTTOM_RIGHT:
                            x += xStep;
                            y += yStep;
                            break;
                    }
                }
            }
        }
        return new float[] { x, y };
    }

    public enum Direction {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private enum UtilityNode {
        Copy,
        Move,
        Delete
    }
}
