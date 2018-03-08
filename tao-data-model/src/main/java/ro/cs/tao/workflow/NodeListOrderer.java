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
    default List<WorkflowNodeDescriptor> orderNodes(List<WorkflowNodeDescriptor> nodes) {
        if (nodes != null) {
            List<WorkflowNodeDescriptor> newList = new ArrayList<>();
            WorkflowNodeDescriptor root = nodes.stream()
                    .filter(n -> n.getIncomingLinks() == null || n.getIncomingLinks().isEmpty())
                    .findFirst().orElse(null);
            if (root == null) {
                throw new IllegalArgumentException("The collection must have exactly one node without incoming links");
            } else {
                newList.add(root);
            }
            Stack<WorkflowNodeDescriptor> stack = new Stack<>();
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
        return masterList.stream().filter(n -> {
            List<ComponentLink> links = n.getIncomingLinks();
            return links != null &&
                    links.stream().anyMatch(l -> node.getComponentId().equals(l.getInput().getParentId()));
        }).collect(Collectors.toList());
    }
}
