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
package ro.cs.tao.messaging;

/**
 * General topics handled by the registered event bus.
 * @author Cosmin Cara
 */
public class Topics {
    /**
     * Topic for informational messages / events.
     */
    public static final String INFORMATION = "info";
    /**
     * Topic for warning messages / events.
     */
    public static final String WARNING = "warn";
    /**
     * Topic for error messages / events.
     */
    public static final String ERROR = "error";
    /**
     * Topic for signaling progress of various execution components.
     */
    public static final String PROGRESS = "progress";
    /**
     * Special topic for signaling that an execution task has changed its state.
     */
    public static final String TASK_STATUS_CHANGED = "task.changed";
}