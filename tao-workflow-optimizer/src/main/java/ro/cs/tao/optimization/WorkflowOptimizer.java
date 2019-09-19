package ro.cs.tao.optimization;

import ro.cs.tao.component.*;
import ro.cs.tao.execution.Optimizers;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.utils.WorkflowUtilities;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A WorkflowOptimizer is a helper class that tries to find a chain in workflow that can be executed
 * on the machine for bypassing the I/O.
 *
 * @author Alexandru Pirlea
 */
public class WorkflowOptimizer {

    /**
     * Creates an easier to work on graph.
     *
     * @param workflow workflow on which optimisation is based
     * @return graph to be optimised
     */
    private static OptimizationGraph createGraph(WorkflowDescriptor workflow) {
        Logger logger = Logger.getLogger(WorkflowOptimizer.class.getName());

        NodesDefinition defined = new NodesDefinition();

        OptimizationGraph graph = new OptimizationGraph();
        Map<Long, OptimizationNode> references = new HashMap<>();

        /* define an optimization node for every workflow node */
        for (WorkflowNodeDescriptor wNode : workflow.getNodes()) {

            /* create optimization node with the component from this node */
            OptimizationNode node = new OptimizationNode(wNode.getId(), defined);

            /* add node to graph */
            graph.addNode(node);

            /* add reference from node id to workflow node */
            references.put(wNode.getId(), node);

            /* link node id with it's workflow node */
            defined.addWorkflowNode(wNode, wNode.getId());

            try {
                /* get component from mode */
                TaoComponent comp = WorkflowUtilities.findComponent(wNode);

                if (comp instanceof ProcessingComponent) {
                    /* link componentId with the node id */
                    defined.addComponent((ProcessingComponent) comp, wNode.getId());

                    /* link RuntimeOptimizer with node id */
                    String containerId = ((ProcessingComponent) comp).getContainerId();
                    RuntimeOptimizer optimizer = Optimizers.findOptimizer(containerId);
                    defined.addOptimizer(optimizer, wNode.getId());

                } else {
                    /* component is not processing component */
                    defined.addComponent(null, wNode.getId());
                    defined.addOptimizer(null, wNode.getId());

                }
            } catch (PersistenceException e) {
                /* error: persistence exception */
                logger.finer("Persistence exception on component: " + wNode.getComponentId() + ".");
                return null;
            }
        }

        /* set dependencies (u -> v) */
        for (WorkflowNodeDescriptor wNode : workflow.getNodes()) {
            Long vId = wNode.getId();
            OptimizationNode v = references.get(vId);

            for (ComponentLink link: wNode.getIncomingLinks()) {
                Long uId = link.getSourceNodeId();

                OptimizationNode u = references.get(uId);

                if (u == null) {
                    /* no known optimization node has node with id uId */
                    return null;
                }

                /* add link between optimization nodes both ways */
                u.addChild(v);
                v.addParent(u);
            }
        }

        return graph;
    }

    /**
     * Cycles all edges in the graph until no modifications are made.
     * If edge is a critical edge and vertices have the same optimizer, vertices are merged.
     *
     * @param graph to be optimised
     */
    private static void optimizeGraph(OptimizationGraph graph) {
        boolean isModified;

        do {
            isModified = false;

            for (int i = 0; i < graph.size(); i++) {
                OptimizationNode u = graph.getNodes().get(i);

                OptimizationNode v = u.getSuccessors().stream().findFirst().orElse(null);

                /* nodes must be compatible for grouping */
                if (v != null && u.isCompatibleWith(v)) {
                    isModified = true;

                    /* merge nodes for aggregated component */
                    u.getNodeIds().addAll(v.getNodeIds());

                    /* change node's parents and children */
                    u.setSuccessors(v.getSuccessors());
                    v.getSuccessors().forEach((node) -> node.setPredecessors(v.getPredecessors()));

                    /* remove v from graph */
                    graph.removeNode(v);
                }
            }
        } while (isModified);
    }

    /**
     * A workflow is created based on graph.
     *
     * @param graph optimized graph
     * @param workflowName original workflow's name
     * @return optimized workflow
     */
    private static WorkflowDescriptor createWorkflow(OptimizationGraph graph, String workflowName) {
        Logger logger = Logger.getLogger(WorkflowOptimizer.class.getName());

        OptimizedWorkflowBuilder builder = new OptimizedWorkflowBuilder(workflowName);

        try {
            Map<OptimizationNode, WorkflowNodeDescriptor> translation = new HashMap<>();

            /* make empty workflow */
            WorkflowDescriptor workflow = builder.createWorkflowDescriptor();

            /* for every optimization node make a workflow node */
            for (OptimizationNode node : graph.getNodes()) {
                WorkflowNodeDescriptor wNode = builder.createOptimizedNode(node, workflow);

                translation.put(node, wNode);
            }

            workflow = builder.updateWorkflowDescriptor(workflow);

            /* add links based on graph */
            for (Map.Entry<OptimizationNode, WorkflowNodeDescriptor> e : translation.entrySet()) {
                List<OptimizationNode> parents = e.getKey().getPredecessors();
                WorkflowNodeDescriptor child = e.getValue();

                for (OptimizationNode node : parents) {
                    WorkflowNodeDescriptor parent = translation.get(node);

                    builder.addLink(parent, child);
                }
            }

            return builder.updateWorkflowDescriptor(workflow);

        } catch (AggregationException e1) {
            logger.finer("Aggregation exception: " + e1.getMessage());
            builder.cleanCache();
            return null;

        } catch (PersistenceException e2) {
            logger.finer("Persistence exception: " + e2.getMessage());
            builder.cleanCache();
            return null;

        }
    }

    /**
     * Sets of components are aggregated if possible.
     * If no such aggregation is possible, no workflow is created and return is null.
     *
     * @param workflow original workflow
     * @return optimized workflow
     */
    public static WorkflowDescriptor getOptimizedWorkflow(WorkflowDescriptor workflow) {
        Logger logger = Logger.getLogger(WorkflowOptimizer.class.getName());

        if (workflow == null) {
            logger.finer("Null workflow parameter.");
            return null;
        }

        if (workflow.getNodes().isEmpty()) {
            logger.finer("Empty workflow.");
            return null;
        }

        OptimizationGraph graph = createGraph(workflow);

        if (graph == null) {
            /* error while creating optimization graph */
            return null;
        }

        int sizeBefore = graph.size();

        /* for every optimization node with more than one component there is an optimization chain */
        optimizeGraph(graph);

        int sizeAfter = graph.size();

        if (sizeAfter == sizeBefore) {
            logger.finer("No need for optimisation.");
            return null;
        }

        return createWorkflow(graph, workflow.getName());
    }
}
