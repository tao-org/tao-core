package ro.cs.tao.optimization;

import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.RuntimeOptimizer;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.services.base.WorkflowBuilderBase;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.ComponentType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * A OptimizedWorkflowBuilder is a helper class for creating a workflow from an optimization graph.
 *
 * @author Alexandru Pirlea
 */
public class OptimizedWorkflowBuilder extends WorkflowBuilderBase {
    private List<Object> cache = new ArrayList<>();
    private String name;
    private Logger logger = Logger.getLogger(getClass().getName());

    public OptimizedWorkflowBuilder(String name) {
        this.name = "Optimized " + name;
    }

    @Override
    protected void addNodes(WorkflowDescriptor workflow) throws PersistenceException {
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

    protected WorkflowDescriptor updateWorkflowDescriptor(WorkflowDescriptor wf) throws PersistenceException {
        return persistenceManager.getWorkflowDescriptor(wf.getId());
    }

    /**
     * Creates and adds a workflow node to workflow.
     *
     * @param node contains component/s for new workflow node
     * @param workflow where to add the node
     * @return created node
     * @throws PersistenceException
     */
    protected WorkflowNodeDescriptor createOptimizedNode(OptimizationNode node, WorkflowDescriptor workflow) throws PersistenceException {
        WorkflowNodeDescriptor optimizedNode = null;

        if (node.getNodeIds().size() == 0) {
            /* error: an optimization node cannot exit without a component */

        } else if (node.getNodeIds().size() == 1) {
            /* one node in list */
            optimizedNode = getNodeClone(node, workflow);

        } else {
            /* aggregate list */
            List<ProcessingComponent> components = new ArrayList<>();

            logger.finer("Creating aggregated components from nodes: " + node.getNodeIds());

            node.getNodeIds().forEach((id) ->
                                 components.add(node.getNodesDefinition().getComponent(id)));

            RuntimeOptimizer optimizer = node.getNodesDefinition().getOptimizer(node.getFirstNodeId());

            ProcessingComponent result = optimizer.aggregate(components.toArray(new ProcessingComponent[0]));

            result = persistenceManager.saveProcessingComponent(result);

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
     * @throws PersistenceException
     */
    private WorkflowNodeDescriptor getNodeClone(OptimizationNode node, WorkflowDescriptor workflow) throws PersistenceException {
        WorkflowNodeDescriptor newNode = new WorkflowNodeDescriptor();
        WorkflowNodeDescriptor oldNode = node.getNodesDefinition().getWorkflowNode(node.getFirstNodeId());

        newNode.setWorkflow(workflow);
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
            DataSourceComponent dsComponent = persistenceManager.getDataSourceInstance(oldNode.getComponentId());

            Query oldQuery = persistenceManager.getQuery(SessionStore.currentContext().getPrincipal().getName(),
                    dsComponent.getSensorName(),
                    dsComponent.getDataSourceName(),
                    oldNode.getId());

            Query newQuery = new Query();

            newQuery.setLabel(oldQuery.getLabel() + UUID.randomUUID().toString());

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

            newQuery = persistenceManager.saveQuery(newQuery);

            cache.add(newQuery);
        }

        return persistenceManager.updateWorkflowNodeDescriptor(newNode);
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
     * @throws PersistenceException
     */
    private WorkflowNodeDescriptor createNodeWithComponent(ProcessingComponent comp, OptimizationNode node, WorkflowDescriptor workflow) throws PersistenceException {
        NodesDefinition defined = node.getNodesDefinition();

        WorkflowNodeDescriptor newNode = new WorkflowNodeDescriptor();
        WorkflowNodeDescriptor oldNode = defined.getWorkflowNode(node.getFirstNodeId());

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
                    persistenceManager.deleteProcessingComponent(((ProcessingComponent) obj).getId());

                } else if (obj instanceof WorkflowNodeDescriptor) {
                    persistenceManager.delete((WorkflowNodeDescriptor) obj);

                } else if (obj instanceof WorkflowDescriptor) {
                    persistenceManager.deleteWorkflowDescriptor(((WorkflowDescriptor) obj).getId());

                } else if (obj instanceof Query) {
                    persistenceManager.removeQuery(((Query) obj).getId());

                } else {
                    logger.fine("Unknown element in list.");

                }
            } catch (PersistenceException e) {
                logger.finer("Persistence exception while deleting object.");
            }
        }
    }
}
