package ro.cs.tao.services.utils;

import org.springframework.stereotype.Component;
import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.workflow.GraphObject;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class for various operations on nodes and workflows.
 *
 * @author Cosmin Cara
 */
@Component
public class WorkflowUtilities {
    private static PersistenceManager persistenceManager;
    private static final Logger logger = Logger.getLogger(WorkflowUtilities.class.getName());

    /**
     * Setter for the persistence manager
     */
    public static void setPersistenceManager(PersistenceManager manager) {
        persistenceManager = manager;
    }

    /**
     * Returns a subset of nodes, from the given list, that have links from the given set of node identifiers.
     * @param parentNodes   The source of the links
     * @param nodes         The list of nodes to be filtered
     */
    public static List<WorkflowNodeDescriptor> findReferrers(Set<Long> parentNodes, List<WorkflowNodeDescriptor> nodes) {
        Set<WorkflowNodeDescriptor> referrers = new LinkedHashSet<>();
        if (parentNodes != null && nodes != null) {
            nodes.forEach(n -> {
                Set<ComponentLink> links = n.getIncomingLinks();
                if (links != null) {
                    links.forEach(l -> {
                        if (parentNodes.contains(l.getSourceNodeId())) {
                            referrers.add(n);
                        }
                    });
                }
            });
        }
        return new ArrayList<>(referrers);
    }

    /**
     * Returns a subset of nodes, from the given list, that have links only from nodes within the list
     * @param nodes The list of nodes to be filtered
     */
    public static List<WorkflowNodeDescriptor> findTerminals(List<WorkflowNodeDescriptor> nodes) {
        Set<WorkflowNodeDescriptor> terminals = new LinkedHashSet<>();
        if (nodes != null) {
            if (nodes.size() == 1) {
                return nodes;
            } else {
                nodes.forEach(n -> {
                    if (nodes.stream()
                            .noneMatch(n1 -> !n1.getId().equals(n.getId()) &&
                                             n1.getIncomingLinks().stream()
                                                .anyMatch(l -> n.getId().equals(l.getSourceNodeId())))) {
                        terminals.add(n);
                    }
                });
            }
        }
        return new ArrayList<>(terminals);
    }

    /**
     * Computes the rectangle that wraps around the given list of nodes.
     * @param nodes The list of nodes
     */
    public static Rectangle2D.Float computeEnvelope(List<WorkflowNodeDescriptor> nodes) {
        Rectangle2D.Float envelope = new Rectangle2D.Float();
        envelope.x = Float.MAX_VALUE;
        envelope.y = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        for (WorkflowNodeDescriptor node : nodes) {
            if (envelope.x > node.getxCoord()) {
                envelope.x = node.getxCoord() - 20;
            }
            if (envelope.y > node.getyCoord()) {
                envelope.y = node.getyCoord() - 20;
            }
            if (maxX < node.getxCoord() + 300) {
                maxX = node.getxCoord() + 300;
            }
            if (maxY < node.getyCoord() + 150) {
                maxY = node.getyCoord() + 150;
            }
        }
        envelope.width = maxX - envelope.x;
        envelope.height = maxY - envelope.y;
        return envelope;
    }

    /**
     * Creates and sets an unique name for a node
     * @param workflow      The parent workflow
     * @param nodeDescriptor    The node descriptor
     */
    public static void ensureUniqueName(WorkflowDescriptor workflow, WorkflowNodeDescriptor nodeDescriptor) {
        final String name = nodeDescriptor.getName();
        Set<String> nodeNames = workflow.getNodes().stream().filter(n -> !n.getId().equals(nodeDescriptor.getId()))
                .map(GraphObject::getName).collect(Collectors.toSet());
        if (nodeNames.contains(name)) {
            int count = 1;
            String newName;
            do {
                newName = String.format("%s (%s)", name, count++);
            } while (nodeNames.contains(newName));
            nodeDescriptor.setName(newName);
        }
    }
}
