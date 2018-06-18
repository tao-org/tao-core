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
package ro.cs.tao.workflow;

import ro.cs.tao.component.ComponentLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public interface NodeListOrderer {

    default List<WorkflowNodeDescriptor> findRoots(List<WorkflowNodeDescriptor> nodes) {
        List<WorkflowNodeDescriptor> roots = null;
        if (nodes != null) {
            List<WorkflowNodeGroupDescriptor> groups =
                    nodes.stream()
                            .filter(n -> n instanceof WorkflowNodeGroupDescriptor)
                            .map(n -> (WorkflowNodeGroupDescriptor)n).collect(Collectors.toList());
            roots = nodes.stream()
                    .filter(n -> (n.getIncomingLinks() == null || n.getIncomingLinks().isEmpty()) &&
                                  groups.stream().noneMatch(g -> g.getNodes() != null && g.getNodes().contains(n)))
                    .collect(Collectors.toList());
            if (roots == null || roots.size() == 0) {
                throw new IllegalArgumentException("The collection must have at least one root node (without incoming links)");
            }
        }
        return roots;
    }

    default List<WorkflowNodeDescriptor> orderNodes(List<WorkflowNodeDescriptor> nodes) {
        if (nodes != null) {
            List<WorkflowNodeDescriptor> newList = new ArrayList<>();
            List<WorkflowNodeDescriptor> roots = findRoots(nodes);
            Stack<WorkflowNodeDescriptor> stack = new Stack<>();
            for (WorkflowNodeDescriptor root : roots) {
                int level = 1;
                root.setLevel(level);
                newList.add(root);
            }
            for (WorkflowNodeDescriptor root : roots) {
                stack.push(root);
                while (!stack.isEmpty()) {
                    List<WorkflowNodeDescriptor> children = findChildren(nodes, stack.pop());
                    if (children != null && children.size() > 0) {
                        children.forEach(n -> {
                            if (!newList.contains(n)) {
                                newList.add(n);
                                stack.push(n);
                            }
                        });
                    }
                }
                stack.clear();
            }
            nodes.clear();
            nodes.addAll(newList);
        }
        return nodes;
    }

    default List<WorkflowNodeDescriptor> findChildren(List<WorkflowNodeDescriptor> masterList,
                                                      WorkflowNodeDescriptor node) {
        if (masterList == null || masterList.size() == 0 || node == null) {
            return null;
        }
        List<WorkflowNodeDescriptor> children;
        if (node instanceof WorkflowNodeGroupDescriptor) {
            children = ((WorkflowNodeGroupDescriptor) node).getNodes().stream().distinct().collect(Collectors.toList());
        } else {
            children = masterList.stream().filter(n -> {
                List<ComponentLink> links = n.getIncomingLinks();
                return links != null && links.stream().anyMatch(l -> node.getId().equals(l.getSourceNodeId()));
                //links.stream().anyMatch(l -> node.getComponentId().equals(l.getInput().getParentId()));
            }).distinct().collect(Collectors.toList());
        }
        children.forEach(c -> c.setLevel(node.getLevel() + 1));
        return children;
    }
}
