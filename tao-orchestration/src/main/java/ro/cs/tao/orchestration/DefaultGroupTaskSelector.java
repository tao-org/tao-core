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

package ro.cs.tao.orchestration;

import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.serialization.BaseSerializer;
import ro.cs.tao.serialization.MapAdapter;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializerFactory;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import javax.xml.transform.stream.StreamSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultGroupTaskSelector implements TaskSelector<ExecutionGroup> {

    private Function<Long, WorkflowNodeDescriptor> workflowProvider;
    private BiFunction<Long, Long, ExecutionTask> taskByNodeProvider;
    private BiFunction<Long, String, List<WorkflowNodeDescriptor>> nodesByComponentProvider;
    private final Logger logger = Logger.getLogger(DefaultJobTaskSelector.class.getName());

    public DefaultGroupTaskSelector() { }

    @Override
    public void setWorkflowProvider(Function<Long, WorkflowNodeDescriptor> workflowProvider) {
        this.workflowProvider = workflowProvider;
    }

    @Override
    public void setTaskByNodeProvider(BiFunction<Long, Long, ExecutionTask> taskByNodeProvider) {
        this.taskByNodeProvider = taskByNodeProvider;
    }

    @Override
    public void setNodesByComponentProvider(BiFunction<Long, String, List<WorkflowNodeDescriptor>> nodesByComponentProvider) {
        this.nodesByComponentProvider = nodesByComponentProvider;
    }

    @Override
    public Class<ExecutionGroup> getTaskContainerClass() {
        return ExecutionGroup.class;
    }

    @Override
    public List<ExecutionTask> chooseNext(ExecutionGroup taskHolder, ExecutionTask currentTask) {
        List<ExecutionTask> next = null;
        List<ExecutionTask> tasks = taskHolder.getTasks();
        if (tasks != null && tasks.size() > 0) {
            next = new ArrayList<>();
            switch (taskHolder.getExecutionStatus()) {
                // If the group is not started, we return the first task in line
                case UNDETERMINED:
                    ExecutionTask task = tasks.get(0);
                    try {
                        BaseSerializer<LoopState> serializer = SerializerFactory.create(LoopState.class, MediaType.JSON);
                        LoopState current = serializer.deserialize(new StreamSource(taskHolder.getInternalState()));
                        MapAdapter mapAdapter = new MapAdapter();
                        Map<String, String> map = mapAdapter.marshal(taskHolder.getInputParameterValues().get(0).getValue());
                        Variable nextInput = null;
                        if (map != null) {
                            nextInput = map.entrySet().stream().map(e -> new Variable(e.getKey(), e.getValue())).collect(Collectors.toList()).get(current.getCurrent());
                            task.setInputParameterValue(nextInput.getKey(), nextInput.getValue());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    next.add(task);
                    break;
                // If the group is already queued for execution,
                // there should be already at least one task queued for execution, so we don't return another one
                case QUEUED_ACTIVE:
                    break;
                case SUSPENDED:
                    next.add(tasks.stream()
                                  .filter(t -> t.getExecutionStatus() == ExecutionStatus.SUSPENDED)
                                  .findFirst().orElse(null));
                    break;
                case RUNNING:
                    List<ExecutionTask> candidates = getCandidatesForExecution(taskHolder, currentTask);
                    if (candidates != null) {
                        next.addAll(candidates);
                    }
                    break;
                case DONE:
                case FAILED:
                case CANCELLED:
                default:
                    break;
            }
        }
        return next;
    }

    private List<ExecutionTask> getCandidatesForExecution(ExecutionGroup group, ExecutionTask task) {
        if (task == null || task.getExecutionStatus() != ExecutionStatus.DONE) {
            return null;
        }
        WorkflowNodeDescriptor workflowNode = this.workflowProvider.apply(task.getWorkflowNodeId());
        if (workflowNode == null) {
            logger.severe(String.format("No workflow node with id %s was found in the database", task.getWorkflowNodeId()));
            return null;
        }
        WorkflowDescriptor workflow = workflowNode.getWorkflow();
        List<WorkflowNodeDescriptor> childNodes = workflow.findChildren(workflow.getNodes(), workflowNode);
        if (childNodes == null || childNodes.size() == 0) {
            return null;
        }
        return childNodes.stream().filter(n ->
                n.getIncomingLinks().stream().allMatch(link -> {
                    List<WorkflowNodeDescriptor> parents =
                            this.nodesByComponentProvider.apply(workflow.getId(),link.getInput().getParentId());
                    return parents.stream().allMatch(p -> {
                        ExecutionTask taskByNode = this.taskByNodeProvider.apply(group.getJob().getId(), p.getId());
                        return taskByNode != null && taskByNode.getExecutionStatus() == ExecutionStatus.DONE;
                    });
                }))
                .map(n -> {
                    ExecutionTask next = this.taskByNodeProvider.apply(group.getJob().getId(), n.getId());
                    List<Variable> outputs = task.getOutputParameterValues();
                    n.getIncomingLinks().forEach(link -> {
                        for (Variable variable : outputs) {
                            if (variable.getKey().equals(link.getInput().getName())) {
                                next.setInputParameterValue(link.getOutput().getName(), variable.getValue());
                            }
                        }
                    });
                    return next;
                })
                .collect(Collectors.toList());
    }
}
