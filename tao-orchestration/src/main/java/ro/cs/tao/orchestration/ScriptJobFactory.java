package ro.cs.tao.orchestration;

import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.docker.Container;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ScriptTask;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ScriptJobFactory extends DefaultJobFactory {

    @Override
    protected ExecutionTask createProcessingExecutionTask(WorkflowNodeDescriptor workflowNode, Set<ComponentLink> incomingLinks, Map<String, String> inputs) {
        final List<ParameterValue> customValues = workflowNode.getCustomValues();
        final ProcessingComponent component = processingComponentProvider.get(workflowNode.getComponentId());
        final ScriptTask task = new ScriptTask();
        task.setComponent(component);
        // Placeholders for inputs of previous tasks
        if (incomingLinks != null) {
            incomingLinks.forEach(link -> {
                String name = link.getOutput().getName();
                task.setInputParameterValue(name, null);
            });
        }
        // Placeholders for outputs of this task
        final List<TargetDescriptor> targets = component.getTargets();
        if (targets != null) {
            targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
        }
        final Container container = TaskUtilities.getComponentContainer(workflowNode);
        if (container != null && container.getFormatNameParameter() != null) {
            task.setExternalCommonParameters(new HashSet<String>() {{ add(container.getFormatNameParameter()); }});
        }
        ParameterValue affinityParam = null;
        if (customValues != null) {
            for (ParameterValue param : customValues) {
                if (!"nodeAffinity".equals(param.getParameterName())) {
                    task.setInputParameterValue(param.getParameterName(), param.getParameterValue());
                } else {
                    affinityParam = param;
                }
            }
        }
        final String nodeAffinity = affinityParam != null
                ? affinityParam.getParameterValue()
                : component.getNodeAffinity();
        if (nodeAffinity != null && !"Any".equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity);
        }
        if (component.getComponentType() == ProcessingComponentType.EXTERNAL && customValues != null) {
            task.setCommand(component.buildExecutionCommand(customValues.stream().collect(Collectors.toMap(ParameterValue::getParameterName, ParameterValue::getParameterValue)),
                                                            null));
        }
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                task.setInputParameterValue(entry.getKey(), entry.getValue());
            }
        }
        return task;
    }
}
