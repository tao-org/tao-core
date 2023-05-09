package ro.cs.tao.optimization;

import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.RuntimeOptimizer;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.services.base.WorkflowBuilderBase;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.ComponentType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * A OptimizedWorkflowBuilder is a helper class for creating a workflow from an optimization graph.
 *
 * @author Alexandru Pirlea
 */
public class OptimizedWorkflowBuilder extends WorkflowBuilderBase {
    private final List<Object> cache = new ArrayList<>();
    private final String name;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public OptimizedWorkflowBuilder(String name) {
        this.name = "Optimized " + name;
    }

    @Override
    protected void addNodes(WorkflowDescriptor workflow) {
        /* do nothing */
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addLink(WorkflowNodeDescriptor parent, WorkflowNodeDescriptor child) throws PersistenceException {
        super.addLink(parent, child);
    }

    @Override
    public void addLink(WorkflowNodeDescriptor parent, String fromParentPort, WorkflowNodeDescriptor child, String toChildPort) throws PersistenceException {
        super.addLink(parent, fromParentPort, child, toChildPort);
    }

    protected WorkflowDescriptor updateWorkflowDescriptor(WorkflowDescriptor wf) {
        return persistenceManager.workflows().get(wf.getId());
    }

    /**
     * Creates and adds a workflow node to workflow.
     *
     * @param node contains component/s for new workflow node
     * @param workflow where to add the node
     * @return created node
     */
    protected WorkflowNodeDescriptor createOptimizedNode(OptimizationNode node, WorkflowDescriptor workflow,
                                                         Map<String, Map<String, String>> values) throws PersistenceException {
        WorkflowNodeDescriptor optimizedNode;

        if (node.getNodeIds().size() == 0) {
            /* error: an optimization node cannot exit without a component */
            throw new IllegalArgumentException("Empty nodes collection");
        } else if (node.getNodeIds().size() == 1) {
            /* one node in list */
            optimizedNode = getNodeClone(node, workflow);

        } else {
            /* aggregate list */
            List<ProcessingComponent> components = new ArrayList<>();

            logger.finer("Creating aggregated components from nodes: " + node.getNodeIds());
            final Map<String, Map<String, String>> componentValues = new HashMap<>();
            node.getNodeIds().forEach((id) -> {
                final ProcessingComponent component = node.getNodesDefinition().getComponent(id);
                components.add(component);
                if (values != null) {
                    final Map<String, String> map = values.get(node.getNodesDefinition().getWorkflowNode(id).getName());
                    if (map != null) {
                        componentValues.put(component.getId(), map);
                    }
                }
            });

            RuntimeOptimizer optimizer = node.getNodesDefinition().getOptimizer(node.getFirstNodeId());

            ProcessingComponent result = optimizer.aggregate(components, componentValues);

            result = persistenceManager.processingComponents().save(result);

            optimizedNode = createNodeWithComponent(result, node, workflow);
        }

        return optimizedNode;
    }

    /**
     * Creates and adds a workflow node to workflow.
     * Copies all information from older node to newer node.
     * If needed, a query is also created.
     *
     * @param node old node
     * @param workflow where to add the node
     * @return new node
     */
    private WorkflowNodeDescriptor getNodeClone(OptimizationNode node, WorkflowDescriptor workflow) throws PersistenceException {
        WorkflowNodeDescriptor newNode = new WorkflowNodeDescriptor();
        WorkflowNodeDescriptor oldNode = node.getNodesDefinition().getWorkflowNode(node.getFirstNodeId());

        newNode.setWorkflow(workflow);
        newNode.setCreatedFromNodeId(oldNode.getId());
        newNode.setName(oldNode.getName());
        newNode.setxCoord(oldNode.getxCoord());
        newNode.setyCoord(oldNode.getyCoord());
        newNode.setComponentId(oldNode.getComponentId());
        newNode.setComponentType(oldNode.getComponentType());
        newNode.setCustomValues(oldNode.getCustomValues());
        newNode.setPreserveOutput(oldNode.getPreserveOutput());
        newNode.setCreated(LocalDateTime.now());
        newNode.setLevel(node.getLevel());

        newNode = workflowService.addNode(workflow.getId(), newNode);

        if (newNode.getComponentType() == ComponentType.DATASOURCE) {
            DataSourceComponent dsComponent = persistenceManager.dataSourceComponents().get(oldNode.getComponentId());

            Query oldQuery = persistenceManager.queries().get(SessionStore.currentContext().getPrincipal().getName(),
                                                              dsComponent.getSensorName(),
                                                              dsComponent.getDataSourceName(),
                                                              oldNode.getId());
            if (oldQuery != null) {
                Query newQuery = new Query();

                newQuery.setLabel(oldQuery.getLabel() + UUID.randomUUID());

                newQuery.setUserId(oldQuery.getUserId());
                newQuery.setSensor(oldQuery.getSensor());
                newQuery.setDataSource(dsComponent.getDataSourceName());
                newQuery.setPageNumber(oldQuery.getPageNumber());
                newQuery.setPageSize(oldQuery.getPageSize());
                newQuery.setLimit(oldQuery.getLimit());
                newQuery.setValues(oldQuery.getValues());
                newQuery.setWorkflowNodeId(newNode.getId());

                newQuery.setUser(oldQuery.getUser());
                newQuery.setPassword(oldQuery.getPassword());

                newQuery = persistenceManager.queries().save(newQuery);

                cache.add(newQuery);
            }
        }

        return persistenceManager.workflowNodes().update(newNode);
    }

    private boolean isTarget(ParameterValue param, ProcessingComponent component) {
        boolean ret = false;

        for (TargetDescriptor target : component.getTargets()) {
            if (target.getName().equals(param.getParameterName())) {
                ret = true;
            }
        }

        return ret;
    }

    private boolean isParameter(ParameterValue param, ProcessingComponent component) {
        boolean ret = false;

        for (ParameterDescriptor descriptor : component.getParameterDescriptors()) {
            if (descriptor.getName().equals(param.getParameterName())) {
                ret = true;
                break;
            }
        }

        return ret;
    }

    /**
     * Creates and adds a workflow node to workflow.
     * Based on type, parameters from older nodes are modified/kept/removed
     *
     * @param comp aggregated component
     * @param node contains information of components before aggregation
     * @param workflow where to add the node
     * @return new node
     */
    private WorkflowNodeDescriptor createNodeWithComponent(ProcessingComponent comp, OptimizationNode node, WorkflowDescriptor workflow) throws PersistenceException {
        NodesDefinition defined = node.getNodesDefinition();

        WorkflowNodeDescriptor newNode = new WorkflowNodeDescriptor();
        WorkflowNodeDescriptor oldNode = defined.getWorkflowNode(node.getFirstNodeId());
        newNode.setCreatedFromNodeId(oldNode.getId());
        newNode.setName("Optimized component chain");

        newNode.setxCoord(oldNode.getxCoord());
        newNode.setyCoord(oldNode.getyCoord());

        newNode.setComponentId(comp.getId());
        newNode.setComponentType(ComponentType.PROCESSING);

        /* set customValues for every component
         * from parameter.name to componentId + "-" + parameter.name
         */
        for (Long nodeId : node.getNodeIds()) {
            WorkflowNodeDescriptor wNode = defined.getWorkflowNode(nodeId);
            ProcessingComponent nodeComp = defined.getComponent(nodeId);

            /* Keep targets only if they are for the aggregated component. */
            for (ParameterValue param : wNode.getCustomValues()) {
                if (isTarget(param, nodeComp)) {
                    /* Node is last one in chain. */
                    if (node.getNodeIds().indexOf(nodeId) == node.getNodeIds().size() - 1) {
                        newNode.addCustomValue(param.getParameterName(), param.getParameterValue());
                    }
                } else if (isParameter(param, nodeComp)) {
                    String key = wNode.getComponentId() + "-" + param.getParameterName();
                    String value = param.getParameterValue();

                    newNode.addCustomValue(key, value);
                } else {
                    newNode.addCustomValue(param.getParameterName(), param.getParameterValue());
                }
            }
        }

        newNode.setPreserveOutput(oldNode.getPreserveOutput());
        newNode.setCreated(LocalDateTime.now());
        newNode.setLevel(node.getLevel());

        newNode = workflowService.addNode(workflow.getId(), newNode);

        return newNode;
    }

    @Override
    public WorkflowDescriptor createWorkflowDescriptor() throws PersistenceException {
        WorkflowDescriptor wf = super.createWorkflowDescriptor();
        cache.add(wf);
        return wf;
    }

    /**
     * Delete elements from newest to oldest.
     */
    protected void cleanCache() {
        Collections.reverse(cache);
        for (Object obj : cache) {
            try {
                if (obj instanceof ProcessingComponent) {
                    persistenceManager.processingComponents().delete(((ProcessingComponent) obj).getId());

                } else if (obj instanceof WorkflowNodeDescriptor) {
                    persistenceManager.workflowNodes().delete((WorkflowNodeDescriptor) obj);

                } else if (obj instanceof WorkflowDescriptor) {
                    persistenceManager.workflows().delete(((WorkflowDescriptor) obj).getId());

                } else if (obj instanceof Query) {
                    persistenceManager.queries().delete(((Query) obj).getId());

                } else {
                    logger.fine("Unknown element in list.");

                }
            } catch (PersistenceException e) {
                logger.finer("Persistence exception while deleting object.");
            }
        }
    }
}
