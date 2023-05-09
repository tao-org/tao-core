package ro.cs.tao.services.utils;

import ro.cs.tao.component.*;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.workflow.GraphObject;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for various operations on nodes and workflows.
 *
 * @author Cosmin Cara
 */
//@Component
public class WorkflowUtilities {
    private static final PersistenceManager persistenceManager;

    static {
        persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
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
                                             n1.getIncomingLinks() != null &&
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
     * Returns a subset of nodes, from the given list, that have incoming links only from nodes outside the list, or at all;
     * @param nodes The list of nodes to be filtered
     */
    public static List<WorkflowNodeDescriptor> findFirstLevel(List<WorkflowNodeDescriptor> nodes) {
        Set<WorkflowNodeDescriptor> firstLevelNodes = new LinkedHashSet<>();
        if (nodes != null) {
            if (nodes.size() == 1) {
                return nodes;
            } else {
                Set<Long> keys = nodes.stream().map(WorkflowNodeDescriptor::getId).collect(Collectors.toSet());
                nodes.forEach(n -> {
                    final Set<ComponentLink> links = n.getIncomingLinks();
                    if (links == null || links.stream().noneMatch(link -> keys.contains(link.getSourceNodeId()))) {
                        firstLevelNodes.add(n);
                    }
                });
            }
        }
        return new ArrayList<>(firstLevelNodes);
    }

    /**
     * Returns a set of nodes that have incoming links from the given list of nodes.
     * @param workflow  The workflow to which the nodes belong
     * @param nodes     The nodes that may be referred into incoming links of other nodes
     */
    public static List<WorkflowNodeDescriptor> findLinkedNodes(WorkflowDescriptor workflow, List<WorkflowNodeDescriptor> nodes) {
        Set<WorkflowNodeDescriptor> linkedNodes = new LinkedHashSet<>();
        if (nodes != null) {
            Map<Long, WorkflowNodeDescriptor> allNodes = workflow.getNodes().stream().collect(Collectors.toMap(WorkflowNodeDescriptor::getId, Function.identity()));
            final Map<Long, WorkflowNodeDescriptor> linkNodesMap = new HashMap<>();
            for (Map.Entry<Long, WorkflowNodeDescriptor> entry : allNodes.entrySet()) {
                final Set<ComponentLink> links = entry.getValue().getIncomingLinks();
                if (links != null) {
                    for (ComponentLink link : links) {
                        linkNodesMap.put(link.getSourceNodeId(), entry.getValue());
                    }
                }
            }
            for (WorkflowNodeDescriptor node : nodes) {
                final Long nodeId = node.getId();
                if (linkNodesMap.containsKey(nodeId)) {
                    linkedNodes.add(linkNodesMap.get(nodeId));
                }
            }
        }
        return new ArrayList<>(linkedNodes);
    }

    /**
     * Returns the TargetDescriptor entity of a workflow node by its id.
     * @param id                The descriptor identifier
     * @param nodeDescriptor    The workflow node
     */
    public static TargetDescriptor findTarget(String id, WorkflowNodeDescriptor nodeDescriptor) throws PersistenceException {
        final TaoComponent component = findComponent(nodeDescriptor);
        //return component != null ? component.findDescriptor(id) : null;
        return component != null ? component.getTargets().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null) : null;
    }
    /**
     * Returns the SourceDescriptor entity of a workflow node by its id.
     * @param id                The descriptor identifier
     * @param nodeDescriptor    The workflow node
     */
    public static SourceDescriptor findSource(String id, WorkflowNodeDescriptor nodeDescriptor) throws PersistenceException {
        TaoComponent component = findComponent(nodeDescriptor);
        //return component != null ? component.findDescriptor(id) : null;
        return component != null ? component.getSources().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null) : null;
    }

    public static WorkflowNodeDescriptor findGroupSourceOwner(SourceDescriptor groupSource, WorkflowNodeGroupDescriptor groupNode) throws PersistenceException {
        WorkflowNodeDescriptor owner = null;
        final List<WorkflowNodeDescriptor> nodes = groupNode.getNodes();
        final GroupComponent groupComponent = (GroupComponent) findComponent(groupNode);
        //final SourceDescriptor groupDescriptor = groupComponent.findDescriptor(groupSourceId);
        for (WorkflowNodeDescriptor node : nodes) {
            final TaoComponent component = findComponent(node);
            if (component.getSources().stream().anyMatch(s -> s.getId().equals(groupSource.getReferencedSourceDescriptorId()))) {
                owner = node;
                break;
            }
        }
        return owner;
    }

    public static WorkflowNodeDescriptor findGroupTargetOwner(TargetDescriptor groupTarget, WorkflowNodeGroupDescriptor groupNode) throws PersistenceException {
        WorkflowNodeDescriptor owner = null;
        final List<WorkflowNodeDescriptor> nodes = groupNode.getNodes();
        final GroupComponent groupComponent = (GroupComponent) findComponent(groupNode);
        //final SourceDescriptor groupDescriptor = groupComponent.findDescriptor(groupTargetId);
        for (WorkflowNodeDescriptor node : nodes) {
            final TaoComponent component = findComponent(node);
            if (component.getTargets().stream().anyMatch(t -> t.getId().equals(groupTarget.getReferencedTargetDescriptorId()))) {
                owner = node;
                break;
            }
        }
        return owner;
    }

    public static List<WorkflowNodeDescriptor> findAncestors(WorkflowNodeDescriptor node) {
        if (node == null) {
            return null;
        }
        final WorkflowDescriptor workflow = node.getWorkflow();
        if (workflow == null) {
            return null;
        }
        final List<WorkflowNodeDescriptor> nodes = workflow.getNodes();;
        final Set<ComponentLink> links = node.getIncomingLinks();
        if (links == null || links.isEmpty()) {
            return null;
        } else {
            final Set<Long> parents = links.stream().map(ComponentLink::getSourceNodeId).collect(Collectors.toSet());
            return nodes.stream().filter(n -> parents.contains(n.getId())).collect(Collectors.toList());
        }
    }

    public static List<WorkflowNodeDescriptor> findDescendants(WorkflowNodeDescriptor node) {
        if (node == null) {
            return null;
        }
        final WorkflowDescriptor workflow = node.getWorkflow();
        if (workflow == null) {
            return null;
        }
        final List<WorkflowNodeDescriptor> nodes = workflow.getNodes();;
        return nodes.stream().filter(n -> n.getIncomingLinks() != null &&
                                          n.getIncomingLinks().stream().anyMatch(l -> node.getId() == l.getSourceNodeId()))
                    .collect(Collectors.toList());
    }

    public static List<WorkflowNodeDescriptor> findSiblings(WorkflowNodeDescriptor node) {
        if (node == null) {
            return null;
        }
        final WorkflowDescriptor workflow = node.getWorkflow();
        if (workflow == null) {
            return null;
        }
        final List<WorkflowNodeDescriptor> nodes = workflow.getNodes();
        final List<WorkflowNodeDescriptor> siblings = new ArrayList<>();
        final Set<ComponentLink> links = node.getIncomingLinks();
        if (links == null || links.isEmpty()) {
            siblings.addAll(nodes.stream().filter(n -> {
                final Set<ComponentLink> linkSet = n.getIncomingLinks();
                return linkSet == null || linkSet.isEmpty();
            }).collect(Collectors.toList()));
        } else {
            final Set<Long> parents = links.stream().map(ComponentLink::getSourceNodeId).collect(Collectors.toSet());
            siblings.addAll(nodes.stream().filter(n -> {
                if (n.getId().equals(node.getId())) {
                    return false;
                }
                final Set<ComponentLink> linkSet = n.getIncomingLinks();
                return linkSet != null && linkSet.stream().anyMatch(l -> parents.contains(l.getSourceNodeId()));
            }).collect(Collectors.toList()));
        }
        return siblings;
    }

    public static void deleteWorkflowNodes(long workflowId) throws PersistenceException {
        WorkflowDescriptor workflow = persistenceManager.workflows().get(workflowId);
        if (workflow == null) {
            throw new PersistenceException("Non-existent workflow with id=%d", workflowId);
        }
        final List<WorkflowNodeDescriptor> nodes = workflow.getOrderedNodes();
        if (nodes != null) {
            for (WorkflowNodeDescriptor node : nodes) {
                final Set<ComponentLink> links = node.getIncomingLinks();
                if (links != null) {
                    links.clear();
                    persistenceManager.workflowNodes().update(node);
                }
            }
            for (WorkflowNodeDescriptor node : nodes) {
                persistenceManager.workflowNodes().delete(node);
            }
        }
        persistenceManager.workflows().delete(workflowId);
    }

    public static SourceDescriptor findSourceById(String id, WorkflowNodeDescriptor node) throws PersistenceException {
        TaoComponent component = findComponent(node);
        return component != null ? component.getSources().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null) : null;
    }

    public static TargetDescriptor findTargetById(String id, WorkflowNodeDescriptor node) throws PersistenceException {
        TaoComponent component = findComponent(node);
        return component != null ? component.getTargets().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null) : null;
    }

    public static TaoComponent findComponent(WorkflowNodeDescriptor nodeDescriptor) throws PersistenceException{
        if (nodeDescriptor == null) {
            return null;
        }
        final String id = nodeDescriptor.getComponentId();
        TaoComponent component = null;
        switch (nodeDescriptor.getComponentType()) {
            case DATASOURCE:
                component = persistenceManager.dataSourceComponents().get(id);
                break;
            case PROCESSING:
                component = persistenceManager.processingComponents().get(id);
                break;
            case GROUP:
                component = persistenceManager.groupComponents().get(id);
                break;
            case DATASOURCE_GROUP:
                component = persistenceManager.dataSourceGroups().get(id);
                break;
        }
        if (component == null) {
            throw new PersistenceException(String.format("No such component [%s]", id));
        }
        return component;
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
