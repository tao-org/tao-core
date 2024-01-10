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
package ro.cs.tao.execution.model;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Enumeration for the possible statuses of an execution (either for a task or for a job).
 *
 * @author Cosmin Udroiu
 */
@XmlEnum(Integer.class)
public enum ExecutionStatus implements TaoEnum<Integer> {
    /**
     * Job status cannot be determined.
     */
    @XmlEnumValue("0")
    UNDETERMINED(0, "Not started"),
    /**
     *  Job is queued and active.
     */
    @XmlEnumValue("1")
    QUEUED_ACTIVE(1, "Queued"),
    /**
     * Job is running.
     */
    @XmlEnumValue("2")
    RUNNING(2, "Running"),
    /**
     * Job is suspended.
     */
    @XmlEnumValue("3")
    SUSPENDED(3, "Suspended"),
    /**
     * Job has finished normally.
     */
    @XmlEnumValue("4")
    DONE(4, "Done"),
    /**
     * Job finished, but terminated abnormally.
     */
    @XmlEnumValue("5")
    FAILED(5, "Failed"),
    /**
     * Job cancelled.
     */
    @XmlEnumValue("6")
    CANCELLED(6, "Cancelled"),
    /**
     * Job finished execution, but is not yet prepared for persistence.
     */
    @XmlEnumValue("7")
    PENDING_FINALISATION(7, "Pending finalisation");

    private final int value;
    private final String description;

    ExecutionStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }

}
