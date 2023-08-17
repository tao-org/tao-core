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
package ro.cs.tao.workflow;

import ro.cs.tao.component.ComponentLink;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Interface defining common (default) implementation for all graph nodes containers
 * (such as {@link WorkflowDescriptor} and {@link WorkflowNodeGroupDescriptor}.
 * It allows the emulation of multiple inheritance for the above classes.
 *
 * @author Cosmin Cara
 */
public interface NodeListOrderer {

    /**
     * Returns the root nodes of from a collection of nodes.
     * A node is root if it has no incoming links from other nodes.
     * @param nodes     The node list.
     */
    default List<WorkflowNodeDescriptor> findRoots(final List<WorkflowNodeDescriptor> nodes) {
        final List<WorkflowNodeDescriptor> roots;
        if (nodes != null) {
            final Set<Long> nodeInGroups = new HashSet<>();
            nodes.stream().filter(n -> n instanceof WorkflowNodeGroupDescriptor)
                          .forEach(n -> {
                              final List<WorkflowNodeDescriptor> children = ((WorkflowNodeGroupDescriptor) n).getNodes();
                              if (children != null) {
                                  nodeInGroups.addAll(children.stream().map(WorkflowNodeDescriptor::getId).collect(Collectors.toSet()));
                              }
                          });
            roots = nodes.stream()
                         .filter(n -> !nodeInGroups.contains(n.getId()) && (n.getIncomingLinks() == null || n.getIncomingLinks().isEmpty()))
                         .collect(Collectors.toList());
            if (roots.size() == 0) {
                throw new IllegalArgumentException("The collection must have at least one root node (without incoming links)");
            }
        } else {
            roots = null;
        }
        return roots;
    }

    /**
     * Returns the terminal nodes (nodes that are not sources for links) from a collection of nodes.
     * @param nodes The node collection
     */
    default List<WorkflowNodeDescriptor> findLeaves(final List<WorkflowNodeDescriptor> nodes) {
        final List<WorkflowNodeDescriptor> leaves;
        if (nodes != null) {
            final Set<Long> parents = new HashSet<>();
            nodes.stream().filter(n -> n.getIncomingLinks() != null)
                          .forEach(n -> n.getIncomingLinks().forEach(l -> parents.add(l.getSourceNodeId())));
            leaves = nodes.stream().filter(n -> !parents.contains(n.getId())).collect(Collectors.toList());
        } else {
            leaves = null;
        }
        return leaves;
    }

    /**
     * Returns an ordered list of nodes from an initial list of nodes, affecting also the input list.
     * The output nodes are obtaining by breadth-first traversal of the graph represented by the input nodes.
     * @param nodes         The nodes to be ordered.
     * @param parentNode    The parent node group descriptor (if the implementor is of this type) or <code>null</code> for WorkflowDescriptor
     */
    default List<WorkflowNodeDescriptor> orderNodes(final List<WorkflowNodeDescriptor> nodes, final WorkflowNodeGroupDescriptor parentNode) {
        if (nodes != null) {
            final List<WorkflowNodeDescriptor> newList = new ArrayList<>();
            final List<WorkflowNodeDescriptor> roots = findRoots(nodes);
            final Queue<WorkflowNodeDescriptor> queue = new ArrayDeque<>(nodes.size());
            for (WorkflowNodeDescriptor root : roots) {
                root.setLevel(parentNode != null ? parentNode.getLevel() + 1 : 1);
                newList.add(root);
            }
            for (WorkflowNodeDescriptor root : roots) {
                queue.add(root);
                while (!queue.isEmpty()) {
                    final List<WorkflowNodeDescriptor> children = findChildren(nodes, queue.remove());
                    if (children != null && !children.isEmpty()) {
                        children.forEach(n -> {
                            if (!newList.contains(n)) {
                                newList.add(n);
                                queue.add(n);
                            }
                        });
                    }
                }
            }
            nodes.clear();
            nodes.addAll(newList);
        }
        return nodes;
    }

    /**
     * Returns the children of the given node, among the collection of nodes given as input.
     * If the node has no children, an empty list is returned.
     *
     * @param masterList    The collection of nodes to be searched.
     * @param node          The node whose children must be returned.
     */
    default List<WorkflowNodeDescriptor> findChildren(final List<WorkflowNodeDescriptor> masterList,
                                                      final WorkflowNodeDescriptor node) {
        if (masterList == null || masterList.isEmpty() || node == null) {
            return null;
        }
        final List<WorkflowNodeDescriptor> children = node instanceof WorkflowNodeGroupDescriptor ?
            ((WorkflowNodeGroupDescriptor) node).getNodes().stream().distinct().collect(Collectors.toList()) : new ArrayList<>();
        children.addAll(masterList.stream().filter(n -> {
            final Set<ComponentLink> links = n.getIncomingLinks();
            return links != null && links.stream().anyMatch(l -> node.getId().equals(l.getSourceNodeId()));
        }).distinct().peek(c -> c.setLevel(node.getLevel() + 1)).collect(Collectors.toList()));
        return children;
    }

    /**
     * Returns the parents of the given node, among the collection of nodes given as input.
     * If the node has no parents, an empty list is returned.
     *
     * @param masterList    The collection of nodes to be searched.
     * @param node          The node whose parents must be returned.
     */
    default List<WorkflowNodeDescriptor> findAncestors(final List<WorkflowNodeDescriptor> masterList,
                                                       final WorkflowNodeDescriptor node) {
        if (masterList == null || masterList.isEmpty() || node == null) {
            return null;
        }
        final List<WorkflowNodeDescriptor> ancestors = new ArrayList<>();
        final Queue<WorkflowNodeDescriptor> queue = new ArrayDeque<>();
        final Set<ComponentLink> componentLinks = node.getIncomingLinks();
        if (componentLinks != null && !componentLinks.isEmpty()) {
            for (ComponentLink link : componentLinks) {
                final WorkflowNodeDescriptor previous = masterList.stream().filter(n -> n.getId().equals(link.getSourceNodeId())).findFirst().orElse(null);
                if (previous != null) {
                    queue.add(previous);
                    if (previous.getIncomingLinks() == null || previous.getIncomingLinks().isEmpty()) {
                        ancestors.add(previous);
                    }
                    while (!queue.isEmpty()) {
                        ancestors.addAll(findAncestors(masterList, queue.remove()));
                    }
                }
            }
        }
        return ancestors;
    }
}
