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

package ro.cs.tao.workflow.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Enumeration representing the behavior of workflow transitions when encountering errors.
 *
 * TransitionBehavior defines how the execution flow of a workflow should behave if an error occurs
 * at the current node. This can be applicable to both regular nodes and group nodes, determining
 * whether the workflow execution halts or continues based on the defined behavior.
 *
 * Each enum constant is associated with an integer value and a descriptive text, providing both
 * functional and readable representations of the possible transition behaviors.
 *
 * Implements the {@code TaoEnum<Integer>} interface to provide additional properties and behavior,
 * such as obtaining a descriptive friendly name and the enumerated value.
 *
 * Enum constants:
 * 1. FAIL_ON_ERROR: The workflow execution halts if the current node fails.
 *    For group nodes, the entire group fails if any sub-task fails.
 * 2. CONTINUE_ON_ERROR: The workflow execution continues even if the current node fails.
 *    For group nodes, the sub-task failure only affects the current sub-flow, and the group
 *    proceeds with the next sub-flow.
 */
@XmlEnum(Integer.class)
public enum TransitionBehavior implements TaoEnum<Integer> {
    /**
     * The execution flow should not continue if the current node fails.
     * This is the default (and only) value for regular nodes.
     * For group nodes, if a sub-task fails then the group fails.
     */
    @XmlEnumValue("1")
    FAIL_ON_ERROR(1, "Fail on error"),
    /**
     * The execution flow should continue if the current node fails.
     * This is intended for group nodes, such that if a sub-task fails then the current sub-flow fails,
     * but the group continues with the next sub-flow.
     */
    @XmlEnumValue("2")
    CONTINUE_ON_ERROR(2, "Continue on error");

    private final int value;
    private final String description;

    TransitionBehavior(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
